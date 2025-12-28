package com.alessandro.astages.mixin.item.ftbquests;

import com.alessandro.astages.integration.ftbquests.networking.packet.TaskAvailabilityCache;
import com.alessandro.astages.integration.ftbquests.networking.packet.TaskAvailabilityRequestC2SPacket;
import com.alessandro.astages.integration.ftbquests.tasks.SinkItemTask;
import com.alessandro.astages.networking.ANetworking;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftbquests.client.gui.quests.TaskButton;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TaskButton.class)
public abstract class TaskButtonMixin
{
    @Shadow
    Task task;
    
    // Prevent duplicate requests if FTB rebuilds buttons
    
    @Inject(method = "<init>(Ldev/ftb/mods/ftblibrary/ui/Panel;Ldev/ftb/mods/ftbquests/quest/task/Task;)V", at = @At("TAIL"))
    private void astages$onCreate(Panel panel, Task task, CallbackInfo ci)
    {
        if (!(task instanceof SinkItemTask itemTask))
            return;
        
        if (Minecraft.getInstance().player == null)
            return;
        
        long id = itemTask.getId();
        
//        if (TaskAvailabilityCache.get(id) != null)
//            return;

        ANetworking.sendToServer(
            new TaskAvailabilityRequestC2SPacket(id)
        );
    }
    
    @Inject(
        method = "draw(Lnet/minecraft/client/gui/GuiGraphics;Ldev/ftb/mods/ftblibrary/ui/Theme;IIII)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void astages$customDraw(
        GuiGraphics graphics,
        Theme theme,
        int x, int y, int w, int h,
        CallbackInfo ci)
    {
        ci.cancel(); // 💥 stop original logic
        
        int bs = h >= 32 ? 32 : 16;
        TaskButton self = (TaskButton) (Object) this;
        self.drawBackground(graphics, theme, x, y, w, h);
        self.drawIcon(graphics, theme, x + (w - bs) / 2, y + (h - bs) / 2, bs, bs);
        
        if (!(task instanceof SinkItemTask itemTask))
            return;
        
        var player = Minecraft.getInstance().player;
        if (player == null)
            return;
        
        TeamData teamData = TeamData.get(player);
        
        if (teamData == null)
            return;
        
        if (teamData.isCompleted(task))
        {
            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.translate(0, 0, 200);
            float centerY = y + h - 10F; // bottom center, tweak if needed
            RenderSystem.enableBlend();
            ThemeProperties.CHECK_ICON.get().draw(graphics, x + w - 9, (int) centerY, 9, 9);
            
            pose.popPose();
        }
        else
        {
            aStagesExtra$drawButtonText(graphics, theme, x, y, w, h, itemTask, teamData, player);
        }
    }
    
    @Unique
    private static void aStagesExtra$drawButtonText(GuiGraphics graphics, Theme theme, int x, int y, int w, int h, SinkItemTask itemTask, TeamData teamData, LocalPlayer player)
    {
        TaskAvailabilityCache.Data cached = TaskAvailabilityCache.get(itemTask.getId());
        
        if (cached == null)
        {
            // Pure rendering fallback — no networking
            Component loading = Component.literal("…");
            theme.drawString(graphics, loading, x + w / 2 - 2, y + h - 6, Color4I.GRAY, Theme.SHADOW);
            return;
        }
        
        long remaining = Math.max(0, itemTask.getMaxProgress() - teamData.getProgress(itemTask));
        
        Component text = Component.literal(
            StringUtils.formatDouble(cached.have(), true)
                + "/"
                + StringUtils.formatDouble(remaining, true)
        );
        
        int textWidth = theme.getStringWidth(text);
        float centerX = x + w / 2F - (textWidth / 2F) * 0.5F;
        float centerY = y + h - 6F; // bottom center, tweak if needed
        
        PoseStack pose = graphics.pose();
        
        pose.pushPose();
        pose.translate(centerX, centerY, 200);
        pose.scale(0.5F, 0.5F, 1F);
        
        theme.drawString(
            graphics,
            text,
            0, 0,
            cached.canSubmit() ? Color4I.rgb(0x55FF55) : Color4I.WHITE,
            Theme.SHADOW
        );
        
        pose.popPose();
    }
}

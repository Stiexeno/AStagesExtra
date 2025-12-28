package com.alessandro.astages.mixin.item.ftbquests;

import com.alessandro.astages.integration.ftbquests.tasks.SinkItemTask;
import com.alessandro.astages.integration.ftbquests.tasks.consumeContext.ItemConsumeManager;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftbquests.client.gui.quests.TaskButton;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TaskButton.class)
public abstract class TaskButtonMixin
{
    @Shadow
    Task task;
    
    @Redirect(
        method = "draw(Lnet/minecraft/client/gui/GuiGraphics;Ldev/ftb/mods/ftblibrary/ui/Theme;IIII)V",
        at = @At(
            value = "INVOKE",
            target = "Ldev/ftb/mods/ftblibrary/ui/Theme;drawString(Lnet/minecraft/client/gui/GuiGraphics;Ljava/lang/Object;IILdev/ftb/mods/ftblibrary/icon/Color4I;I)I"
        )
    )
    private int astages$drawRemainingAmount(
        Theme theme,
        GuiGraphics graphics,
        Object originalText,
        int x,
        int y,
        Color4I originalColor,
        int shadow
    )
    {
        // Only affect ItemTask (including your SinkItemTask)
        if (!(task instanceof ItemTask itemTask))
        {
            return theme.drawString(graphics, originalText, x, y, originalColor, shadow);
        }
        
        TeamData teamData = null;
        
        if (Minecraft.getInstance().player != null)
        {
            teamData = TeamData.get(Minecraft.getInstance().player);
        }
        
        if (teamData == null)
        {
            return theme.drawString(graphics, originalText, x, y, originalColor, shadow);
        }
        
        long max = itemTask.getMaxProgress();
        long current = teamData.getProgress(itemTask);
        long remaining = Math.max(0, max - current);
        
        // Build new text
        Component newText = Component.literal(Long.toString(remaining));
        
        boolean canSubmitNow = true;
        
        if (itemTask instanceof SinkItemTask sink && Minecraft.getInstance().player != null)
        {
            canSubmitNow = ItemConsumeManager.canConsume(
                Minecraft.getInstance().player,
                sink,
                remaining
            );
        }
        
        Color4I color = canSubmitNow
            ? Color4I.rgb(0x55FF55) // green
            : originalColor;
        
        int width = theme.drawString(graphics, newText, x, y, color, shadow);
//
//        // Draw small green check icon if can submit
//        if (canSubmitNow)
//        {
//            graphics.pose().pushPose();
//            graphics.pose().translate(0, 0, 210);
//
//            Icon check = ThemeProperties.CHECK_ICON.get();
//
//            check.draw(
//                graphics,
//                x + width + 1,
//                y - 1,
//                6,
//                6
//            );
//
//            graphics.pose().popPose();
//        }
        
        return width;
    }
}

package com.alessandro.astages.mixin.item.ftbquests;

import com.alessandro.astages.integration.ftbquests.tasks.SinkItemTask;
import com.alessandro.astages.integration.ftbquests.tasks.consumeContext.ItemConsumeManager;
import dev.ftb.mods.ftblibrary.icon.Color4I;
import dev.ftb.mods.ftblibrary.ui.Theme;
import dev.ftb.mods.ftblibrary.util.StringUtils;
import dev.ftb.mods.ftbquests.client.gui.quests.TaskButton;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
        int shadow)
    {
        if (!(task instanceof SinkItemTask itemTask))
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
        
        long have = ItemConsumeManager.countAvailable(
            Minecraft.getInstance().player,
            itemTask,
            remaining
        );
        
        boolean canSubmitNow = ItemConsumeManager.canConsume(
            Minecraft.getInstance().player,
            itemTask,
            remaining
        );
        
        String text = StringUtils.formatDouble(have, true) + "/" + StringUtils.formatDouble(remaining, true);
        
        Color4I color = canSubmitNow
            ? Color4I.rgb(0x55FF55) // green
            : originalColor;
        
        int textWidth = theme.getStringWidth(text);
        int centeredX = -textWidth / 2;
        
        return theme.drawString(graphics, text, centeredX, y, color, shadow);
    }
}

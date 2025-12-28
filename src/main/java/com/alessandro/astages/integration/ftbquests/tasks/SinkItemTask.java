package com.alessandro.astages.integration.ftbquests.tasks;

import com.alessandro.astages.integration.ftbquests.tasks.consumeContext.ItemConsumeManager;
import com.simibubi.create.foundation.item.TooltipHelper;
import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.util.TooltipList;
import dev.ftb.mods.ftbquests.item.MissingItem;
import dev.ftb.mods.ftbquests.net.SubmitTaskMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import static net.minecraft.ChatFormatting.GRAY;
import static net.minecraft.client.gui.screens.Screen.hasShiftDown;

public class SinkItemTask extends ItemTask
{
    private static final int RADIUS = 10;
    
    public SinkItemTask(long id, Quest quest)
    {
        super(id, quest);
        setConsumeItems(Tristate.TRUE);
    }
    
    @Override
    public TaskType getType()
    {
        return AFTBTasks.SINK_ITEM_TASK;
    }
    
    @Override
    public void onButtonClicked(Button button, boolean canClick)
    {
        NetworkManager.sendToServer(new SubmitTaskMessage(getId()));
    }
    
    @Override
    public void submitTask(TeamData teamData, ServerPlayer player, ItemStack craftedItem)
    {
        if (!checkTaskSequence(teamData) || teamData.isCompleted(this) || getItemStack().getItem() instanceof MissingItem)
            return;
        
        long remaining = getMaxProgress() - teamData.getProgress(this);
        
        if (remaining <= 0)
            return;
        
        ItemConsumeManager.consume(
            player,
            teamData,
            this,
            remaining
        );
    }
    
    @Override
    public void addMouseOverText(TooltipList list, TeamData teamData)
    {
        list.getLines().clear();
        var itemName = getItemStack().getHoverName();
        
        list.add(itemName);
        
        list.add(TooltipHelper.holdShift(FontHelper.Palette.YELLOW, false));
        
        if (!hasShiftDown())
            return;
        
        list.add(Component.empty());
        list.add(Component.translatable("tooltip.astages.sink_item.condition1").withStyle(GRAY));
        
        MutableComponent[] components = new MutableComponent[]{
            Component.translatable("tooltip.astages.sink_item.behaviour1"),
            Component.translatable("tooltip.astages.sink_item.behaviour2"),
            Component.translatable("tooltip.astages.sink_item.behaviour3"),
            Component.translatable("tooltip.astages.sink_item.behaviour4"),
        };
        
        for (MutableComponent component : components)
        {
            for (Component line : TooltipHelper.cutTextComponent(component, FontHelper.Palette.STANDARD_CREATE))
            {
                list.add(line);
            }
        }
    }
}

package com.alessandro.astages.integration.ftbquests.tasks;

import com.alessandro.astages.integration.ftbquests.tasks.consumeContext.ItemConsumeManager;
import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftbquests.item.MissingItem;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

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
}

package com.alessandro.astages.integration.ftbquests.tasks;

import com.alessandro.astages.integration.ftbquests.tasks.consumeContext.ItemConsumeContext;
import com.alessandro.astages.integration.ftbquests.tasks.consumeContext.ItemConsumeManager;
import dev.ftb.mods.ftblibrary.config.Tristate;
import dev.ftb.mods.ftbquests.item.MissingItem;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.IdentityHashMap;
import java.util.Map;

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
        
        ItemConsumeContext ctx = new ItemConsumeContext(
            player,
            teamData,
            this,
            getItemStack(),
            remaining,
            RADIUS
        );
        
        ItemConsumeManager.consumeAll(ctx);
    }
    
    private void consumeFromNearbyInventories(TeamData teamData, ServerPlayer player)
    {
        long remainingNeeded = getRemainingNeeded(teamData);
        if (remainingNeeded <= 0) return;
        
        Level level = player.level();
        BlockPos center = player.blockPosition();
        
        // Avoid double-consuming the same handler (many blocks expose per-side handlers)
        Map<IItemHandler, Boolean> visited = new IdentityHashMap<>();
        
        BlockPos min = center.offset(-RADIUS, -RADIUS, -RADIUS);
        BlockPos max = center.offset(RADIUS, RADIUS, RADIUS);
        
        for (BlockPos pos : BlockPos.betweenClosed(min, max))
        {
            if (remainingNeeded <= 0) break;
            
            // Query all sides + null context (some providers use null)
            remainingNeeded = consumeFromPos(level, pos, teamData, remainingNeeded, visited);
        }
    }
    
    private long consumeFromPos(
        Level level,
        BlockPos pos,
        TeamData teamData,
        long remainingNeeded,
        Map<IItemHandler, Boolean> visited
    )
    {
        // Try null context first, then all 6 sides
        remainingNeeded = consumeFromHandlerIfPresent(level, pos, null, teamData, remainingNeeded, visited);
        if (remainingNeeded <= 0) return 0;
        
        for (Direction dir : Direction.values())
        {
            remainingNeeded = consumeFromHandlerIfPresent(level, pos, dir, teamData, remainingNeeded, visited);
            if (remainingNeeded <= 0) return 0;
        }
        
        return remainingNeeded;
    }
    
    private long consumeFromHandlerIfPresent(
        Level level,
        BlockPos pos,
        Direction side,
        TeamData teamData,
        long remainingNeeded,
        Map<IItemHandler, Boolean> visited
    )
    {
        IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
        if (handler == null) return remainingNeeded;
        
        if (visited.put(handler, Boolean.TRUE) != null)
        {
            // already processed this exact handler instance
            return remainingNeeded;
        }
        
        for (int slot = 0; slot < handler.getSlots(); slot++)
        {
            if (remainingNeeded <= 0) break;
            
            ItemStack inSlot = handler.getStackInSlot(slot);
            if (inSlot.isEmpty()) continue;
            
            // Use ItemTask's own matching logic (respects matchComponents setting, etc.)
            if (!test(inSlot)) continue;
            
            int canTake = (int) Math.min(Integer.MAX_VALUE, remainingNeeded);
            int take = Math.min(inSlot.getCount(), canTake);
            if (take <= 0) continue;
            
            // Actually remove from storage
            ItemStack extracted = handler.extractItem(slot, take, false);
            if (extracted.isEmpty()) continue;
            
            // Update quest progress by what we REALLY extracted
            teamData.addProgress(this, extracted.getCount());
            remainingNeeded -= extracted.getCount();
            
            if (teamData.isCompleted(this)) break;
        }
        
        return remainingNeeded;
    }
    
    private long getRemainingNeeded(TeamData teamData)
    {
        long max = getMaxProgress(); // this is ItemTask.count :contentReference[oaicite:5]{index=5}
        long progress = teamData.getProgress(this);
        return Math.max(0L, max - progress);
    }
}

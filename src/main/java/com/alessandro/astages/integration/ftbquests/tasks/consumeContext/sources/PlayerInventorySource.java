package com.alessandro.astages.integration.ftbquests.tasks.consumeContext.sources;

import com.alessandro.astages.api.nullability.Nullable;
import com.alessandro.astages.integration.ftbquests.tasks.consumeContext.ItemConsumeSource;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PlayerInventorySource implements ItemConsumeSource
{
    @Override
    public boolean isAvailable()
    {
        return true;
    }
    
    @Override
    public long count(Player player, ItemTask task, long limit) {
        long found = 0;
        
        var inventory = player.getInventory();
        for (ItemStack stack : inventory.items) {
            if (stack.isEmpty()) continue;
            if (!task.test(stack)) continue;
            
            found += stack.getCount();
            if (found >= limit) return limit;
        }
        
        return found;
    }
    
    @Override
    public long process(
        Player player,
        ItemTask task,
        long remaining,
        boolean simulate,
        @Nullable TeamData teamData)
    {
        var inventory = player.getInventory();
        
        for (int i = 0; i < inventory.items.size() && remaining > 0; i++)
        {
            ItemStack stack = inventory.items.get(i);
            if (stack.isEmpty()) continue;
            if (!task.test(stack)) continue;
            
            int matched = Math.min(stack.getCount(), (int) remaining);
            
            if (!simulate)
            {
                stack.shrink(matched);
                
                if (teamData != null)
                {
                    teamData.addProgress(task, matched);
                }
                
                if (stack.isEmpty())
                {
                    inventory.items.set(i, ItemStack.EMPTY);
                }
            }
            
            remaining -= matched;
        }
        
        if (!simulate && player instanceof ServerPlayer sp)
        {
            inventory.setChanged();
            sp.containerMenu.broadcastChanges();
        }
        
        return remaining;
    }
}

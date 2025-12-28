package com.alessandro.astages.integration.ftbquests.consume;

import com.alessandro.astages.api.nullability.Nullable;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.Collection;

public final class ItemHandlerConsumer
{
    public long count(Player player, Collection<HandlerRef> handlers, ItemTask task, long limit)
    {
        long found = 0;
        
        for (HandlerRef ref : handlers)
        {
            IItemHandler h = ref.handler();
            
            for (int i = 0; i < h.getSlots(); i++)
            {
                ItemStack stack = h.getStackInSlot(i);
                
                if (stack.isEmpty())
                    continue;
                
                if (!task.test(stack))
                    continue;
                
                found += stack.getCount();
                
                if (found >= limit)
                    return limit;
            }
        }
        
        return found;
    }
    
    public long consume(Player player,
                        Collection<HandlerRef> handlers,
                        ItemTask task,
                        long remaining,
                        boolean simulate,
                        @Nullable TeamData teamData)
    {
        for (HandlerRef ref : handlers)
        {
            IItemHandler h = ref.handler();
            
            for (int i = 0; i < h.getSlots() && remaining > 0; i++)
            {
                while (remaining > 0)
                {
                    ItemStack stack = h.getStackInSlot(i);
                    
                    if (stack.isEmpty())
                        break;
                    
                    if (!task.test(stack))
                        break;
                    
                    int toTake = (int) Math.min(stack.getCount(), remaining);
                    if (toTake <= 0) break;
                    
                    if (simulate)
                    {
                        remaining -= toTake;
                        break; // simulate should not loop-extract
                    }
                    
                    ItemStack extracted = h.extractItem(i, toTake, false);
                    if (extracted.isEmpty()) break;
                    
                    remaining -= extracted.getCount();
                    
                    if (teamData != null)
                    {
                        teamData.addProgress(task, extracted.getCount());
                    }
                }
            }
            
            if (!simulate && ref.onChanged() != null)
            {
                ref.onChanged().run();
            }
            
            if (remaining <= 0)
                break;
        }
        
        return remaining;
    }
}

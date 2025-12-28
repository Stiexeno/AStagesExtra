package com.alessandro.astages.integration.ftbquests.tasks.consumeContext.sources;

import com.alessandro.astages.integration.ftbquests.tasks.consumeContext.ItemConsumeContext;
import com.alessandro.astages.integration.ftbquests.tasks.consumeContext.ItemConsumeSource;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class PlayerInventorySource implements ItemConsumeSource
{
    @Override
    public boolean isAvailable()
    {
        return true; // always available
    }
    
    @Override
    public long consume(ItemConsumeContext ctx)
    {
        ServerPlayer player = ctx.player;
        ItemTask task = ctx.task;
        
        var inventory = player.getInventory();
        
        for (int i = 0; i < inventory.items.size(); i++)
        {
            if (ctx.remaining <= 0) break;
            
            ItemStack stack = inventory.items.get(i);
            
            if (stack.isEmpty())
                continue;
            
            if (!task.test(stack))
                continue;
            
            int toConsume = (int) Math.min(stack.getCount(), ctx.remaining);
            
            stack.shrink(toConsume);
            
            ctx.teamData.addProgress(task, toConsume);
            ctx.remaining -= toConsume;
            
            if (stack.isEmpty())
            {
                inventory.items.set(i, ItemStack.EMPTY);
            }
        }
        
        inventory.setChanged();
        player.containerMenu.broadcastChanges();
        
        return ctx.remaining;
    }
}

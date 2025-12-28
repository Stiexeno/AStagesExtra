package com.alessandro.astages.integration.ftbquests.tasks.consumeContext.sources;

import com.alessandro.astages.integration.ftbquests.tasks.consumeContext.ItemConsumeContext;
import com.alessandro.astages.integration.ftbquests.tasks.consumeContext.ItemConsumeSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

public class VanillaBlockItemHandlerSource implements ItemConsumeSource
{
    @Override
    public boolean isAvailable()
    {
        return true;
    }
    
    @Override
    public long consume(ItemConsumeContext ctx)
    {
        BlockPos min = ctx.center.offset(-ctx.radius, -ctx.radius, -ctx.radius);
        BlockPos max = ctx.center.offset(ctx.radius, ctx.radius, ctx.radius);
        
        for (BlockPos pos : BlockPos.betweenClosed(min, max))
        {
            if (ctx.remaining <= 0) break;
            
            for (Direction dir : Direction.values())
            {
                IItemHandler handler =
                    ctx.level.getCapability(
                        Capabilities.ItemHandler.BLOCK, pos, dir);
                
                if (handler == null || !ctx.visited.add(handler))
                    continue;
                
                ctx.remaining = consumeFromHandler(handler, ctx);
            }
        }
        
        return ctx.remaining;
    }
    
    private long consumeFromHandler(IItemHandler handler, ItemConsumeContext ctx)
    {
        for (int slot = 0; slot < handler.getSlots(); slot++)
        {
            if (ctx.remaining <= 0) break;
            
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty() || !ctx.task.test(stack)) continue;
            
            int toTake = (int) Math.min(stack.getCount(), ctx.remaining);
            ItemStack extracted = handler.extractItem(slot, toTake, false);
            
            if (!extracted.isEmpty())
            {
                ctx.teamData.addProgress(ctx.task, extracted.getCount());
                ctx.remaining -= extracted.getCount();
            }
        }
        return ctx.remaining;
    }
}

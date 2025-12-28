package com.alessandro.astages.integration.ftbquests.tasks.consumeContext.sources;

import com.alessandro.astages.integration.ftbquests.tasks.consumeContext.ItemConsumeSource;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class VanillaBlockItemHandlerSource implements ItemConsumeSource {
    
    private static final int RADIUS = 10;
    
    @Override
    public boolean isAvailable() {
        return true;
    }
    
    @Override
    public long process(
        Player player,
        ItemTask task,
        long remaining,
        boolean simulate,
        @Nullable TeamData teamData
    ) {
        if (remaining <= 0) return 0;
        
        Level level = player.level();
        BlockPos center = player.blockPosition();
        
        BlockPos min = center.offset(-RADIUS, -RADIUS, -RADIUS);
        BlockPos max = center.offset(RADIUS, RADIUS, RADIUS);
        
        // Avoid double-visiting the same handler via multiple sides
        Set<IItemHandler> visited = new HashSet<>();
        
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (remaining <= 0) break;
            
            for (Direction dir : Direction.values()) {
                IItemHandler handler =
                    level.getCapability(Capabilities.ItemHandler.BLOCK, pos, dir);
                
                if (handler == null || !visited.add(handler)) {
                    continue;
                }
                
                remaining = processHandler(
                    handler,
                    task,
                    remaining,
                    simulate,
                    teamData
                );
                
                if (remaining <= 0) break;
            }
        }
        
        return remaining;
    }
    
    private long processHandler(
        IItemHandler handler,
        ItemTask task,
        long remaining,
        boolean simulate,
        @Nullable TeamData teamData
    ) {
        for (int slot = 0; slot < handler.getSlots() && remaining > 0; slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            if (!task.test(stack)) continue;
            
            int toTake = (int) Math.min(stack.getCount(), remaining);
            
            if (simulate) {
                // count only
                remaining -= toTake;
            } else {
                ItemStack extracted = handler.extractItem(slot, toTake, false);
                if (!extracted.isEmpty()) {
                    remaining -= extracted.getCount();
                    if (teamData != null) {
                        teamData.addProgress(task, extracted.getCount());
                    }
                }
            }
        }
        
        return remaining;
    }
}

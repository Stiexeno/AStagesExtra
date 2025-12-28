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

public class StorageBlockSource implements ItemConsumeSource
{
    private static final int RADIUS = 10;
    
    @Override
    public boolean isAvailable()
    {
        return true;
    }
    
    @Override
    public long count(Player player, ItemTask task, long limit)
    {
        if (limit <= 0) return 0;
        
        Level level = player.level();
        long found = 0;
        
        Set<Object> visited = new HashSet<>();
        
        for (InventoryEntry entry : iterateStorages(level, player.blockPosition(), visited))
        {
            IItemHandler handler = entry.handler();
            
            for (int slot = 0; slot < handler.getSlots(); slot++)
            {
                ItemStack stack = handler.getStackInSlot(slot);
                if (stack.isEmpty()) continue;
                if (!task.test(stack)) continue;
                
                found += stack.getCount();
                if (found >= limit) return limit;
            }
        }
        
        return found;
    }
    
    @Override
    public long process(
        Player player,
        ItemTask task,
        long remaining,
        boolean simulate,
        @Nullable TeamData teamData
    )
    {
        if (remaining <= 0) return 0;
        
        Level level = player.level();
        Set<Object> visited = new HashSet<>();
        
        for (InventoryEntry entry : iterateStorages(level, player.blockPosition(), visited))
        {
            remaining = processHandler(
                entry.handler(),
                task,
                remaining,
                simulate,
                teamData
            );
            
            if (remaining <= 0)
                break;
        }
        
        return remaining;
    }
    
    private Iterable<InventoryEntry> iterateStorages(
        Level level,
        BlockPos center,
        Set<Object> visited
    )
    {
        BlockPos min = center.offset(-RADIUS, -RADIUS, -RADIUS);
        BlockPos max = center.offset(RADIUS, RADIUS, RADIUS);
        
        return () -> new java.util.Iterator<>()
        {
            private final java.util.Iterator<BlockPos> posIterator =
                BlockPos.betweenClosed(min, max).iterator();
            
            private InventoryEntry next;
            
            @Override
            public boolean hasNext()
            {
                while (next == null && posIterator.hasNext())
                {
                    BlockPos pos = posIterator.next();
                    next = createEntry(level, pos, visited);
                }
                return next != null;
            }
            
            @Override
            public InventoryEntry next()
            {
                if (!hasNext())
                    throw new java.util.NoSuchElementException();
                
                InventoryEntry result = next;
                next = null;
                return result;
            }
        };
    }
    
    private InventoryEntry createEntry(
        Level level,
        BlockPos pos,
        Set<Object> visited
    )
    {
        var be = level.getBlockEntity(pos);
        if (be == null) return null;
        if (!isPlayerStorage(be)) return null;
        
        IItemHandler handler =
            level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        
        if (handler == null) return null;
        
        Object key = getStableInventoryKey(level, pos, be);
        if (!visited.add(key)) return null;
        
        return new InventoryEntry(handler);
    }
    
    private static Object getStableInventoryKey(
        Level level,
        BlockPos pos,
        net.minecraft.world.level.block.entity.BlockEntity be
    )
    {
        var state = level.getBlockState(pos);
        
        if (state.getBlock() instanceof net.minecraft.world.level.block.ChestBlock)
        {
            var chestType = state.getValue(net.minecraft.world.level.block.ChestBlock.TYPE);
            if (chestType != net.minecraft.world.level.block.state.properties.ChestType.SINGLE)
            {
                Direction dir = net.minecraft.world.level.block.ChestBlock.getConnectedDirection(state);
                BlockPos other = pos.relative(dir);
                return canonicalPairPos(pos, other);
            }
            return pos.immutable();
        }
        
        return be;
    }
    
    private static BlockPos canonicalPairPos(BlockPos a, BlockPos b)
    {
        if (a.getY() != b.getY()) return a.getY() < b.getY() ? a.immutable() : b.immutable();
        if (a.getZ() != b.getZ()) return a.getZ() < b.getZ() ? a.immutable() : b.immutable();
        return a.getX() < b.getX() ? a.immutable() : b.immutable();
    }
    
    private static boolean isPlayerStorage(net.minecraft.world.level.block.entity.BlockEntity be)
    {
        return be instanceof net.minecraft.world.level.block.entity.ChestBlockEntity
            || be instanceof net.minecraft.world.level.block.entity.BarrelBlockEntity
            || be instanceof net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
    }
    
    private long processHandler(
        IItemHandler handler,
        ItemTask task,
        long remaining,
        boolean simulate,
        @Nullable TeamData teamData
    )
    {
        for (int slot = 0; slot < handler.getSlots() && remaining > 0; slot++)
        {
            ItemStack stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            if (!task.test(stack)) continue;
            
            int toTake = (int) Math.min(stack.getCount(), remaining);
            
            if (simulate)
            {
                remaining -= toTake;
            }
            else
            {
                ItemStack extracted = handler.extractItem(slot, toTake, false);
                if (!extracted.isEmpty())
                {
                    remaining -= extracted.getCount();
                    if (teamData != null)
                        teamData.addProgress(task, extracted.getCount());
                }
            }
        }
        
        return remaining;
    }
    
    private record InventoryEntry(IItemHandler handler)
    {
    }
}


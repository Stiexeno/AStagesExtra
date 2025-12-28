package com.alessandro.astages.integration.ftbquests.consume.locators;

import com.alessandro.astages.integration.ftbquests.consume.HandlerRef;
import com.alessandro.astages.integration.ftbquests.consume.ItemHandlerLocator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public final class BlockItemLocator implements ItemHandlerLocator
{
    private final int radius;
    
    public BlockItemLocator(int radius)
    {
        this.radius = radius;
    }
    
    @Override
    public boolean isAvailable()
    {
        return true;
    }
    
    @Override
    public void find(Player player, Consumer<HandlerRef> out)
    {
        Level level = player.level();
        BlockPos center = player.blockPosition();
        
        BlockPos min = center.offset(-radius, -radius, -radius);
        BlockPos max = center.offset(radius, radius, radius);
        
        Set<IItemHandler> visitedHandlers = new HashSet<>();
        
        for (BlockPos pos : BlockPos.betweenClosed(min, max))
        {
            BlockState state = level.getBlockState(pos);
            
            if (state.getBlock() instanceof ChestBlock ||
                state.getBlock() instanceof net.p3pp3rf1y.sophisticatedstorage.block.ChestBlock)
            {
                ChestType type = state.getValue(ChestBlock.TYPE);
                if (type != ChestType.SINGLE)
                {
                    Direction dir = ChestBlock.getConnectedDirection(state);
                    BlockPos other = pos.relative(dir);
                    if (!canonical(pos, other).equals(pos))
                        continue;
                }
            }
            
            IItemHandler handler = level.getCapability(
                Capabilities.ItemHandler.BLOCK, pos, null);
            
            if (handler == null)
                continue;
            
            if (!visitedHandlers.add(handler))
                continue;
            
            out.accept(new HandlerRef(handler, handler, null));
        }
    }
    
    private static BlockPos canonical(BlockPos a, BlockPos b)
    {
        if (a.getY() != b.getY()) return a.getY() < b.getY() ? a : b;
        if (a.getZ() != b.getZ()) return a.getZ() < b.getZ() ? a : b;
        return a.getX() < b.getX() ? a : b;
    }
}

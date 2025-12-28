package com.alessandro.astages.integration.ftbquests.consume.locators;

import com.alessandro.astages.integration.ftbquests.consume.HandlerRef;
import com.alessandro.astages.integration.ftbquests.consume.ItemHandlerLocator;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.function.Consumer;

public final class PlayerItemLocator implements ItemHandlerLocator
{
    @Override
    public boolean isAvailable()
    {
        return true;
    }
    
    @Override
    public void find(Player player, Consumer<HandlerRef> out)
    {
        var inv = player.getInventory();
        
        // main inventory
        for (int slot = 0; slot < inv.items.size(); slot++)
        {
            ItemStack stack = inv.items.get(slot);
            findFromStack(player, stack, new SlotKey(player, slot), out);
        }
        
        // armor + offhand
        int idx = inv.items.size();
        for (ItemStack stack : inv.armor)
        {
            findFromStack(player, stack, new SlotKey(player, idx++), out);
        }
        
        findFromStack(player, inv.offhand.getFirst(), new SlotKey(player, idx), out);
    }
    
    private void findFromStack(
        Player player,
        ItemStack stack,
        Object key,
        Consumer<HandlerRef> out)
    {
        if (stack.isEmpty())
            return;
        
        IItemHandler handler = stack.getCapability(Capabilities.ItemHandler.ITEM);
        
        if (handler == null)
            return;
        
        Runnable onChanged = () ->
        {
            player.getInventory().setChanged();
            
            if (player instanceof ServerPlayer sp)
            {
                sp.containerMenu.broadcastChanges();
            }
        };
        
        out.accept(new HandlerRef(key, handler, onChanged));
    }
    
    private record SlotKey(Player player, int slot)
    {
    }
}

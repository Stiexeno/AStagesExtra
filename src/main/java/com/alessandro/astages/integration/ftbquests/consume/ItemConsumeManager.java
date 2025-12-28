package com.alessandro.astages.integration.ftbquests.consume;

import com.alessandro.astages.integration.ftbquests.consume.locators.BlockItemLocator;
import com.alessandro.astages.integration.ftbquests.consume.locators.PlayerItemLocator;
import com.alessandro.astages.integration.ftbquests.networking.packet.TaskAvailabilityRequestC2SPacket;
import com.alessandro.astages.networking.ANetworking;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ItemConsumeManager
{
    private static final List<ItemHandlerLocator> LOCATORS = List.of(
        new PlayerItemLocator(),
        new BlockItemLocator(16)
    );
    
    private static final ItemHandlerConsumer CONSUMER = new ItemHandlerConsumer();
    
    public static long countAvailable(
        Player player,
        ItemTask task,
        long limit)
    {
        return CONSUMER.count(player, discover(player), task, limit);
    }
    
    public static void consume(
        ServerPlayer player,
        TeamData teamData,
        ItemTask task,
        long amount)
    {
        long remaining = CONSUMER.consume(
            player,
            discover(player),
            task,
            amount,
            false,
            teamData
        );
        
        if (remaining > 0)
        {
            ANetworking.sendToServer(new TaskAvailabilityRequestC2SPacket(task.getId())
            );
        }
    }
    
    private static List<HandlerRef> discover(Player player)
    {
        Map<Object, HandlerRef> result = new LinkedHashMap<>();
        
        for (ItemHandlerLocator locator : LOCATORS)
        {
            if (!locator.isAvailable())
                continue;
            
            locator.find(player, ref ->
                result.putIfAbsent(ref.stableKey(), ref)
            );
        }
        
        return new ArrayList<>(result.values());
    }
    
    private ItemConsumeManager()
    {
    }
}


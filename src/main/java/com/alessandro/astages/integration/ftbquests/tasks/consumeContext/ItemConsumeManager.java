package com.alessandro.astages.integration.ftbquests.tasks.consumeContext;

import com.alessandro.astages.integration.ftbquests.tasks.consumeContext.sources.PlayerInventorySource;
import com.alessandro.astages.integration.ftbquests.tasks.consumeContext.sources.VanillaBlockItemHandlerSource;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public final class ItemConsumeManager
{
    
    private static final List<ItemConsumeSource> SOURCES = new ArrayList<>();
    
    static
    {
        SOURCES.add(new PlayerInventorySource());
        SOURCES.add(new VanillaBlockItemHandlerSource());
    }
    
    public static boolean canConsume(
        Player player,
        ItemTask task,
        long amount)
    {
        long remaining = amount;
        
        for (var source : SOURCES)
        {
            if (!source.isAvailable()) continue;
            remaining = source.process(player, task, remaining, true, null);
            if (remaining <= 0) return true;
        }
        
        return false;
    }
    
    public static long countAvailable(
        Player player,
        ItemTask task,
        long limit) {
        long found = 0;
        
        for (ItemConsumeSource source : SOURCES) {
            if (!source.isAvailable()) continue;
            found += source.count(player, task, limit - found);
            if (found >= limit) return limit;
        }
        
        return found;
    }
    
    public static boolean consume(
        ServerPlayer player,
        TeamData teamData,
        ItemTask task,
        long amount)
    {
        long remaining = amount;
        
        for (var source : SOURCES)
        {
            if (!source.isAvailable())
                continue;
            
            remaining = source.process(player, task, remaining, false, teamData);
            
            if (remaining <= 0)
                return true;
        }
        
        return false;
    }
    
    private ItemConsumeManager()
    {
    }
}

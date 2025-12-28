package com.alessandro.astages.integration.ftbquests.tasks.consumeContext;

import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.IdentityHashMap;
import java.util.Set;

public final class ItemConsumeContext
{
    public final ServerPlayer player;
    public final TeamData teamData;
    public final ItemTask task;
    
    public final ItemStack requiredPrototype;
    public long remaining;
    
    public final Level level;
    public final BlockPos center;
    public final int radius;
    
    // prevents double-consuming the same handler
    public final Set<Object> visited = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
    
    public ItemConsumeContext(
        ServerPlayer player,
        TeamData teamData,
        ItemTask task,
        ItemStack requiredPrototype,
        long remaining,
        int radius)
    {
        this.player = player;
        this.teamData = teamData;
        this.task = task;
        this.requiredPrototype = requiredPrototype;
        this.remaining = remaining;
        this.level = player.level();
        this.center = player.blockPosition();
        this.radius = radius;
    }
}

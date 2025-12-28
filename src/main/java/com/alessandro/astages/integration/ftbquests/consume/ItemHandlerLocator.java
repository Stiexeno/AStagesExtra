package com.alessandro.astages.integration.ftbquests.consume;

import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public interface ItemHandlerLocator
{
    boolean isAvailable();
    void find(Player player, Consumer<HandlerRef> out);
}

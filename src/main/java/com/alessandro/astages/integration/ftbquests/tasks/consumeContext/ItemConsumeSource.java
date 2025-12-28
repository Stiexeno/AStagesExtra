package com.alessandro.astages.integration.ftbquests.tasks.consumeContext;

import com.alessandro.astages.api.nullability.Nullable;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import net.minecraft.world.entity.player.Player;

public interface ItemConsumeSource {
    boolean isAvailable();
    
    long process(
        Player player,
        ItemTask task,
        long remaining,
        boolean simulate,
        @Nullable TeamData teamData
    );
    
    long count(
        Player player,
        ItemTask task,
        long limit
    );
}


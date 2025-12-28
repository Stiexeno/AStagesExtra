package com.alessandro.astages.integration.ftbquests.tasks.consumeContext;

import com.alessandro.astages.api.nullability.Nullable;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.ItemTask;
import net.minecraft.world.entity.player.Player;

public interface ItemConsumeSource {
    boolean isAvailable();
    
    /**
     * @param player     ServerPlayer OR LocalPlayer (never both)
     * @param task       ItemTask to test
     * @param remaining  how many still needed
     * @param simulate   true = count only, false = actually consume
     * @return remaining amount after this source
     */
    long process(
        Player player,
        ItemTask task,
        long remaining,
        boolean simulate,
        @Nullable TeamData teamData
    );
}

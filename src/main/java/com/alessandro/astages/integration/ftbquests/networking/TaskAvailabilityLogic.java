package com.alessandro.astages.integration.ftbquests.networking;

import com.alessandro.astages.integration.ftbquests.consume.ItemConsumeManager;
import com.alessandro.astages.integration.ftbquests.networking.packet.TaskAvailabilitySyncS2CPacket;
import com.alessandro.astages.integration.ftbquests.tasks.SinkItemTask;
import com.alessandro.astages.networking.ANetworking;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.server.level.ServerPlayer;

public final class TaskAvailabilityLogic
{
    public static void handle(ServerPlayer player, long taskId)
    {
        TeamData teamData = TeamData.get(player);
        Task task = teamData.getFile().getTask(taskId);
        
        if (!(task instanceof SinkItemTask sink))
            return;
        
        long max = sink.getMaxProgress();
        long current = teamData.getProgress(sink);
        long remaining = Math.max(0, max - current);
        
        long have = ItemConsumeManager.countAvailable(
            player,
            sink,
            remaining
        );
        
        boolean canSubmit = have >= remaining;
        
        ANetworking.sendToPlayer(
            player,
            new TaskAvailabilitySyncS2CPacket(
                taskId,
                have,
                remaining,
                canSubmit
            )
        );
    }
    
    private TaskAvailabilityLogic()
    {
    }
}

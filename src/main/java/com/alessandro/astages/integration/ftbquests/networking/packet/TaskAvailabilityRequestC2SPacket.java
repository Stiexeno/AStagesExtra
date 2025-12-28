package com.alessandro.astages.integration.ftbquests.networking.packet;

import com.alessandro.astages.api.AResourceLocation;
import com.alessandro.astages.integration.ftbquests.networking.TaskAvailabilityLogic;
import com.alessandro.astages.networking.AStagesPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record TaskAvailabilityRequestC2SPacket(long taskId) implements AStagesPacket
{
    public static final Type<TaskAvailabilityRequestC2SPacket> TYPE =
        new Type<>(AResourceLocation.fromNamespaceAndPath(
            "task_availability_c2s"
        ));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, TaskAvailabilityRequestC2SPacket>
        STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_LONG, TaskAvailabilityRequestC2SPacket::taskId,
        TaskAvailabilityRequestC2SPacket::new
    );
    
    @Override
    public void run(IPayloadContext context)
    {
        if (!(context.player() instanceof ServerPlayer player))
        {
            return;
        }
        
        TaskAvailabilityLogic.handle(player, taskId);
    }
    
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}

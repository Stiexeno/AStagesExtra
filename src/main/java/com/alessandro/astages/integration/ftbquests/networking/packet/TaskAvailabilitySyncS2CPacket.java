package com.alessandro.astages.integration.ftbquests.networking.packet;

import com.alessandro.astages.api.AResourceLocation;
import com.alessandro.astages.networking.AStagesPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record TaskAvailabilitySyncS2CPacket(long taskId, long have, long remaining, boolean canSubmit) implements AStagesPacket
{
    public static final Type<TaskAvailabilitySyncS2CPacket> TYPE =
        new Type<>(AResourceLocation.fromNamespaceAndPath(
            "task_availability_s2c"
        ));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, TaskAvailabilitySyncS2CPacket>
        STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_LONG, TaskAvailabilitySyncS2CPacket::taskId,
        ByteBufCodecs.VAR_LONG, TaskAvailabilitySyncS2CPacket::have,
        ByteBufCodecs.VAR_LONG, TaskAvailabilitySyncS2CPacket::remaining,
        ByteBufCodecs.BOOL, TaskAvailabilitySyncS2CPacket::canSubmit,
        TaskAvailabilitySyncS2CPacket::new
    );
    
    @Override
    public void run(IPayloadContext context)
    {
        TaskAvailabilityCache.put(
            taskId,
            have,
            remaining,
            canSubmit
        );
    }
    
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}


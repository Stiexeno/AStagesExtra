package com.alessandro.astages.integration.ftbquests.networking.packet;

import com.alessandro.astages.api.nullability.Nullable;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public final class TaskAvailabilityCache
{
    private static final Long2ObjectMap<Data> CACHE = new Long2ObjectOpenHashMap<>();
    
    public static void put(long taskId, long have, long remaining, boolean canSubmit)
    {
        CACHE.put(taskId, new Data(have, remaining, canSubmit));
    }
    
    @Nullable
    public static Data get(long taskId)
    {
        return CACHE.get(taskId);
    }
    
    public static void clear()
    {
        CACHE.clear();
    }
    
    public record Data(long have, long remaining, boolean canSubmit)
    {
    }
}

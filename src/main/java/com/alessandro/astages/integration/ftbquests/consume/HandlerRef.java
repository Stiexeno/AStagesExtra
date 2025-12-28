package com.alessandro.astages.integration.ftbquests.consume;

import com.alessandro.astages.api.nullability.Nullable;
import net.neoforged.neoforge.items.IItemHandler;

public record HandlerRef(Object stableKey, IItemHandler handler, @Nullable Runnable onChanged)
{
}


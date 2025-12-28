package com.alessandro.astages.integration.ftbquests.tasks.consumeContext;

import com.alessandro.astages.integration.ftbquests.tasks.consumeContext.sources.PlayerInventorySource;
import com.alessandro.astages.integration.ftbquests.tasks.consumeContext.sources.VanillaBlockItemHandlerSource;

import java.util.ArrayList;
import java.util.List;

public final class ItemConsumeManager
{
    private static final List<ItemConsumeSource> SOURCES = new ArrayList<>();
    
    static
    {
        register(new PlayerInventorySource());
        register(new VanillaBlockItemHandlerSource());
//        // Optional integrations
//        register(new CreateVaultSource());
//        register(new SophisticatedStorageSource());
//        register(new BackpackSource());
    }
    
    public static void register(ItemConsumeSource source)
    {
        SOURCES.add(source);
    }
    
    public static void consumeAll(ItemConsumeContext ctx)
    {
        for (ItemConsumeSource source : SOURCES)
        {
            if (ctx.remaining <= 0)
                return;
            
            if (!source.isAvailable())
                continue;
            
            ctx.remaining = source.consume(ctx);
        }
    }
    
    private ItemConsumeManager()
    {
    }
}

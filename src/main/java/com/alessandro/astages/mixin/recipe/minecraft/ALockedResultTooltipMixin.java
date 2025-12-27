package com.alessandro.astages.mixin.recipe.minecraft;

import com.alessandro.astages.api.holder.AHolder;
import com.alessandro.astages.core.ARestrictionManager;
import com.alessandro.astages.store.Attributes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class ALockedResultTooltipMixin<T extends AbstractContainerMenu> extends Screen
{
    
    // AbstractContainerScreen has this field (shown in javadocs as inherited field) :contentReference[oaicite:1]{index=1}
    @Shadow
    @Nullable
    protected Slot hoveredSlot;
    
    protected ALockedResultTooltipMixin(Component title)
    {
        super(title);
    }
    
    // In 1.21.x this method exists on AbstractContainerScreen :contentReference[oaicite:2]{index=2}
    @Inject(method = "getTooltipFromContainerItem", at = @At("RETURN"), cancellable = true)
    private void astages$addLockedLines(ItemStack stack, CallbackInfoReturnable<List<Component>> cir)
    {
        if (stack == null || stack.isEmpty()) return;
        if (hoveredSlot == null) return;
        
        // Only apply in crafting output slot (vanilla)
        // If some mods use a different result slot class, add extra instanceof checks here.
        if (!(hoveredSlot instanceof ResultSlot)) return;
        
        // Client-only tooltip: player can be null in some contexts, so guard it.
        if (this.minecraft == null || this.minecraft.player == null) return;
        
        var restriction = ARestrictionManager.ITEM_INSTANCE
            .getRestriction(AHolder.player(this.minecraft.player), stack);
        
        if (restriction == null || !restriction.isEnabled(Attributes.BLOCK_FROM_CRAFTING))
        {
            return;
        }
        
        List<Component> original = cir.getReturnValue();
        if (original == null) return;
        
        List<Component> out = new ArrayList<>(original.size() + 2);
        
        // Keep vanilla tooltip unchanged
        out.addAll(original);
        
        // Bottom hint
        out.add(Component.literal("\uD83D\uDD12 Unlock by completing quests").withStyle(ChatFormatting.RED));
        
        cir.setReturnValue(out);
    }
}

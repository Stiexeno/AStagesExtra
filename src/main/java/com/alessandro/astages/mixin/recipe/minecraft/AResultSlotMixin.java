package com.alessandro.astages.mixin.recipe.minecraft;

import com.alessandro.astages.api.holder.AHolder;
import com.alessandro.astages.core.ARestrictionManager;
import com.alessandro.astages.store.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Slot.class)
public abstract class AResultSlotMixin
{
    private boolean astages$isBlocked(Player player)
    {
        if((Object) this instanceof ResultSlot == false)
        {
            return false;
        }
        
        ItemStack stack = ((Slot) (Object) this).getItem();
        
        if (stack.isEmpty())
            return false;
        
        var restriction = ARestrictionManager.ITEM_INSTANCE
            .getRestriction(AHolder.player(player), stack);
        
        return restriction != null
            && restriction.isEnabled(Attributes.BLOCK_FROM_CRAFTING);
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void astages$mayPickup(Player player, CallbackInfoReturnable<Boolean> cir)
    {
        if (astages$isBlocked(player))
        {
            cir.setReturnValue(false);
        }
    }
    
    @Inject(method = "tryRemove", at = @At("HEAD"), cancellable = true)
    private void astages$tryRemove(int count, int flags, Player player, CallbackInfoReturnable<Optional<ItemStack>> cir)
    {
        if (astages$isBlocked(player))
        {
            cir.setReturnValue(Optional.empty());
        }
    }
    
    @Inject(method = "safeTake", at = @At("HEAD"), cancellable = true)
    private void astages$safeTake(int count, int flags, Player player, CallbackInfoReturnable<ItemStack> cir)
    {
        if (astages$isBlocked(player))
        {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(method = "allowModification", at = @At("HEAD"), cancellable = true)
    private void astages$allowModification(Player player, CallbackInfoReturnable<Boolean> cir)
    {
        if (astages$isBlocked(player))
        {
            cir.setReturnValue(false);
        }
    }
    
//    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
//    private void astages$mayPlace(ItemStack stack, CallbackInfoReturnable<Boolean> cir)
//    {
//        if ((Object) this instanceof ResultSlot)
//        {
//            cir.setReturnValue(false);
//        }
//    }

//    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
//    private void astages$remove(int amount, CallbackInfoReturnable<ItemStack> cir)
//    {
//        if ((Object) this instanceof ResultSlot)
//        {
//            cir.setReturnValue(ItemStack.EMPTY);
//        }
//    }
    
    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    private void astages$onTake(Player player, ItemStack stack, CallbackInfo ci)
    {
        if (astages$isBlocked(player))
        {
            ci.cancel();
        }
    }
    
//    @Inject(method = "isHighlightable", at = @At("HEAD"), cancellable = true)
//    private void astages$isHighlightable(CallbackInfoReturnable<Boolean> cir)
//    {
//        if ((Object) this instanceof ResultSlot)
//        {
//            cir.setReturnValue(false);
//        }
//    }
}

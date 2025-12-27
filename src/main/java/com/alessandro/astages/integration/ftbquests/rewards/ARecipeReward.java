package com.alessandro.astages.integration.ftbquests.rewards;

import com.alessandro.astages.AStages;
import com.alessandro.astages.api.AStagesUtils;
import com.alessandro.astages.api.holder.AHolder;
import com.alessandro.astages.integration.ftbquests.AFTBRewards;
import com.alessandro.astages.integration.ftbquests.common.ScaledIcon;
import dev.architectury.hooks.item.ItemStackHooks;
import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.net.NotifyItemRewardMessage;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ARecipeReward extends ItemReward
{
    private static final ResourceLocation BACKGROUND_ICON_LOCATION = ResourceLocation.fromNamespaceAndPath(AStages.MODID, "item/recipe");
    
    public ARecipeReward(long id, Quest quest, ItemStack is)
    {
        super(id, quest, is);
    }
    
    public ARecipeReward(long id, Quest quest, ItemStack is, int count)
    {
        super(id, quest, is, count);
    }
    
    public ARecipeReward(long id, Quest quest)
    {
        super(id, quest, new ItemStack(Items.DIAMOND));
        disableRewardScreenBlur = true;
    }
    
    @Override
    public void claim(ServerPlayer player, boolean notify)
    {
        var tags = getTags();
        
        if (!tags.isEmpty())
        {
            return;
        }
        
        var item = getItem();
        var id = BuiltInRegistries.ITEM.getKey(item.getItem());
        
        AStagesUtils.addStage(AHolder.player(player), id.toString(), true);
        
        if (notify)
        {
            NetworkManager.sendToPlayer(player, new NotifyItemRewardMessage(item, 1, disableRewardScreenBlur));
        }
    }
    
    @Override
    public RewardType getType()
    {
        return AFTBRewards.RECIPE_REWARD;
    }
    
    @Override
    public Icon getAltIcon()
    {
        ItemStack item = getItem();
        int count = getCount();
        
        Icon itemIcon = ItemIcon.getItemIcon(ItemStackHooks.copyWithCount(item, count));
        
        var recipeIcon = Icon.getIcon(BACKGROUND_ICON_LOCATION);
        var scaledBackgroundIcon = new ScaledIcon(recipeIcon, 4);
        
        return scaledBackgroundIcon.combineWith(itemIcon);
    }
    
    @Override
    public boolean automatedClaimPre(BlockEntity blockEntity, List<ItemStack> items, RandomSource random, UUID playerId, @Nullable ServerPlayer player)
    {
        return false;
    }
    
    @Override
    public String getButtonText()
    {
        return "";
    }
}


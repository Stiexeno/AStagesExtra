package com.alessandro.astages.integration.ftbquests;

import com.alessandro.astages.integration.ftbquests.rewards.ARecipeReward;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;

public class AFTBRewards
{
    public static final RewardType RECIPE_REWARD = RewardTypes.register(FTBQuestsAPI.rl("recipe_item"), ARecipeReward::new, () -> Icon.getIcon("minecraft:item/writable_book"));
    
    public static void init()
    {
    }
}

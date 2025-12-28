package com.alessandro.astages.integration.ftbquests;

import com.alessandro.astages.api.nullability.NotNullParamsAndMethodsReturn;
import com.alessandro.astages.integration.ftbquests.rewards.AFTBRewards;
import com.alessandro.astages.integration.ftbquests.rewards.ARecipeReward;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.net.CreateObjectResponseMessage;
import dev.ftb.mods.ftbquests.net.EditObjectResponseMessage;
import dev.ftb.mods.ftbquests.net.SyncTranslationMessageToClient;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.translation.TranslationKey;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@NotNullParamsAndMethodsReturn
public class FTBQuestsUtils
{
    public static void createQuestsFromInventory(Player player, String chapterName)
    {
        for (Slot slot : player.inventoryMenu.slots)
        {
            ItemStack stack = slot.getItem();
            
            if (!stack.isEmpty())
            {
                createQuest(player, chapterName, stack);
            }
        }
    }
    
    public static void createQuestFromHand(Player player, String chapterName)
    {
        ItemStack stack = player.getMainHandItem();
        
        if (!stack.isEmpty())
        {
            createQuest(player, chapterName, stack);
        }
    }
    
    public static String getRestrictionsFromQuests(Player player)
    {
        TeamData teamData = TeamData.get(player);
        
        BaseQuestFile questFile = teamData.getFile();
        
        if (questFile == null)
            return "Quest file is null!";
        
        StringBuilder out = new StringBuilder();
        
        for (ChapterGroup group : questFile.getChapterGroups())
            for (Chapter chapter : group.getChapters())
                for (Quest quest : chapter.getQuests())
                {
                    quest.getRewards().forEach(reward ->
                    {
                        if (!(reward instanceof ARecipeReward recipeReward))
                        {
                            return;
                        }
                        
                        ItemStack stack = recipeReward.getItem(); // <-- REQUIRED
                        
                        if (stack == null || stack.isEmpty())
                            return;
                        
                        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
                        
                        String itemPath = itemId.getNamespace() + "/" + itemId.getPath();
                        
                        out.append("AStages.addRestrictionForItem(\"")
                            .append(itemPath).append("\", \"")
                            .append(itemId).append("\", \"")
                            .append(itemId).append("\")\n")
                            .append("    .setBlockFromCrafting(true)\n")
                            .append("    .setHideInJEI(true);\n\n");
                    });
                }
        
        return out.toString();
    }
    
    private static void createQuest(Player player, String chapterName, ItemStack stack)
    {
        var chapter = findChapter(player, chapterName);
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        
        Quest quest = new Quest(file.newID(), chapter);
        quest.setRawTitle("Unlock " + stack.getHoverName().getString());
        quest.setRawIcon(stack);
        
        var range = 3;
        
        var randX = (int) (Math.random() * range * 2) - range;
        var randY = (int) (Math.random() * range * 2) - range;
        quest.setX(randX);
        quest.setY(randY);
        
        quest.onCreated();
        NetworkHelper.sendToAll(file.server, CreateObjectResponseMessage.create(quest, null));
        
        ARecipeReward reward = new ARecipeReward(
            file.newID(),
            quest,
            stack,
            stack.getCount()
        );
        
        HolderLookup.Provider provider = player.registryAccess();
        
        var tag = new CompoundTag();
        tag.put("item", stack.save(provider));
        reward.readData(tag, provider);
        reward.onCreated();
        
        var extra = new CompoundTag();
        file.getTranslationManager().processInitialTranslation(extra, reward);
        extra.putString("type", AFTBRewards.RECIPE_REWARD.getTypeForNBT());
        NetworkHelper.sendToAll(file.server, CreateObjectResponseMessage.create(reward, extra, player.getUUID()));
        
        file.refreshIDMap();
        file.clearCachedData();
        file.markDirty();
        file.saveNow();
        
        NetworkHelper.sendToAll(file.server, new EditObjectResponseMessage(quest));
        NetworkHelper.sendToAll(file.server, SyncTranslationMessageToClient.create(quest, file.getLocale(), TranslationKey.TITLE, quest.getRawTitle()));
    }
    
    private static @Nullable Chapter findChapter(Player player, String chapterName)
    {
        TeamData teamData = TeamData.get(player);
        
        BaseQuestFile questFile = teamData.getFile();
        
        for (ChapterGroup group : questFile.getChapterGroups())
        {
            for (Chapter chapter : group.getChapters())
            {
                if (chapter.getTitle().getString().equals(chapterName))
                {
                    return chapter;
                }
            }
        }
        
        return null;
    }
}

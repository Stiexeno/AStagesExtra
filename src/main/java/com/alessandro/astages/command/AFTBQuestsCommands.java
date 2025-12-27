package com.alessandro.astages.command;

import com.alessandro.astages.api.nullability.NotNullParams;
import com.alessandro.astages.integration.ftbquests.FTBQuestsUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

@NotNullParams
public class AFTBQuestsCommands
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(Commands.literal("ftbquests").requires(c -> c.hasPermission(2))
            .then(Commands.literal("astages")
                .then(Commands.literal("create").then(Commands.literal("hand").then(Commands.argument("chapter", StringArgumentType.string())
                    .executes(context -> addAStageQuestsFromHandCommand(context, StringArgumentType.getString(context, "chapter")))
                )))
                .then(Commands.literal("create").then(Commands.literal("inventory").then(Commands.argument("chapter", StringArgumentType.string())
                    .executes(context -> addAStageQuestsFromInventoryCommand(context, StringArgumentType.getString(context, "chapter")))
                ))))
        );
    }
    
    private static int addAStageQuestsFromHandCommand(CommandContext<CommandSourceStack> context, String chapter)
    {
        var player = context.getSource().getPlayer();
        
        if (player == null)
            return 0;
        
        FTBQuestsUtils.createQuestFromHand(player, chapter);
        
        context.getSource().sendSuccess(() -> Component.literal("Quest created from item in hand!").withStyle(net.minecraft.ChatFormatting.GREEN), false);
        return 1;
    }
    
    private static int addAStageQuestsFromInventoryCommand(CommandContext<CommandSourceStack> context, String chapter)
    {
        var player = context.getSource().getPlayer();
        
        if (player == null)
            return 0;
        
        FTBQuestsUtils.createQuestsFromInventory(player, chapter);
        context.getSource().sendSuccess(() -> Component.literal("Quests created from inventory in hand!").withStyle(net.minecraft.ChatFormatting.GREEN), false);
        return 1;
    }
}

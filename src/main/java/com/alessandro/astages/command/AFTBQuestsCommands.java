package com.alessandro.astages.command;

import com.alessandro.astages.api.nullability.NotNullParams;
import com.alessandro.astages.integration.ftbquests.FTBQuestsUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
                )))
                .then(Commands.literal("dump").then(Commands.literal("restrictions")
                    .executes(AFTBQuestsCommands::dumpRestrictionsCommand)
                )))
        );
    }
    
    private static int dumpRestrictionsCommand(CommandContext<CommandSourceStack> context)
    {
        var player = context.getSource().getPlayer();
        
        if (player == null)
            return 0;
        
        Path file = Paths.get("astages", "restrictions.txt");
        
        try
        {
            var text = FTBQuestsUtils.getRestrictionsFromQuests(player);
            Files.createDirectories(file.getParent());
            Files.writeString(file, text, StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        var component = Component.literal("Restrictions exported: ")
            .append(
                Component.literal("[Open file]")
                    .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(
                            ClickEvent.Action.OPEN_FILE,
                            file.toAbsolutePath().toString()
                        ))
                    )
            );
        
        context.getSource().sendSuccess(() -> component, false);
        return 1;
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

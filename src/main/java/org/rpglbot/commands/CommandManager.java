package org.rpglbot.commands;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.rpgl.uuidtable.UUIDTable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommandManager extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getChannel().getName().equals("rpgl-stuff")) {
            switch(event.getFullCommandName()) {
                case "test" -> slashTest(event);
                case "new" -> slashNew(event);
                case "save" -> slashSave(event);
                case "load" -> slashLoad(event);
            }
        }
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("test", "a test command"));
        commandData.add(Commands.slash("new", "start a new adventure"));
        commandData.add(Commands.slash("save", "save your adventure for later"));
        commandData.add(Commands.slash("load", "load your previous adventure"));
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }

    @Override
    public void onReady(ReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("test", "a test command"));
        commandData.add(Commands.slash("new", "start a new adventure"));
        commandData.add(Commands.slash("save", "save your adventure for later"));
        commandData.add(Commands.slash("load", "load your previous adventure"));
        event.getJDA().updateCommands().addCommands(commandData).queue();
    }

    private static void slashTest(SlashCommandInteractionEvent event) {
        event.reply("Hello world!").queue();
    }

    private static void slashNew(SlashCommandInteractionEvent event) {
        event.reply("Starting new adventure!").queue();
        UUIDTable.clear();
    }

    private static void slashSave(SlashCommandInteractionEvent event) {
        String username = event.getUser().getName();
        try {
            UUIDTable.saveToDirectory(new File("saves" + File.separator + username));
            event.reply("Saved as " + username).queue();
        } catch (IOException e) {
            System.out.println("ERROR: failed to save as " + username);
            System.out.println(e.getMessage());
            event.reply("Failed to save.").queue();
        }
    }

    private static void slashLoad(SlashCommandInteractionEvent event) {
        String username = event.getUser().getName();
        try {
            UUIDTable.clear();
            UUIDTable.loadFromDirectory(new File("saves" + File.separator + username));
            event.reply("Loaded as " + username).queue();
        } catch (IOException e) {
            System.out.println("ERROR: failed to load as " + username);
            System.out.println(e.getMessage());
            event.reply("Failed to load. Did you save an adventure before?").queue();
        }
    }
}

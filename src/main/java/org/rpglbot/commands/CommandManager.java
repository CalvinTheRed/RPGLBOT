package org.rpglbot.commands;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.rpgl.core.RPGLFactory;
import org.rpgl.core.RPGLObject;
import org.rpgl.uuidtable.UUIDTable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommandManager extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getChannel().getName().equals("rpgl-stuff")) {
            String command = event.getFullCommandName();
            switch(command) {
                case "new" -> slashNew(event);
                case "save" -> slashSave(event);
                case "load" -> slashLoad(event);
                case "spawn" -> {
                    try {
                        slashSpawn(event);
                    } catch (NullPointerException e) {
                        event.reply("SPAWN requires an id").queue();
                    }
                }
            }
        }
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("new", "start a new adventure"));
        commandData.add(Commands.slash("save", "save your adventure for later"));
        commandData.add(Commands.slash("load", "load your previous adventure"));
        commandData.add(Commands.slash("spawn", "spawns an RPGLObject")
                .addOption(OptionType.STRING, "id", "the id of the RPGLObject to be spawned", true, true));
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }

    @Override
    public void onReady(ReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>();
        commandData.add(Commands.slash("new", "start a new adventure"));
        commandData.add(Commands.slash("save", "save your adventure for later"));
        commandData.add(Commands.slash("load", "load your previous adventure"));
        commandData.add(Commands.slash("spawn", "spawns an RPGLObject")
                .addOption(OptionType.STRING, "id", "the id of the RPGLObject to be spawned", true, true));
        event.getJDA().updateCommands().addCommands(commandData).queue();
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

    @SuppressWarnings("ConstantConditions")
    private static void slashSpawn(SlashCommandInteractionEvent event) {
        RPGLObject object = RPGLFactory.newObject(event.getOption("id").getAsString());
        event.reply("Spawned " + object.getName()).queue();
    }
}

package org.rpglbot.commands;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.rpgl.core.RPGLContext;
import org.rpgl.core.RPGLEffectTemplate;
import org.rpgl.core.RPGLEvent;
import org.rpgl.core.RPGLEventTemplate;
import org.rpgl.core.RPGLFactory;
import org.rpgl.core.RPGLItemTemplate;
import org.rpgl.core.RPGLObject;
import org.rpgl.core.RPGLObjectTemplate;
import org.rpgl.core.RPGLResource;
import org.rpgl.core.RPGLResourceTemplate;
import org.rpgl.datapack.DatapackLoader;
import org.rpgl.uuidtable.UUIDTable;
import org.rpglbot.CustomContext;
import org.rpglbot.RPGLClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Stack;
import java.util.UUID;

public class CommandManager extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getChannel().getName().equals("rpgl-stuff")) {
            String command = event.getFullCommandName();
            switch(command) {
                case "as" -> {
                    try {
                        slashAs(event);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        event.reply("Something went wrong!").queue();
                    }
                }
                case "fight" -> slashFight(event);
                case "help" -> {
                    try {
                        slashHelp(event);
                    } catch (NullPointerException e) {
                        event.reply("HELP requires an id").queue();
                    }
                }
                case "list" -> slashList(event);
                case "load" -> slashLoad(event);
                case "new" -> slashNew(event);
                case "rename" -> slashRename(event);
                case "save" -> slashSave(event);
                case "spawn" -> {
                    try {
                        slashSpawn(event);
                    } catch (NullPointerException e) {
                        event.reply("SPAWN requires an id").queue();
                    }
                }
                case "turn" -> {
                    try {
                        slashTurn(event);
                    } catch (Exception e) {
                        event.reply("TURN requires an operation (`end` | `who`)").queue();
                    }
                }
            }
        }
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>() {{

            this.add(Commands.slash("as", "cause one of your RPGLObjects to do something")
                    .addOption(OptionType.STRING, "my_object", "the object you want to cause to act", true, true)
                    .addOption(OptionType.STRING, "my_event", "the act you want your object to take", true, true)
                    .addOption(OptionType.STRING, "targets", "an object you want to target with your action", true, true)
                    .addOption(OptionType.STRING, "my_resources", "the resources you want to spend to act", true, true));

            this.add(Commands.slash("fight", "roll everyone's initiative"));

            this.add(Commands.slash("help", "displays template data for a specified DatapackContent")
                    .addOption(OptionType.STRING, "data_type", "the type of the DatapackContent to be queried", true, true)
                    .addOption(OptionType.STRING, "id", "the id of the DatapackContent to be queried", true, true));

            this.add(Commands.slash("list", "list objects in a specified scope")
                    .addOption(OptionType.STRING, "scope", "the scope of which objects to list", true, true));

            this.add(Commands.slash("load", "load your previous adventure")
                    .addOption(OptionType.STRING, "save_name", "the name of the save file to load", true, true));

            this.add(Commands.slash("new", "start a new adventure")
                    .addOption(OptionType.STRING, "save_name", "the name of the new save file"));

            this.add(Commands.slash("rename", "rename one of your objects")
                    .addOption(OptionType.STRING, "my_object", "the object you want to rename", true, true)
                    .addOption(OptionType.STRING, "new_name", "the new name for the object", true));

            this.add(Commands.slash("save", "save your adventure for later")
                    .addOption(OptionType.STRING, "save_name", "the target save file"));

            this.add(Commands.slash("spawn", "spawns an RPGLObject")
                    .addOption(OptionType.STRING, "id", "the id of the RPGLObject to be spawned", true, true));

            this.add(Commands.slash("turn", "interact with the turn order")
                    .addOption(OptionType.STRING, "operation", "end | who", true, true));

        }};
        event.getGuild().updateCommands().addCommands(commandData).queue();
    }

    @Override
    public void onReady(ReadyEvent event) {
        List<CommandData> commandData = new ArrayList<>() {{

            this.add(Commands.slash("as", "cause one of your RPGLObjects to do something")
                    .addOption(OptionType.STRING, "my_object", "the object you want to cause to act", true, true)
                    .addOption(OptionType.STRING, "my_event", "the act you want your object to take", true, true)
                    .addOption(OptionType.STRING, "targets", "an object you want to target with your action", true, true)
                    .addOption(OptionType.STRING, "my_resources", "the resources you want to spend to act", true, true));

            this.add(Commands.slash("fight", "roll everyone's initiative"));

            this.add(Commands.slash("help", "displays template data for a specified DatapackContent")
                    .addOption(OptionType.STRING, "data_type", "the type of the DatapackContent to be queried", true, true)
                    .addOption(OptionType.STRING, "id", "the id of the DatapackContent to be queried", true, true));

            this.add(Commands.slash("list", "list objects in a specified scope")
                    .addOption(OptionType.STRING, "scope", "the scope of which objects to list", true, true));

            this.add(Commands.slash("load", "load your previous adventure")
                    .addOption(OptionType.STRING, "save_name", "the name of the save file to load", true, true));

            this.add(Commands.slash("new", "start a new adventure")
                    .addOption(OptionType.STRING, "save_name", "the name of the new save file"));

            this.add(Commands.slash("rename", "rename one of your objects")
                    .addOption(OptionType.STRING, "my_object", "the object you want to rename", true, true)
                    .addOption(OptionType.STRING, "new_name", "the new name for the object", true));

            this.add(Commands.slash("save", "save your adventure for later")
                    .addOption(OptionType.STRING, "save_name", "the target save file"));

            this.add(Commands.slash("spawn", "spawns an RPGLObject")
                    .addOption(OptionType.STRING, "id", "the id of the RPGLObject to be spawned", true, true));

            this.add(Commands.slash("turn", "interact with the turn order")
                    .addOption(OptionType.STRING, "operation", "end | who", true, true));

        }};
        event.getJDA().updateCommands().addCommands(commandData).queue();
    }

    private static void slashAs(SlashCommandInteractionEvent event) throws Exception {
        RPGLObject source = UUIDTable.getObject(Objects.requireNonNull(event.getOption("my_object")).getAsString());
        RPGLEvent rpglEvent = RPGLFactory.newEvent(Objects.requireNonNull(event.getOption("my_event")).getAsString());

        String[] targetUuids = Objects.requireNonNull(event.getOption("targets")).getAsString().split(",");
        RPGLObject[] targets = new RPGLObject[targetUuids.length];
        for (int i = 0; i < targets.length; i++) {
            targets[i] = UUIDTable.getObject(targetUuids[i]);
        }

        String[] resourceUuids = Objects.requireNonNull(event.getOption("my_resources")).getAsString().split(",");
        RPGLResource[] resources = new RPGLResource[resourceUuids.length];
        for (int i = 0; i < resources.length; i++) {
            resources[i] = UUIDTable.getResource(resourceUuids[i]);
        }

        CustomContext context = (CustomContext) RPGLClient.CONTEXT;
        context.clearMessages();
        source.invokeEvent(targets, rpglEvent, List.of(resources), context);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(source.getName())
                .append(" uses ")
                .append(rpglEvent.getName())
                .append(" on ");
        for (int i = 0; i < targets.length; i++) {
            stringBuilder.append(targets[i].getName());
            if (i < targets.length - 1) {
                stringBuilder.append(", ");
            } else {
                stringBuilder.append("!");
            }
        }

        Stack<String> messages = context.getMessages();
        while (!messages.isEmpty()) {
            stringBuilder.append('\n').append(messages.pop());
        }
        event.reply(stringBuilder.toString()).queue();
    }

    private static void slashRename(SlashCommandInteractionEvent event) {
        RPGLObject object = UUIDTable.getObject(Objects.requireNonNull(event.getOption("my_object")).getAsString());
        String newName = Objects.requireNonNull(event.getOption("new_name")).getAsString();
        String oldName = object.getName();
        object.setName(newName);
        event.reply(String.format("Renamed %s to %s!", oldName, newName)).queue();
    }

    private static void slashNew(SlashCommandInteractionEvent event) {
        event.reply("Starting new adventure!").queue();
        UUIDTable.clear();
        RPGLClient.CONTEXT.clear();
        OptionMapping saveNameOption = event.getOption("save_name");
        String saveName = saveNameOption == null ? null : saveNameOption.getAsString();
        if (saveName == null) {
            RPGLClient.setCurrentSaveName(RPGLClient.getDefaultSaveName());
        } else {
            RPGLClient.setCurrentSaveName(saveName);
        }
    }

    private static void slashSave(SlashCommandInteractionEvent event) {
        OptionMapping saveNameOption = event.getOption("save_name");
        String saveName = saveNameOption == null ? RPGLClient.getCurrentSaveName() : saveNameOption.getAsString();
        try {
            UUIDTable.saveToDirectory(new File("saves" + File.separator + saveName));
            event.reply("Saved as " + saveName).queue();
        } catch (IOException e) {
            event.reply("ERROR: failed to save as " + saveName).queue();
            System.out.println("ERROR: failed to save as " + saveName);
            System.out.println(e.getMessage());
        }
    }

    private static void slashList(SlashCommandInteractionEvent event) {
        String scope = Objects.requireNonNull(event.getOption("scope")).getAsString();
        StringBuilder replyBuilder = new StringBuilder();
        List<RPGLObject> objects = List.of();
        switch (scope) {
            case "all" -> objects = List.of();
            case "mine" -> objects = UUIDTable.getObjectsByUserId(event.getUser().getName());
        }
        for (RPGLObject object : objects) {
            replyBuilder.append("[").append(object.getUuid().split("-")[0]).append("] ").append(object.getName()).append("\n");
        }
        String reply = replyBuilder.toString();
        if (Objects.equals("", reply)) {
            event.reply("no objects to list").queue();
        } else {
            event.reply(reply).queue();
        }
    }

    private static void slashLoad(SlashCommandInteractionEvent event) {
        String saveName = Objects.requireNonNull(event.getOption("save_name")).getAsString();
        try {
            UUIDTable.clear();
            UUIDTable.loadFromDirectory(new File("saves" + File.separator + saveName));
            for (RPGLObject object : UUIDTable.getObjects()) {
                RPGLClient.CONTEXT.add(object);
            }
            event.reply("Loaded as " + saveName).queue();
        } catch (IOException e) {
            event.reply("ERROR: failed to load as " + saveName).queue();
            System.out.println("ERROR: failed to load as " + saveName);
            System.out.println(e.getMessage());
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static void slashSpawn(SlashCommandInteractionEvent event) {
        RPGLObject object = RPGLFactory.newObject(event.getOption("id").getAsString(), event.getUser().getName());
        RPGLClient.CONTEXT.add(object);
        event.reply("Spawned " + object.getName()).queue();
    }

    private static void slashHelp(SlashCommandInteractionEvent event) {
        String dataType = Objects.requireNonNull(event.getOption("data_type")).getAsString();
        String id = Objects.requireNonNull(event.getOption("id")).getAsString();
        String[] splitId = id.split(":");
        switch(dataType) {
            case "effect": {
                RPGLEffectTemplate template = DatapackLoader.DATAPACKS.get(splitId[0]).getEffectTemplate(splitId[1]);
                event.reply("HELP for effect " + id + ":\n\n" + template.prettyPrint()).queue();
            }
            case "event": {
                RPGLEventTemplate template = DatapackLoader.DATAPACKS.get(splitId[0]).getEventTemplate(splitId[1]);
                event.reply("HELP for event " + " " + id + ":\n\n" + template.prettyPrint()).queue();
            }
            case "item": {
                RPGLItemTemplate template = DatapackLoader.DATAPACKS.get(splitId[0]).getItemTemplate(splitId[1]);
                event.reply("HELP for item " + " " + id + ":\n\n" + template.prettyPrint()).queue();
            }
            case "object": {
                RPGLObjectTemplate template = DatapackLoader.DATAPACKS.get(splitId[0]).getObjectTemplate(splitId[1]);
                event.reply("HELP for object " + " " + id + ":\n\n" + template.prettyPrint()).queue();
            }
            case "resource": {
                RPGLResourceTemplate template = DatapackLoader.DATAPACKS.get(splitId[0]).getResourceTemplate(splitId[1]);
                event.reply("HELP for resource " + " " + id + ":\n\n" + template.prettyPrint()).queue();
            }
        }
    }

    private static void slashFight(SlashCommandInteractionEvent event) {
        RPGLClient.clearTurnOrder();
        RPGLContext context = RPGLClient.CONTEXT;
        List<RPGLObject> objects = context.getContextObjects();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Rolling for initiative!\n");
        for (RPGLObject object : objects) {
            try {
                stringBuilder.append(object.getName()).append(": ");
                double initiative = object.abilityCheck("dex", null, context);
                stringBuilder.append((int) initiative).append('\n');
                initiative += ((double) object.getAbilityScoreFromAbilityName("dex", context) + new Random().nextDouble()) / 100.0;
                RPGLClient.assignInitiative(initiative, object);
            } catch (Exception e) {
                stringBuilder.append("ERROR\n");
                System.out.println(e.getMessage());
            }
        }

        RPGLObject firstTurn = RPGLClient.currentTurnObject();
        stringBuilder.append("First up is ").append(firstTurn.getName()).append('!');
        event.reply(stringBuilder.toString()).queue();
    }

    private static void slashTurn(SlashCommandInteractionEvent event) throws Exception {
        String operation = Objects.requireNonNull(event.getOption("operation")).getAsString();

        switch(operation) {
            case "end" -> endTurn(event);
            case "who" -> whoseTurn(event);
        }
    }

    private static void endTurn(SlashCommandInteractionEvent event) throws Exception {
        RPGLObject currentObject = RPGLClient.currentTurnObject();
        if (currentObject != null) {
            currentObject.invokeInfoSubevent(RPGLClient.CONTEXT, "end_turn");

            RPGLObject nextObject = RPGLClient.nextTurnObject();
            nextObject.invokeInfoSubevent(RPGLClient.CONTEXT, "start_turn");

            event.reply(currentObject.getName() + " ends its turn.\n\nNext up is " + nextObject.getName() + '!').queue();
        }
        event.reply("You are not in combat.").queue();
    }

    private static void whoseTurn(SlashCommandInteractionEvent event) {
        RPGLObject currentTurnObject = RPGLClient.currentTurnObject();
        String reply;
        if (currentTurnObject != null) {
            reply = String.format("It is %s's turn! (controlled by %s)",
                    currentTurnObject.getName(),
                    currentTurnObject.getUserId()
            );
        } else {
            reply = "You are not in combat.";
        }
        event.reply(reply).queue();
    }
}

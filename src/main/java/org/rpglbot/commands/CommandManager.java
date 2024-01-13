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
import org.rpgl.core.RPGLEffect;
import org.rpgl.core.RPGLEffectTemplate;
import org.rpgl.core.RPGLEvent;
import org.rpgl.core.RPGLEventTemplate;
import org.rpgl.core.RPGLFactory;
import org.rpgl.core.RPGLItem;
import org.rpgl.core.RPGLItemTemplate;
import org.rpgl.core.RPGLObject;
import org.rpgl.core.RPGLObjectTemplate;
import org.rpgl.core.RPGLResource;
import org.rpgl.core.RPGLResourceTemplate;
import org.rpgl.datapack.DatapackContent;
import org.rpgl.datapack.DatapackLoader;
import org.rpgl.json.JsonArray;
import org.rpgl.json.JsonObject;
import org.rpgl.uuidtable.UUIDTable;
import org.rpglbot.CustomContext;
import org.rpglbot.RPGLClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Stack;

public class CommandManager extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        if (e.getChannel().getName().equals("rpgl-stuff")) {
            String command = e.getFullCommandName();
            try {
                switch(command) {
                    case "action" -> slashAction(e);
                    case "equip" -> slashEquip(e);
                    case "fight" -> slashFight(e);
                    case "give" -> slashGive(e);
                    case "help" -> slashHelp(e);
                    case "inspect" -> slashInspect(e);
                    case "invoke" -> slashInvoke(e);
                    case "list" -> slashList(e);
                    case "load" -> slashLoad(e);
                    case "new" -> slashNew(e);
                    case "rename" -> slashRename(e);
                    case "rest" -> slashRest(e);
                    case "save" -> slashSave(e);
                    case "spawn" -> slashSpawn(e);
                    case "spend" -> slashSpend(e);
                    case "target" -> slashTarget(e);
                    case "turn" -> slashTurn(e);
                    case "unequip" -> slashUnequip(e);
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                e.reply("Something went wrong!").queue();
            }
        }
    }

    @Override
    public void onGuildReady(GuildReadyEvent e) {
        List<CommandData> commandData = new ArrayList<>() {{

            this.add(Commands.slash("action", "choose an action to take")
                    .addOption(OptionType.STRING, "event", "an action", true, true));

            this.add(Commands.slash("target", "choose a target for your action (can be used more than once)")
                    .addOption(OptionType.STRING, "target", "an object to target", true, true));

            this.add(Commands.slash("spend", "choose a resource to fuel your action (can be used more than once)")
                    .addOption(OptionType.STRING, "resource", "a resource to spend", true, true));

            this.add(Commands.slash("invoke", "invoke an action you have prepared"));

            this.add(Commands.slash("fight", "roll everyone's initiative"));

            this.add(Commands.slash("help", "displays template data for a specified DatapackContent")
                    .addOption(OptionType.STRING, "data_type", "the type of the DatapackContent to be queried", true, true)
                    .addOption(OptionType.STRING, "id", "the id of the DatapackContent to be queried", true, true));

            this.add(Commands.slash("inspect", "inspect an object in the game")
                    .addOption(OptionType.STRING, "target", "an object to inspect", true, true));

            this.add(Commands.slash("equip", "equip an item")
                    .addOption(OptionType.STRING, "my_object", "the object you want to equip an item", true, true)
                    .addOption(OptionType.STRING, "item", "which item to equip", true, true)
                    .addOption(OptionType.STRING, "slot", "which inventory slot to put the item", true, true));

            this.add(Commands.slash("give", "give an item to an object")
                    .addOption(OptionType.STRING, "target", "the object getting the item", true, true)
                    .addOption(OptionType.STRING, "item", "the item to give", true, true));

            this.add(Commands.slash("list", "list objects in a specified scope")
                    .addOption(OptionType.STRING, "scope", "the scope of which objects to list", true, true));

            this.add(Commands.slash("load", "load your previous adventure")
                    .addOption(OptionType.STRING, "save_name", "the name of the save file to load", true, true));

            this.add(Commands.slash("new", "start a new adventure")
                    .addOption(OptionType.STRING, "save_name", "the name of the new save file"));

            this.add(Commands.slash("rename", "rename one of your objects")
                    .addOption(OptionType.STRING, "my_object", "the object you want to rename", true, true)
                    .addOption(OptionType.STRING, "new_name", "the new name for the object", true));

            this.add(Commands.slash("rest", "take a rest")
                    .addOption(OptionType.STRING, "duration", "the duration of your rest", true, true));

            this.add(Commands.slash("save", "save your adventure for later")
                    .addOption(OptionType.STRING, "save_name", "the target save file", true, true));

            this.add(Commands.slash("spawn", "spawns an RPGLObject")
                    .addOption(OptionType.STRING, "id", "the id of the RPGLObject to be spawned", true, true)
                    .addOption(OptionType.STRING, "name", "the name of the object"));

            this.add(Commands.slash("turn", "interact with the turn order")
                    .addOption(OptionType.STRING, "operation", "end | who", true, true));

            this.add(Commands.slash("unequip", "equip an item")
                    .addOption(OptionType.STRING, "my_object", "the object you want to unequip an item", true, true)
                    .addOption(OptionType.STRING, "slot", "which inventory slot to unequip", true, true));

        }};
        e.getGuild().updateCommands().addCommands(commandData).queue();
    }

    @Override
    public void onReady(ReadyEvent e) {
        List<CommandData> commandData = new ArrayList<>() {{

            this.add(Commands.slash("action", "choose an action to take")
                    .addOption(OptionType.STRING, "event", "an action", true, true));

            this.add(Commands.slash("target", "choose a target for your action (can be used more than once)")
                    .addOption(OptionType.STRING, "target", "an object to target", true, true));

            this.add(Commands.slash("spend", "choose a resource to fuel your action (can be used more than once)")
                    .addOption(OptionType.STRING, "resource", "a resource to spend", true, true));

            this.add(Commands.slash("invoke", "invoke an action you have prepared"));

            this.add(Commands.slash("fight", "roll everyone's initiative"));

            this.add(Commands.slash("help", "displays template data for a specified DatapackContent (WIP)")
                    .addOption(OptionType.STRING, "data_type", "the type of the DatapackContent to be queried", true, true)
                    .addOption(OptionType.STRING, "id", "the id of the DatapackContent to be queried", true, true));

            this.add(Commands.slash("inspect", "inspect an object in the game")
                    .addOption(OptionType.STRING, "target", "an object to inspect", true, true));

            this.add(Commands.slash("equip", "equip an item")
                    .addOption(OptionType.STRING, "my_object", "the object you want to equip an item", true, true)
                    .addOption(OptionType.STRING, "item", "which item to equip", true, true)
                    .addOption(OptionType.STRING, "slot", "which inventory slot to put the item", true, true));

            this.add(Commands.slash("give", "give an item to an object")
                    .addOption(OptionType.STRING, "target", "the object getting the item", true, true)
                    .addOption(OptionType.STRING, "item", "the item to give", true, true));

            this.add(Commands.slash("list", "list objects in a specified scope")
                    .addOption(OptionType.STRING, "scope", "the scope of which objects to list", true, true));

            this.add(Commands.slash("load", "load your previous adventure")
                    .addOption(OptionType.STRING, "save_name", "the name of the save file to load", true, true));

            this.add(Commands.slash("new", "start a new adventure")
                    .addOption(OptionType.STRING, "save_name", "the name of the new save file"));

            this.add(Commands.slash("rename", "rename one of your objects")
                    .addOption(OptionType.STRING, "my_object", "the object you want to rename", true, true)
                    .addOption(OptionType.STRING, "new_name", "the new name for the object", true));

            this.add(Commands.slash("rest", "take a rest")
                    .addOption(OptionType.STRING, "duration", "the duration of your rest", true, true));

            this.add(Commands.slash("save", "save your adventure for later")
                    .addOption(OptionType.STRING, "save_name", "the target save file", true, true));

            this.add(Commands.slash("spawn", "spawns an RPGLObject")
                    .addOption(OptionType.STRING, "id", "the id of the RPGLObject to be spawned", true, true)
                    .addOption(OptionType.STRING, "name", "the name of the object"));

            this.add(Commands.slash("turn", "interact with the turn order")
                    .addOption(OptionType.STRING, "operation", "end | who", true, true));

            this.add(Commands.slash("unequip", "equip an item")
                    .addOption(OptionType.STRING, "my_object", "the object you want to unequip an item", true, true)
                    .addOption(OptionType.STRING, "slot", "which inventory slot to unequip", true, true));

        }};
        e.getJDA().updateCommands().addCommands(commandData).queue();
    }

    private static boolean isUsersTurn(SlashCommandInteractionEvent e) {
        RPGLObject currentTurnObject = RPGLClient.currentTurnObject();
        return currentTurnObject != null && Objects.equals(currentTurnObject.getUserId(), e.getUser().getName());
    }

    private static void slashAction(SlashCommandInteractionEvent e) throws Exception {
        if (isUsersTurn(e)) {
            List<RPGLEvent> events = RPGLClient.currentTurnObject().getEventObjects(RPGLClient.CONTEXT);
            for (RPGLEvent event : events) {
                if (Objects.equals(event.getId(), Objects.requireNonNull(e.getOption("event")).getAsString())) {
                    RPGLClient.setEvent(event);
                    System.out.println(event.prettyPrint());
                    break;
                }
            }
            e.reply("Action selected: `" + RPGLClient.getEvent().getName() + '`').setEphemeral(true).queue();
        } else {
            e.reply("You cannot use this command unless it is your turn! Did you remember to use `/fight` first?")
                    .setEphemeral(true)
                    .queue();
        }
    }

    private static void slashTarget(SlashCommandInteractionEvent e) {
        if (isUsersTurn(e)) {
            RPGLEvent event = RPGLClient.getEvent();
            if (event != null) {
                RPGLObject target = UUIDTable.getObject(Objects.requireNonNull(e.getOption("target")).getAsString());
                RPGLClient.addTarget(target);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Added target: `").append(target.getName()).append("`\n\nTargets:");
                for (RPGLObject selectedTarget : RPGLClient.getTargets()) {
                    stringBuilder.append("\n`").append(selectedTarget.getName()).append('`');
                }
                e.reply(stringBuilder.toString()).setEphemeral(true).queue();
            } else {
                e.reply("You cannot use this command until you complete `/action` first!").setEphemeral(true).queue();
            }
        } else {
            e.reply("You cannot use this command unless it is your turn!").setEphemeral(true).queue();
        }
    }

    private static void slashSpend(SlashCommandInteractionEvent e) {
        if (isUsersTurn(e)) {
            RPGLEvent event = RPGLClient.getEvent();
            if (event != null) {
                RPGLResource resource = UUIDTable.getResource(Objects.requireNonNull(e.getOption("resource")).getAsString());
                RPGLClient.addResource(resource);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Added resource: `").append(resource.getName()).append("`\n\nResources:");
                List<RPGLResource> resources = RPGLClient.getResources();
                for (RPGLResource selectedResource : resources) {
                    stringBuilder.append("\n`").append(selectedResource.getName()).append('`');
                }
                JsonArray cost = event.getCost();
                if (cost.size() > resources.size()) {
                    stringBuilder.append("\n\nResources still needed:");
                    for (int i = resources.size(); i < cost.size(); i++) {
                        JsonArray resourceTags = cost.getJsonObject(i).getJsonArray("resource_tags");
                        stringBuilder.append("\n`").append(resourceTags.toString()).append('`');
                    }
                }
                e.reply(stringBuilder.toString()).setEphemeral(true).queue();
            } else {
                e.reply("You cannot use this command until you complete `/action` first!").setEphemeral(true).queue();
            }
        } else {
            e.reply("You cannot use this command unless it is your turn!").setEphemeral(true).queue();
        }
    }

    private static void slashInvoke(SlashCommandInteractionEvent e) throws Exception {
        RPGLObject object = RPGLClient.currentTurnObject();
        RPGLEvent event = RPGLClient.getEvent();
        List<RPGLObject> targets = RPGLClient.getTargets();
        List<RPGLResource> resources = RPGLClient.getResources();

        if (object != null && event != null && !targets.isEmpty() && !resources.isEmpty() && event.getCost().size() == resources.size()) {
            RPGLObject[] targetsArray = new RPGLObject[targets.size()];
            for (int i = 0; i < targets.size(); i++) {
                targetsArray[i] = targets.get(i);
            }

            CustomContext context = (CustomContext) RPGLClient.CONTEXT;
            context.clearMessages();
            object.invokeEvent(targetsArray, event, resources, context);
            RPGLClient.setEvent(null);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder
                    .append(object.getName())
                    .append(" uses ")
                    .append(event.getName())
                    .append(" on ");
            for (int i = 0; i < targetsArray.length; i++) {
                stringBuilder.append(targetsArray[i].getName());
                if (i < targetsArray.length - 1) {
                    stringBuilder.append(", ");
                } else {
                    stringBuilder.append("!");
                }
            }

            Stack<String> messages = context.getMessages();
            while (!messages.isEmpty()) {
                stringBuilder.append('\n').append(messages.pop());
            }
            e.reply(stringBuilder.toString()).queue();
        } else {
            e.reply("You cannot use this command until you complete `/action`, `/target`, and `/spend` first! Do you need to use `/spend` again?")
                    .setEphemeral(true)
                    .queue();
        }
    }

    private static void slashGive(SlashCommandInteractionEvent e) {
        RPGLObject object = UUIDTable.getObject(Objects.requireNonNull(e.getOption("target")).getAsString());
        RPGLItem item = RPGLFactory.newItem(Objects.requireNonNull(e.getOption("item")).getAsString());
        object.giveItem(item.getUuid());
        e.reply(String.format("Gave %s to %s", item.getName(), object.getName())).queue();
    }

    private static void slashRename(SlashCommandInteractionEvent e) {
        RPGLObject object = UUIDTable.getObject(Objects.requireNonNull(e.getOption("my_object")).getAsString());
        String newName = Objects.requireNonNull(e.getOption("new_name")).getAsString();
        String oldName = object.getName();
        object.setName(newName);
        e.reply(String.format("Renamed %s to %s!", oldName, newName)).queue();
    }

    private static void slashNew(SlashCommandInteractionEvent e) {
        e.reply("Starting new adventure!").queue();
        UUIDTable.clear();
        RPGLClient.CONTEXT.clear();
        OptionMapping saveNameOption = e.getOption("save_name");
        String saveName = saveNameOption == null ? null : saveNameOption.getAsString();
        if (saveName == null) {
            RPGLClient.setCurrentSaveName(RPGLClient.getDefaultSaveName());
        } else {
            RPGLClient.setCurrentSaveName(saveName);
        }
    }

    private static void slashSave(SlashCommandInteractionEvent e) {
        OptionMapping saveNameOption = e.getOption("save_name");
        String saveName = saveNameOption == null ? RPGLClient.getCurrentSaveName() : saveNameOption.getAsString();
        try {
            UUIDTable.saveToDirectory(new File("saves" + File.separator + saveName));
            e.reply("Saved as " + saveName).queue();
        } catch (IOException ex) {
            e.reply("ERROR: failed to save as " + saveName).queue();
            System.out.println("ERROR: failed to save as " + saveName);
            System.out.println(ex.getMessage());
        }
    }

    private static void slashList(SlashCommandInteractionEvent e) {
        String scope = Objects.requireNonNull(e.getOption("scope")).getAsString();
        StringBuilder replyBuilder = new StringBuilder();
        List<RPGLObject> objects = List.of();
        switch (scope) {
            case "all" -> objects = UUIDTable.getObjects();
            case "mine" -> objects = UUIDTable.getObjectsByUserId(e.getUser().getName());
        }
        for (RPGLObject object : objects) {
            replyBuilder.append("`[").append(object.getUuid().split("-")[0]).append("]` ").append(object.getName()).append("\n");
        }
        String reply = replyBuilder.toString();
        if (Objects.equals("", reply)) {
            e.reply("no objects to list").queue();
        } else {
            e.reply(reply).queue();
        }
    }

    private static void slashEquip(SlashCommandInteractionEvent e) {
        RPGLObject object = UUIDTable.getObject(Objects.requireNonNull(e.getOption("my_object")).getAsString());
        RPGLItem item = UUIDTable.getItem(Objects.requireNonNull(e.getOption("item")).getAsString());
        String inventorySlot = Objects.requireNonNull(e.getOption("slot")).getAsString();
        object.equipItem(item.getUuid(), inventorySlot);
        e.reply(String.format("%s equipped %s in its %s slot",
                object.getName(),
                item.getName(),
                inventorySlot
        )).queue();
    }

    private static void slashUnequip(SlashCommandInteractionEvent e) throws Exception {
        RPGLObject object = UUIDTable.getObject(Objects.requireNonNull(e.getOption("my_object")).getAsString());
        String inventorySlot = Objects.requireNonNull(e.getOption("slot")).getAsString();
        String reply;
        if (object.getEquippedItems().asMap().containsKey(inventorySlot)) {
            object.unequipItem(inventorySlot, false, RPGLClient.CONTEXT);
            reply = String.format("%s emptied %s",
                    object.getName(),
                    inventorySlot
            );
        } else {
            reply = String.format("%s does not have an item equipped in its %s slot", object.getName(), inventorySlot);
        }
        e.reply(reply).queue();
    }

    private static void slashLoad(SlashCommandInteractionEvent e) {
        String saveName = Objects.requireNonNull(e.getOption("save_name")).getAsString();
        try {
            UUIDTable.clear();
            UUIDTable.loadFromDirectory(new File("saves" + File.separator + saveName));
            for (RPGLObject object : UUIDTable.getObjects()) {
                RPGLClient.CONTEXT.add(object);
            }
            e.reply("Loaded as " + saveName).queue();
        } catch (IOException ex) {
            e.reply("ERROR: failed to load as " + saveName).queue();
            System.out.println("ERROR: failed to load as " + saveName);
            System.out.println(ex.getMessage());
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static void slashSpawn(SlashCommandInteractionEvent e) {
        RPGLObject object = RPGLFactory.newObject(e.getOption("id").getAsString(), e.getUser().getName());
        OptionMapping nameOption = e.getOption("name");
        if (nameOption != null) {
            object.setName(nameOption.getAsString());
        }
        RPGLClient.CONTEXT.add(object);
        e.reply("Spawned " + object.getName()).queue();
    }

    private static void slashHelp(SlashCommandInteractionEvent e) {
        String dataType = Objects.requireNonNull(e.getOption("data_type")).getAsString();
        String id = Objects.requireNonNull(e.getOption("id")).getAsString();
        String[] splitId = id.split(":");
        StringBuilder stringBuilder = new StringBuilder();
        switch (dataType) {
            case "effect" -> {
                RPGLEffectTemplate template = DatapackLoader.DATAPACKS.get(splitId[0]).getEffectTemplate(splitId[1]);
                stringBuilder.append("```").append(template.prettyPrint()).append("```");
            }
            case "e" -> {
                RPGLEventTemplate template = DatapackLoader.DATAPACKS.get(splitId[0]).getEventTemplate(splitId[1]);
                stringBuilder.append("```").append(template.prettyPrint()).append("```");
            }
            case "item" -> {
                RPGLItemTemplate template = DatapackLoader.DATAPACKS.get(splitId[0]).getItemTemplate(splitId[1]);
                stringBuilder.append("```").append(template.prettyPrint()).append("```");
            }
            case "object" -> {
                RPGLObjectTemplate template = DatapackLoader.DATAPACKS.get(splitId[0]).getObjectTemplate(splitId[1]);
                stringBuilder.append("```").append(template.prettyPrint()).append("```");
            }
            case "resource" -> {
                RPGLResourceTemplate template = DatapackLoader.DATAPACKS.get(splitId[0]).getResourceTemplate(splitId[1]);
                stringBuilder.append("```").append(template.prettyPrint()).append("```");
            }
        }
        e.reply(stringBuilder.toString()).setEphemeral(true).queue();
    }

    private static void slashFight(SlashCommandInteractionEvent e) throws Exception {
        RPGLClient.clearTurnOrder();
        RPGLContext context = RPGLClient.CONTEXT;
        List<RPGLObject> objects = context.getContextObjects();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Rolling for initiative!\n");
        for (RPGLObject object : objects) {
            try {
                stringBuilder.append(object.getName()).append(": ");
                double initiative = object.abilityCheck("dex", "initiative", context);
                stringBuilder.append((int) initiative).append('\n');
                initiative += ((double) object.getAbilityScoreFromAbilityName("dex", context) + new Random().nextDouble()) / 100.0;
                RPGLClient.assignInitiative(initiative, object);
            } catch (Exception ex) {
                stringBuilder.append("ERROR\n");
                System.out.println(ex.getMessage());
            }
        }

        RPGLObject firstTurn = Objects.requireNonNull(RPGLClient.currentTurnObject());
        firstTurn.invokeInfoSubevent(RPGLClient.CONTEXT, "start_turn");
        stringBuilder.append("First up is ").append(firstTurn.getName()).append('!');
        e.reply(stringBuilder.toString()).queue();
    }

    private static void slashRest(SlashCommandInteractionEvent e) throws Exception {
        String duration = Objects.requireNonNull(e.getOption("duration")).getAsString();
        switch (duration) {
            case "long" -> longRest(e);
            case "short" -> shortRest(e);
        }
    }

    private static void slashInspect(SlashCommandInteractionEvent e) throws Exception {
        RPGLObject object = UUIDTable.getObject(Objects.requireNonNull(e.getOption("target")).getAsString());
        StringBuilder stringBuilder = new StringBuilder();

        // Name
        stringBuilder.append(object.getName()).append('\n');

        // AC
        stringBuilder.append("`AC: ").append(object.getBaseArmorClass(RPGLClient.CONTEXT)).append("`\n");

        // Health
        JsonObject healthData = object.getHealthData();
        stringBuilder.append("`Health: ").append(healthData.getInteger("current"));
        if (healthData.getInteger("temporary") > 0) {
            stringBuilder.append(" (+").append(healthData.getInteger("temporary")).append(')');
        }
        stringBuilder.append(" / ").append(object.getMaximumHitPoints(RPGLClient.CONTEXT)).append("`\n");

        // Equipped Items
        JsonObject equippedItems = object.getEquippedItems();
        if (!equippedItems.asMap().isEmpty()) {
            stringBuilder.append("Equipment:\n");
            equippedItems.asMap().keySet().stream().sorted().forEach(inventorySlot -> stringBuilder
                    .append('`')
                    .append(inventorySlot)
                    .append(": ")
                    .append(UUIDTable.getItem(equippedItems.getString(inventorySlot)).getName())
                    .append("`\n")
            );
        }

        // if userid matches user invoking command
        if (Objects.equals(object.getUserId(), e.getUser().getName())) {

            // Inventory
            JsonArray inventory = object.getInventory();
            if (!inventory.asList().isEmpty()) {
                stringBuilder.append("Inventory:\n");
                for (int i = 0; i < inventory.size(); i++) {
                    stringBuilder.append('`').append(UUIDTable.getItem(inventory.getString(i)).getName()).append("`\n");
                }
            }

            // Effects
            List<RPGLEffect> effects = object.getEffectObjects().stream()
                    .sorted(Comparator.comparing(DatapackContent::getName))
                    .filter(effect -> effect.hasTag("temporary"))
                    .toList();
            if (!effects.isEmpty()) {
                stringBuilder.append("Effects:\n");
                for (RPGLEffect effect : effects) {
                    stringBuilder.append('`').append(effect.getName()).append("`\n");
                }
            }
        }

        e.reply(stringBuilder.toString()).setEphemeral(true).queue();
    }

    private static void slashTurn(SlashCommandInteractionEvent e) throws Exception {
        String operation = Objects.requireNonNull(e.getOption("operation")).getAsString();
        switch(operation) {
            case "end" -> endTurn(e);
            case "who" -> whoseTurn(e);
        }
    }

    private static void endTurn(SlashCommandInteractionEvent e) throws Exception {
        if (isUsersTurn(e)) {
            RPGLObject currentObject = RPGLClient.currentTurnObject();
            if (currentObject != null) {
                currentObject.invokeInfoSubevent(RPGLClient.CONTEXT, "end_turn");

                RPGLObject nextObject = RPGLClient.nextTurnObject();
                nextObject.invokeInfoSubevent(RPGLClient.CONTEXT, "start_turn");

                e.reply(currentObject.getName() + " ends its turn.\n\nNext up is " + nextObject.getName() + '!').queue();
            } else {
                e.reply("You are not in combat.").queue();
            }
        } else {
            e.reply("You cannot use this command unless it is your turn!").setEphemeral(true).queue();
        }
    }

    private static void whoseTurn(SlashCommandInteractionEvent e) {
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
        e.reply(reply).setEphemeral(true).queue();
    }

    private static void longRest(SlashCommandInteractionEvent e) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        for (RPGLObject object : UUIDTable.getObjectsByUserId(e.getUser().getName())) {
            object.invokeInfoSubevent(RPGLClient.CONTEXT, "long_rest");
            stringBuilder.append(object.getName()).append(" takes a long rest.");
            object.getHealthData().putInteger("current", object.getMaximumHitPoints(RPGLClient.CONTEXT));
        }
        e.reply(stringBuilder.toString()).setEphemeral(true).queue();
    }

    private static void shortRest(SlashCommandInteractionEvent e) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        for (RPGLObject object : UUIDTable.getObjectsByUserId(e.getUser().getName())) {
            object.invokeInfoSubevent(RPGLClient.CONTEXT, "short_rest");
            stringBuilder.append(object.getName()).append(" takes a short rest.");
            // TODO something about spending hit dice here.
        }
        e.reply(stringBuilder.toString()).setEphemeral(true).queue();
    }

}

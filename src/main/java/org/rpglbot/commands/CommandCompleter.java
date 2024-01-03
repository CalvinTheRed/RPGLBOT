package org.rpglbot.commands;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.rpgl.core.RPGLFactory;
import org.rpgl.core.RPGLObject;
import org.rpgl.datapack.DatapackLoader;
import org.rpgl.json.JsonArray;
import org.rpgl.json.JsonObject;
import org.rpgl.uuidtable.UUIDTable;
import org.rpglbot.RPGLClient;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class CommandCompleter extends ListenerAdapter {

    private static String fileAsDatapackId(File file) {
        String[] split = file.getPath().split("[/\\\\]");
        StringBuilder stringBuilder = new StringBuilder(split[1] + ":");
        for (int i = 3; i < split.length; i++) {
            stringBuilder.append(split[i]);
            if (i < split.length - 1) {
                stringBuilder.append("/");
            }
        }
        if (file.isDirectory()) {
            return stringBuilder.append("/").toString();
        }
        return stringBuilder.toString().replaceFirst("\\..*", "");
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        String focusedOption = event.getFocusedOption().getName();
        switch (focusedOption) {
            case "id" -> autocompleteIdOption(event);
            case "item" -> autocompleteItemOption(event);
            case "data_type" -> autocompleteDataTypeOption(event);
            case "save_name" -> autocompleteSaveNameOption(event);
            case "scope" -> autocompleteScopeOption(event);
            case "slot" -> autocompleteSlotOption(event);
            case "my_object" -> autocompleteMyObjectsOption(event);
            case "my_event" -> autocompleteMyEventsOption(event);
            case "my_resources" -> autocompleteMyResourcesOption(event);
            case "target", "targets" -> autocompleteTargetObjectOption(event);
            case "operation" -> autocompleteOperationOption(event);
            case "duration" -> autocompleteDurationOption(event);
        }
    }

    private void autocompleteSlotOption(CommandAutoCompleteInteractionEvent event) {
        switch (event.getName()) {
            case "equip" -> listOrdinaryEquipmentSlots(event);
            case "unequip" -> listActiveEquipmentSlots(event);
        }
    }

    private void autocompleteDurationOption(CommandAutoCompleteInteractionEvent event) {
        event.replyChoices(List.of(
                new Command.Choice("long", "long"),
                new Command.Choice("short", "short")
        )).queue();
    }

    private void autocompleteItemOption(CommandAutoCompleteInteractionEvent event) {
        switch (event.getName()) {
            case "equip" -> listItemsInInventory(event);
            case "give" -> autocompleteIdOption(event);
        }
    }

    private void listItemsInInventory(CommandAutoCompleteInteractionEvent event) {
        event.replyChoices(UUIDTable.getObject(Objects.requireNonNull(event.getOption("my_object")).getAsString())
                .getInventory().asList().stream().map(uuid -> new Command.Choice(
                        UUIDTable.getItem((String) uuid).getName(),
                        (String) uuid
                )).toList()).queue();
    }

    private void autocompleteIdOption(CommandAutoCompleteInteractionEvent event) {
        switch (event.getName()) {
            case "help" -> listRPGLIds(Objects.requireNonNull(event.getOption("data_type")).getAsString(), event);
            case "spawn" -> listRPGLIds("object", event);
            case "give" -> listRPGLIds("item", event);
        }
    }

    private void autocompleteDataTypeOption(CommandAutoCompleteInteractionEvent event) {
        String value = event.getFocusedOption().getValue();
        List<Command.Choice> options = Stream.of("effect", "event", "item", "object", "resource")
                .filter(dataType -> dataType.startsWith(value))
                .map(dataType -> new Command.Choice(dataType, dataType))
                .toList();
        event.replyChoices(options).queue();
    }

    private void autocompleteSaveNameOption(CommandAutoCompleteInteractionEvent event) {
        switch (event.getName()) {
            case "load", "save" -> listSaves(event);
        }
    }

    private void autocompleteScopeOption(CommandAutoCompleteInteractionEvent event) {
        String value = event.getFocusedOption().getValue();
        List<Command.Choice> options = Stream.of("all", "mine")
                .filter(dataType -> dataType.startsWith(value))
                .map(dataType -> new Command.Choice(dataType, dataType))
                .toList();
        event.replyChoices(options).queue();
    }

    private void autocompleteMyObjectsOption(CommandAutoCompleteInteractionEvent event) {
        String userId = event.getUser().getName();
        String value = event.getFocusedOption().getValue();
        List<Command.Choice> options = UUIDTable.getObjectsByUserId(userId).stream()
                .filter(object -> object.getName().startsWith(value))
                .map(object -> new Command.Choice(object.getName(), object.getUuid()))
                .toList();
        event.replyChoices(options).queue();
    }

    private void autocompleteTargetObjectOption(CommandAutoCompleteInteractionEvent event) {
        String value = event.getFocusedOption().getValue();
        int lastDelimiter = value.lastIndexOf(',');
        String lockedTargets = value.substring(0, Math.max(lastDelimiter, 0));
        String focusedTarget = value.substring(Math.max(lastDelimiter + 1, 0));
        List<Command.Choice> options = UUIDTable.getObjects().stream()
                .filter(object -> object.getName().startsWith(focusedTarget) && !lockedTargets.contains(object.getUuid()))
                .map(object -> lockedTargets.equals("")
                        ? new Command.Choice(object.getUuid(), object.getUuid())
                        : new Command.Choice(
                                lockedTargets + "," + object.getUuid(),
                                lockedTargets + "," + object.getUuid()))
                .toList();
        event.replyChoices(options).queue();
    }

    private void autocompleteMyEventsOption(CommandAutoCompleteInteractionEvent event) {
        RPGLObject object = UUIDTable.getObject(Objects.requireNonNull(event.getOption("my_object")).getAsString());
        String value = event.getFocusedOption().getValue();
        List<Command.Choice> options;
        try {
            options = object.getEventObjects(RPGLClient.CONTEXT).stream()
                    .filter(rpglEvent -> rpglEvent.getName().startsWith(value))
                    .map(rpglEvent -> new Command.Choice(rpglEvent.getName(), rpglEvent.getId()))
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        event.replyChoices(options).queue();
    }

    private void autocompleteOperationOption(CommandAutoCompleteInteractionEvent event) {
        event.replyChoices(List.of(
                new Command.Choice("end", "end"),
                new Command.Choice("who", "who")
        )).queue();
    }

    private void autocompleteMyResourcesOption(CommandAutoCompleteInteractionEvent event) {
        RPGLObject object = UUIDTable.getObject(Objects.requireNonNull(event.getOption("my_object")).getAsString());
        String value = event.getFocusedOption().getValue();
        int lastDelimiter = value.lastIndexOf(',');
        String lockedResources = value.substring(0, Math.max(lastDelimiter, 0));
        String focusedResource = value.substring(Math.max(lastDelimiter + 1, 0));

        // find resource tags required
        JsonArray costArray = RPGLFactory.newEvent(Objects.requireNonNull(event.getOption("my_event")).getAsString()).getCost();
        int numLockedResources = "".equals(lockedResources) ? 0 : lockedResources.split(",").length;
        int costIndex = 0;
        JsonObject cost;
        do {
            cost = costArray.getJsonObject(costIndex++);
            numLockedResources -= Objects.requireNonNullElse(cost.getInteger("count"), 1); // <-- should be a RPGL thing...
        } while (numLockedResources >= 0 && costIndex < costArray.size());
        JsonArray resourceTags = cost.getJsonArray("resource_tags");

        List<Command.Choice> options = object.getResourceObjects().stream()
                .filter(resource -> resource.getName().startsWith(focusedResource)
                        && !resource.getExhausted()
                        && resource.getTags().containsAny(resourceTags.asList())
                        && !lockedResources.contains(resource.getUuid()))
                .map(resource -> lockedResources.equals("")
                        ? new Command.Choice(resource.getUuid(), resource.getUuid())
                        : new Command.Choice(
                                lockedResources + "," + resource.getUuid(),
                                lockedResources + "," + resource.getUuid()))
                .toList();
        event.replyChoices(options).queue();
    }

    private void listRPGLIds(String dataType, CommandAutoCompleteInteractionEvent event) {
        String value = event.getFocusedOption().getValue();
        List<Command.Choice> options;
        if (value.contains(":")) {
            String[] split = value.split(":");
            int deepestSlashIndex = split.length == 1 ? -1 : split[1].lastIndexOf("/");
            String rootDirPath = String.format("datapacks/%s/%ss/%s",
                    split[0],
                    dataType,
                    deepestSlashIndex == -1 ? "" : split[1].substring(0, deepestSlashIndex + 1)
            ).replace("/", File.separator);
            options = Stream.of(Objects.requireNonNull(new File(rootDirPath).listFiles()))
                    .filter(file -> fileAsDatapackId(file).startsWith(value))
                    .map(file -> new Command.Choice(fileAsDatapackId(file), fileAsDatapackId(file)))
                    .toList();
        } else {
            options = DatapackLoader.DATAPACKS.keySet().stream()
                    .filter(datapack -> datapack.startsWith(value))
                    .map(datapack -> new Command.Choice(datapack + ":", datapack + ":"))
                    .toList();
        }
        event.replyChoices(options).queue();
    }

    private void listOrdinaryEquipmentSlots(CommandAutoCompleteInteractionEvent event) {
        event.replyChoices(List.of(
                new Command.Choice("armor", "armor"),
                new Command.Choice("mainhand", "mainhand"),
                new Command.Choice("offhand", "offhand")
        )).queue();
    }

    private void listActiveEquipmentSlots(CommandAutoCompleteInteractionEvent event) {
        event.replyChoices(UUIDTable
                .getObject(Objects.requireNonNull(event.getOption("my_object")).getAsString())
                .getEquippedItems().asMap().keySet().stream().filter(Objects::nonNull).map(
                        slot -> new Command.Choice(slot, slot)
                ).toList()).queue();
    }

    private void listSaves(CommandAutoCompleteInteractionEvent event) {
        List<Command.Choice> options = Stream.of(Objects.requireNonNull(new File("saves").listFiles()))
                .map(file -> new Command.Choice(file.getName(), file.getName()))
                .toList();
        event.replyChoices(options).queue();
    }

}

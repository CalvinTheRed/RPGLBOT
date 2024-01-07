package org.rpglbot.commands;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.rpgl.core.RPGLEvent;
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
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent e) {
        String focusedOption = e.getFocusedOption().getName();
        try {
            switch (focusedOption) {
                case "id" -> autocompleteIdOption(e);
                case "item" -> autocompleteItemOption(e);
                case "data_type" -> autocompleteDataTypeOption(e);
                case "save_name" -> autocompleteSaveNameOption(e);
                case "scope" -> autocompleteScopeOption(e);
                case "slot" -> autocompleteSlotOption(e);
                case "my_object" -> autocompleteMyObjectsOption(e);
                case "event" -> autocompleteEventOption(e);
                case "resource" -> autocompleteResourceOption(e);
                case "target" -> autocompleteTargetObjectOption(e);
                case "operation" -> autocompleteOperationOption(e);
                case "duration" -> autocompleteDurationOption(e);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void autocompleteSlotOption(CommandAutoCompleteInteractionEvent e) {
        switch (e.getName()) {
            case "equip" -> listOrdinaryEquipmentSlots(e);
            case "unequip" -> listActiveEquipmentSlots(e);
        }
    }

    private void autocompleteDurationOption(CommandAutoCompleteInteractionEvent e) {
        e.replyChoices(List.of(
                new Command.Choice("long", "long"),
                new Command.Choice("short", "short")
        )).queue();
    }

    private void autocompleteItemOption(CommandAutoCompleteInteractionEvent e) {
        switch (e.getName()) {
            case "equip" -> listItemsInInventory(e);
            case "give" -> autocompleteIdOption(e);
        }
    }

    private void listItemsInInventory(CommandAutoCompleteInteractionEvent e) {
        e.replyChoices(UUIDTable.getObject(Objects.requireNonNull(e.getOption("my_object")).getAsString())
                .getInventory().asList().stream().map(uuid -> new Command.Choice(
                        UUIDTable.getItem((String) uuid).getName(),
                        (String) uuid
                )).toList()).queue();
    }

    private void autocompleteIdOption(CommandAutoCompleteInteractionEvent e) {
        switch (e.getName()) {
            case "help" -> listRPGLIds(Objects.requireNonNull(e.getOption("data_type")).getAsString(), e);
            case "spawn" -> listRPGLIds("object", e);
            case "give" -> listRPGLIds("item", e);
        }
    }

    private void autocompleteDataTypeOption(CommandAutoCompleteInteractionEvent e) {
        String value = e.getFocusedOption().getValue();
        List<Command.Choice> options = Stream.of("effect", "event", "item", "object", "resource")
                .filter(dataType -> dataType.toUpperCase().startsWith(value.toUpperCase()))
                .map(dataType -> new Command.Choice(dataType, dataType))
                .toList();
        e.replyChoices(options).queue();
    }

    private void autocompleteSaveNameOption(CommandAutoCompleteInteractionEvent e) {
        switch (e.getName()) {
            case "load", "save" -> listSaves(e);
        }
    }

    private void autocompleteScopeOption(CommandAutoCompleteInteractionEvent e) {
        String value = e.getFocusedOption().getValue();
        List<Command.Choice> options = Stream.of("all", "mine")
                .filter(dataType -> dataType.toUpperCase().startsWith(value.toUpperCase()))
                .map(dataType -> new Command.Choice(dataType, dataType))
                .toList();
        e.replyChoices(options).queue();
    }

    private void autocompleteMyObjectsOption(CommandAutoCompleteInteractionEvent e) {
        String userId = e.getUser().getName();
        String value = e.getFocusedOption().getValue();
        List<Command.Choice> options = UUIDTable.getObjectsByUserId(userId).stream()
                .filter(object -> object.getName().toUpperCase().startsWith(value.toUpperCase()))
                .map(object -> new Command.Choice(object.getName(), object.getUuid()))
                .toList();
        e.replyChoices(options).queue();
    }

    private void autocompleteTargetObjectOption(CommandAutoCompleteInteractionEvent e) {
        String value = e.getFocusedOption().getValue();
        List<Command.Choice> options = UUIDTable.getObjects().stream()
                .filter(object -> object.getName().toUpperCase().startsWith(value.toUpperCase())
                        && !RPGLClient.getTargets().contains(object))
                .map(object -> new Command.Choice(object.getName(), object.getUuid()))
                .toList();
        e.replyChoices(options).queue();
    }

    private void autocompleteEventOption(CommandAutoCompleteInteractionEvent e) throws Exception {
        RPGLObject object = RPGLClient.currentTurnObject();
        if (object != null && Objects.equals(object.getUserId(), e.getUser().getName())) {
            String value = e.getFocusedOption().getValue();
            List<Command.Choice> options = object.getEventObjects(RPGLClient.CONTEXT).stream()
                    .filter(rpglEvent -> rpglEvent.getName().toUpperCase().startsWith(value.toUpperCase()))
                    .map(rpglEvent -> new Command.Choice(rpglEvent.getName(), rpglEvent.getId()))
                    .toList();
            e.replyChoices(options).queue();
        }
    }

    private void autocompleteOperationOption(CommandAutoCompleteInteractionEvent e) {
        e.replyChoices(List.of(
                new Command.Choice("end", "end"),
                new Command.Choice("who", "who")
        )).queue();
    }

    private void autocompleteResourceOption(CommandAutoCompleteInteractionEvent e) {
        String value = e.getFocusedOption().getValue();

        // find resource tags required
        RPGLEvent event = RPGLClient.getEvent();
        if (event != null) {
            JsonArray costArray = event.getCost();
            int numSelectedResources = RPGLClient.getResources().size();
            int costIndex = 0;
            JsonObject cost;
            do {
                cost = costArray.getJsonObject(costIndex++);
                numSelectedResources -= Objects.requireNonNullElse(cost.getInteger("count"), 1); // <-- should be a RPGL thing...
            } while (numSelectedResources >= 0 && costIndex < costArray.size());
            JsonArray resourceTags = cost.getJsonArray("resource_tags");

            // compile options
            RPGLObject object = RPGLClient.currentTurnObject();
            if (object != null) {
                List<Command.Choice> options = object.getResourceObjects().stream()
                        .filter(resource -> resource.getName().toUpperCase().startsWith(value.toUpperCase())
                                && !resource.getExhausted()
                                && resource.getTags().containsAny(resourceTags.asList())
                                && !RPGLClient.getResources().contains(resource))
                        .map(resource -> new Command.Choice(resource.getName(), resource.getUuid()))
                        .toList();
                e.replyChoices(options).queue();
            }
        }
    }

    private void listRPGLIds(String dataType, CommandAutoCompleteInteractionEvent e) {
        String value = e.getFocusedOption().getValue();
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
                    .filter(file -> fileAsDatapackId(file).toUpperCase().startsWith(value.toUpperCase()))
                    .map(file -> new Command.Choice(fileAsDatapackId(file), fileAsDatapackId(file)))
                    .toList();
        } else {
            options = DatapackLoader.DATAPACKS.keySet().stream()
                    .filter(datapack -> datapack.toUpperCase().startsWith(value.toUpperCase()))
                    .map(datapack -> new Command.Choice(datapack + ":", datapack + ":"))
                    .toList();
        }
        e.replyChoices(options).queue();
    }

    private void listOrdinaryEquipmentSlots(CommandAutoCompleteInteractionEvent e) {
        e.replyChoices(List.of(
                new Command.Choice("armor", "armor"),
                new Command.Choice("mainhand", "mainhand"),
                new Command.Choice("offhand", "offhand")
        )).queue();
    }

    private void listActiveEquipmentSlots(CommandAutoCompleteInteractionEvent e) {
        e.replyChoices(UUIDTable
                .getObject(Objects.requireNonNull(e.getOption("my_object")).getAsString())
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

package org.rpglbot.commands;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;

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
            case "data_type" -> autocompleteDataTypeOption(event);
            case "save_name" -> autocompleteSaveNameOption(event);
            case "scope" -> autocompleteScopeOption(event);
        }
    }

    private void autocompleteIdOption(CommandAutoCompleteInteractionEvent event) {
        switch (event.getName()) {
            case "help" -> listRPGLIds(Objects.requireNonNull(event.getOption("data_type")).getAsString(), event);
            case "spawn" -> listRPGLIds("object", event);
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
            case "load" -> listSaves(event);
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

    private void listRPGLIds(String dataType, CommandAutoCompleteInteractionEvent event) {
        String value = event.getFocusedOption().getValue();
        if (value.contains(":")) {
            String[] split = value.split(":");
            int deepestSlashIndex = split.length == 1 ? -1 : split[1].lastIndexOf("/");
            String rootDirPath = String.format("datapacks/%s/%ss/%s",
                    split[0],
                    dataType,
                    deepestSlashIndex == -1 ? "" : split[1].substring(0, deepestSlashIndex + 1)
            ).replace("/", File.separator);
            List<Command.Choice> options = Stream.of(Objects.requireNonNull(new File(rootDirPath).listFiles()))
                    .filter(file -> fileAsDatapackId(file).startsWith(value))
                    .map(file -> new Command.Choice(fileAsDatapackId(file), fileAsDatapackId(file)))
                    .toList();
            event.replyChoices(options).queue();
        }
    }

    private void listSaves(CommandAutoCompleteInteractionEvent event) {
        List<Command.Choice> options = Stream.of(Objects.requireNonNull(new File("saves").listFiles()))
                .map(file -> new Command.Choice(file.getName(), file.getName()))
                .toList();
        event.replyChoices(options).queue();
    }

}

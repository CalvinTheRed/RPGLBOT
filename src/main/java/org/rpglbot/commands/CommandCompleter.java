package org.rpglbot.commands;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
        String value = event.getFocusedOption().getValue();
        if (event.getName().equals("spawn") && event.getFocusedOption().getName().equals("id") && value.contains(":")) {
            String[] split = value.split(":");
            int deepestSlashIndex = split.length == 1 ? -1 : split[1].lastIndexOf("/");
            String rootDirPath = String.format("datapacks/%s/objects/%s",
                    split[0],
                    deepestSlashIndex == -1 ? "" : split[1].substring(0, deepestSlashIndex + 1)
            ).replace("/", File.separator);
            List<Command.Choice> options = Stream.of(Objects.requireNonNull(new File(rootDirPath).listFiles()))
                    .filter(file -> fileAsDatapackId(file).startsWith(value))
                    .map(file -> new Command.Choice(fileAsDatapackId(file), fileAsDatapackId(file)))
                    .collect(Collectors.toList());
            event.replyChoices(options).queue();
        }
    }

}

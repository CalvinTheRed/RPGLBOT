package org.rpglbot.listeners;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MyListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String author = event.getAuthor().getName();
        MessageChannel messageChannel = event.getChannel();
        if ("rpgl-stuff".equals(messageChannel.getName()) && !"RPGLBOT".equals(author)) {
            String botMessage = String.format("%s said \"%s\" in %s !",
                    author,
                    event.getMessage(),
                    messageChannel.getAsMention()
            );
            messageChannel.sendMessage(botMessage).queue();

            TextChannel textChannel = event.getGuild().getTextChannelsByName(event.getMessage().toString(), true).get(0);
            textChannel.sendMessage("TEST").queue();
        } else {
            System.out.println(messageChannel.getName());
        }
    }
}

package org.rpglbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.rpglbot.commands.CommandCompleter;
import org.rpglbot.commands.CommandManager;

public class Main {

    public static void main(String[] args)  {
        RPGLClient.init();
        JDA bot = JDABuilder.createDefault(RPGLClient.CONFIG.get("TOKEN"))
                .setActivity(Activity.customStatus("Slinging Spells"))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();
        bot.addEventListener(new CommandCompleter(), new CommandManager());
    }

}
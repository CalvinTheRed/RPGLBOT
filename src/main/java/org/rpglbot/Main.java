package org.rpglbot;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.rpglbot.commands.CommandCompleter;
import org.rpglbot.commands.CommandManager;


public class Main {

    private static final Dotenv CONFIG = Dotenv.configure().load();

    public static void main(String[] args)  {
        RPGLClient.init();
        JDA bot = JDABuilder.createDefault(CONFIG.get("TOKEN"))
                .setActivity(Activity.customStatus("Slinging Spells"))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();
        bot.addEventListener(new CommandCompleter(), new CommandManager());
    }

}
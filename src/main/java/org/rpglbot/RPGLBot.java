package org.rpglbot;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.rpglbot.commands.CommandManager;
import org.rpglbot.listeners.MyListener;


public class RPGLBot {

    private static final Dotenv CONFIG = Dotenv.configure().load();

    public static void main(String[] args)  {
        new RPGLBot();
    }

    public RPGLBot() {
        RPGLClient.init();
        JDA bot = JDABuilder.createDefault(CONFIG.get("TOKEN"))
                .setActivity(Activity.customStatus("Slinging Spells"))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();
        bot.addEventListener(new MyListener(), new CommandManager());
    }

}
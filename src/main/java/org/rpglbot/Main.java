package org.rpglbot;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class Main {

    // Put your bot token here!
    private static final String TOKEN = "TOKEN";

    public static void main(String[] args) {
        new RPGLBot(JDABuilder.createDefault(TOKEN).setActivity(Activity.customStatus("Slinging Spells")).build());
    }

}
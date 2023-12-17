package org.rpglbot;

import io.github.cdimascio.dotenv.Dotenv;
import org.rpgl.core.RPGLCore;
import org.rpgl.datapack.DatapackLoader;

import java.io.File;

public final class RPGLClient {

    public static final Dotenv CONFIG = Dotenv.configure().load();

    private static String loadedSave = getDefaultSaveName();

    public static void init() {
        DatapackLoader.loadDatapacks(new File("datapacks"));
        RPGLCore.initialize();
    }

    public static String getDefaultSaveName() {
        return CONFIG.get("DEFAULT_SAVE_NAME");
    }

    public static String getCurrentSaveName() {
        return loadedSave;
    }

    public static void setCurrentSaveName(String loadedSave) {
        RPGLClient.loadedSave = loadedSave;
    }

}

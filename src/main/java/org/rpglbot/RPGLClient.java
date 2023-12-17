package org.rpglbot;

import io.github.cdimascio.dotenv.Dotenv;
import org.rpgl.core.RPGLContext;
import org.rpgl.core.RPGLCore;
import org.rpgl.core.RPGLObject;
import org.rpgl.datapack.DatapackLoader;

import java.io.File;
import java.util.List;

public final class RPGLClient {

    public static final Dotenv CONFIG = Dotenv.configure().load();

    private static String loadedSave = getDefaultSaveName();
    private static RPGLContext context = new CustomContext();

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

    public static RPGLContext getContext() {
        return context;
    }

    public static void clearContext() {
        context = new CustomContext();
    }

    public static void addContextObject(RPGLObject object) {
        context.add(object);
    }

    public static List<RPGLObject> getContextObjects() {
        return context.getContextObjects();
    }

}

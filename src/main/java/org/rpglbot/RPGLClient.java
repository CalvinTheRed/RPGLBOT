package org.rpglbot;

import io.github.cdimascio.dotenv.Dotenv;
import org.rpgl.core.RPGLContext;
import org.rpgl.core.RPGLCore;
import org.rpgl.core.RPGLObject;
import org.rpgl.datapack.DatapackLoader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class RPGLClient {

    public static final Dotenv CONFIG = Dotenv.configure().load();
    public static final RPGLContext CONTEXT = new CustomContext();

    private static String loadedSave = getDefaultSaveName();
    private static final Map<Double, RPGLObject> TURN_ORDER = new HashMap<>();

    private static double currentInitiative = Double.MAX_VALUE;

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

    public static void clearTurnOrder() {
        TURN_ORDER.clear();
        currentInitiative = Double.MAX_VALUE;
    }

    public static void assignInitiative(double initiative, RPGLObject object) {
        TURN_ORDER.put(initiative, object);
    }

    public static RPGLObject currentTurnObject() {
        if (!TURN_ORDER.isEmpty()) {
            Object[] sorted = TURN_ORDER.keySet().stream().sorted().toArray();
            int index = sorted.length - 1;
            while (index >= 0 && (Double) sorted[index] > currentInitiative) {
                index--;
            }
            currentInitiative = (Double) sorted[index];
            return TURN_ORDER.get((Double) sorted[index]);
        }
        return null;
    }

    public static RPGLObject nextTurnObject() {
        Object[] sorted = TURN_ORDER.keySet().stream().sorted().toArray();
        if (currentInitiative == (Double) sorted[0]) {
            // loop back to the top of initiative
            currentInitiative = (Double) sorted[sorted.length - 1];
        } else {
            int index = sorted.length - 1;
            while (index >= 0 && (Double) sorted[index] >= currentInitiative) {
                index--;
            }
            currentInitiative = (Double) sorted[index];
        }
        return TURN_ORDER.get(currentInitiative);
    }

}

package org.rpglbot;

import org.rpgl.core.RPGLCore;
import org.rpgl.datapack.DatapackLoader;

import java.io.File;

public final class RPGLClient {

    public static void init() {
        DatapackLoader.loadDatapacks(new File("datapacks"));
        RPGLCore.initialize();
    }

}

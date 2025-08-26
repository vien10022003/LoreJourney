package com.loreJourney.main.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.loreJourney.main.LoreJourney;

/**
 * Desktop version access
 */
public class DesktopLauncher {

	public static void main(String[] args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = LoreJourney.V_WIDTH * LoreJourney.V_SCALE;
        config.height = LoreJourney.V_HEIGHT * LoreJourney.V_SCALE;
        config.title = LoreJourney.TITLE;
        config.resizable = false;
        config.vSyncEnabled = false;
        config.backgroundFPS = 10;
        config.foregroundFPS = 60;
        config.addIcon("desktop_icon128.png", Files.FileType.Internal);
        config.addIcon("desktop_icon32.png", Files.FileType.Internal);
        config.addIcon("desktop_icon16.png", Files.FileType.Internal);
        new LwjglApplication(new LoreJourney(), config);
    }

}

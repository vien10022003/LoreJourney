package com.loreJourney.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.loreJourney.entity.Player;
import com.loreJourney.parallax.Background;
import com.loreJourney.resource.ResourceManager;
import com.loreJourney.save.Save;
import com.loreJourney.screen.*;
import com.loreJourney.screen.game.VictoryScreen;


/**
 * "LoreJourney" is a RPG/Dungeon Crawler based on RNG
 * The player will go through various levels with numerous enemies
 * and attempt to complete each level by reaching the end tile.
 *
 * @author Ming Li
 */
public class LoreJourney extends Game {

    public static final String VERSION = "1.0";
    public static final String TITLE = "LoreJourney Version " + VERSION;

    // Links
    public static final String GITHUB = "https://github.com/vien10022003/LoreJourney";

    // Desktop screen dimensions
    public static final int V_WIDTH = 200;
    public static final int V_HEIGHT = 120;
    public static final int V_SCALE = 6;

    // Rendering utilities
    public SpriteBatch batch;

    // Resources
    public ResourceManager rm;

    // Universal player
    public Player player;

    // Game save
    public Save save;

    // Screens
    public MenuScreen menuScreen;
    public GameScreen gameScreen;
    public WorldSelectScreen worldSelectScreen;
    public LevelSelectScreen levelSelectScreen;
    public StatisticsScreen statisticsScreen;
    public SettingsScreen settingsScreen;
    public VictoryScreen victoryScreen;

    // main bg
    public Background[] menuBackground;

    // debugging
    public Label fps;

	public void create() {
        batch = new SpriteBatch();
        rm = new ResourceManager();
        player = new Player("player", rm);

        save = new Save(player, "save.json");
        save.load(rm);

        // debugging
        fps = new Label("", new Label.LabelStyle(rm.pixel10, Color.RED));
        fps.setFontScale(0.5f);
        fps.setVisible(player.settings.showFps);

        // Initialize screens first
        menuScreen = new MenuScreen(this, rm);
        gameScreen = new GameScreen(this, rm);
        worldSelectScreen = new WorldSelectScreen(this, rm);
        levelSelectScreen = new LevelSelectScreen(this, rm);
        victoryScreen = new VictoryScreen(this, rm);
        
        statisticsScreen = new StatisticsScreen(this, rm);
        settingsScreen = new SettingsScreen(this, rm);

        // create parallax background
        menuBackground = new Background[3];

        // ordered by depth
        // sky
        menuBackground[0] = new Background(rm.titleScreenBackground[0],
            (OrthographicCamera) menuScreen.getStage().getCamera(), new Vector2(0, 0));
        menuBackground[0].setVector(0, 0);
        // back clouds
        menuBackground[1] = new Background(rm.titleScreenBackground[2],
            (OrthographicCamera) menuScreen.getStage().getCamera(), new Vector2(0.3f, 0));
        menuBackground[1].setVector(20, 0);
        // front clouds
        menuBackground[2] = new Background(rm.titleScreenBackground[1],
            (OrthographicCamera) menuScreen.getStage().getCamera(), new Vector2(0.3f, 0));
        menuBackground[2].setVector(60, 0);

        // profiler
        GLProfiler.enable();

        this.setScreen(menuScreen);
	}

	public void render() {
        fps.setText(Gdx.graphics.getFramesPerSecond() + " fps");
        super.render();
    }

	public void dispose() {
        batch.dispose();
        super.dispose();

        rm.dispose();
        menuScreen.dispose();
        worldSelectScreen.dispose();
        levelSelectScreen.dispose();
        victoryScreen.dispose();
        
        statisticsScreen.dispose();
        settingsScreen.dispose();

        GLProfiler.disable();
	}

    /**
     * Logs profile for SpriteBatch calls
     */
	public void profile(String source) {
        System.out.println("Profiling " + source + "..." + "\n" +
            "  Drawcalls: " + GLProfiler.drawCalls +
            ", Calls: " + GLProfiler.calls +
            ", TextureBindings: " + GLProfiler.textureBindings +
            ", ShaderSwitches:  " + GLProfiler.shaderSwitches +
            " vertexCount: " + GLProfiler.vertexCount.value);
        GLProfiler.reset();
    }

}

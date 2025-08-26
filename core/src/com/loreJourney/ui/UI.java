package com.loreJourney.ui;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.loreJourney.entity.Player;
import com.loreJourney.main.LoreJourney;
import com.loreJourney.map.TileMap;
import com.loreJourney.resource.ResourceManager;
import com.loreJourney.screen.GameScreen;

/**
 * Superclass for all UI
 * Contains useful variables and references
 *
 * @author Ming Li
 */
public abstract class UI implements Disposable {

    protected Stage stage;
    protected Viewport viewport;

    protected ResourceManager rm;
    protected TileMap tileMap;
    protected Player player;
    protected GameScreen gameScreen;
    protected LoreJourney game;

    // graphics
    protected ShapeRenderer shapeRenderer;

    public UI(final LoreJourney game, Player player, ResourceManager rm) {
        this.game = game;
        this.player = player;
        this.rm = rm;

        viewport = new StretchViewport(LoreJourney.V_WIDTH, LoreJourney.V_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, game.batch);

        shapeRenderer = new ShapeRenderer();
    }

    public UI(GameScreen gameScreen, TileMap tileMap, Player player, ResourceManager rm) {
        this.game = gameScreen.getGame();
        this.gameScreen = gameScreen;
        this.tileMap = tileMap;
        this.player = player;
        this.rm = rm;

        viewport = new StretchViewport(LoreJourney.V_WIDTH, LoreJourney.V_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, gameScreen.getBatch());

        shapeRenderer = new ShapeRenderer();
    }

    public abstract void update(float dt);

    public abstract void render(float dt);

    public Stage getStage() {
        return stage;
    }

    @Override
    public void dispose() {
        stage.dispose();
        shapeRenderer.dispose();
    }

    public void setTileMap(TileMap tileMap) { this.tileMap = tileMap; }

}
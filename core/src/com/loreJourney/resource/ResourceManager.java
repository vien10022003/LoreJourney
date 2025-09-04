package com.loreJourney.resource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
// REMOVED: import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
// REMOVED: import com.loreJourney.battle.Move;
// REMOVED: Inventory system disabled
// import com.loreJourney.inventory.Item;
// import com.loreJourney.inventory.ShopItem;
import com.loreJourney.map.Level;
import com.loreJourney.map.World;

/**
 * Main resource loading and storage class. Uses an AssetManager to manage textures, sounds,
 * musics, etc. Contains convenience methods to load and get resources from the asset manager.
 *
 * @author Ming Li
 */
public class ResourceManager {

    public AssetManager assetManager;
    // json
    private JsonReader jsonReader;

    // Texture Atlas that contains every sprite
    public TextureAtlas atlas;

    // 2D TextureRegion arrays that stores sprites of various sizes for easy animation
    public TextureRegion[][] sprites16x16;
    public TextureRegion[][] tiles16x16;
    public TextureRegion[][] atiles16x16;
    public TextureRegion[][] battleSprites96x96;
    public TextureRegion raindrop;
    public TextureRegion[][] raindropAnim16x16;
    public TextureRegion snowflake;
    public TextureRegion lightning;
    public TextureRegion shade;

    // Menu
    public TextureRegion[] title;
    public TextureRegion[] titleScreenBackground;
    public TextureRegion[][] playButton;
    public TextureRegion[][] menuButtons;
    public TextureRegion[] worldSelectBackgrounds;
    public TextureRegion[][] menuExitButton;
    public TextureRegion[][] enterButton;

    // Lighting
    public TextureRegion darkness;

    // UI
    public TextureRegion[][] dirpad20x20;
    public TextureRegion[][] optionbutton32x32;
    public TextureRegion[][] exitbutton18x18;
    public TextureRegion[][] invbuttons92x28;
    public TextureRegion dialogBox400x80;
    public TextureRegion[] creditsicons;
    public TextureRegion[][] smoveButtons;

    // Skin
    public Skin skin;
    public Skin dialogSkin;

    // misc
    public TextureRegion shadow11x6;
    public TextureRegion redarrow10x9;

    // Music
    public Music menuTheme;
    public Music slimeForestTheme;
    public Music spookyGraveyardTheme;
    public Music frostyCaveTheme;
    public Music battleTheme;

    // Sound Effects
    public Sound buttonclick0;
    public Sound buttonclick1;
    public Sound buttonclick2;
    public Sound invselectclick;
    public Sound moveselectclick;
    public Sound textprogression;
    public Sound thunder;
    public Sound lightrain;
    public Sound heavyrain;
    public Sound teleport;
    public Sound movement;
    public Sound finish;

    // Worlds
    public Array<World> worlds = new Array<World>();

    // Fonts
    public final BitmapFont pixel10;

    public ResourceManager() {
        assetManager = new AssetManager();
        jsonReader = new JsonReader();

        assetManager.load("textures.atlas", TextureAtlas.class);
        assetManager.load("skins/ui.atlas", TextureAtlas.class);
        assetManager.load("skins/dialog.atlas", TextureAtlas.class);

        assetManager.load("music/menu_theme.ogg", Music.class);
        assetManager.load("music/slime_forest_theme.ogg", Music.class);
        assetManager.load("music/spooky_graveyard_theme.ogg", Music.class);
        assetManager.load("music/frosty_cave_theme.ogg", Music.class);
        assetManager.load("music/battle_theme.ogg", Music.class);

        assetManager.load("sfx/button_click0.ogg", Sound.class);
        assetManager.load("sfx/button_click1.ogg", Sound.class);
        assetManager.load("sfx/button_click2.ogg", Sound.class);
        assetManager.load("sfx/inventory_select_click.ogg", Sound.class);
        assetManager.load("sfx/move_select_click.ogg", Sound.class);
        assetManager.load("sfx/text_progression.wav", Sound.class);
        assetManager.load("sfx/thunder.ogg", Sound.class);
        assetManager.load("sfx/light_rain.ogg", Sound.class);
        assetManager.load("sfx/heavy_rain.ogg", Sound.class);
        assetManager.load("sfx/teleport.ogg", Sound.class);
        assetManager.load("sfx/movement.ogg", Sound.class);
        assetManager.load("sfx/finish.ogg", Sound.class);

        assetManager.finishLoading();

        atlas = assetManager.get("textures.atlas", TextureAtlas.class);

        // load font
        pixel10 = new BitmapFont(Gdx.files.internal("fonts/pixel.fnt"), atlas.findRegion("pixel"), false);

        skin = new Skin(atlas);
        skin.add("default-font", pixel10);
        skin.load(Gdx.files.internal("skins/ui.json"));

        dialogSkin = new Skin(assetManager.get("skins/dialog.atlas", TextureAtlas.class));
        dialogSkin.add("default-font", pixel10);
        dialogSkin.load(Gdx.files.internal("skins/dialog.json"));

        // sprites
        sprites16x16 = atlas.findRegion("16x16_sprites").split(16, 16);
        tiles16x16 = atlas.findRegion("16x16_tiles").split(16, 16);
        atiles16x16 = atlas.findRegion("16x16_atiles").split(16, 16);
        battleSprites96x96 = atlas.findRegion("96x96_battle_sprites").split(48, 48);
        shadow11x6 = atlas.findRegion("11x6_shadow");
        redarrow10x9 = atlas.findRegion("10x9_redarrow");
        raindrop = atlas.findRegion("raindrop");
        raindropAnim16x16 = atlas.findRegion("raindrop_anim").split(16, 16);
        snowflake = atlas.findRegion("snowflake");
        lightning = atlas.findRegion("lightning");
        shade = atlas.findRegion("shade");

        // menu
        title = atlas.findRegion("loreJourney_title").split(18, 24)[0];
        titleScreenBackground = atlas.findRegion("title_bg").split(200, 120)[0];
        playButton = atlas.findRegion("play_button").split(80, 40);
        menuButtons = atlas.findRegion("menu_buttons").split(16, 16);
        worldSelectBackgrounds = atlas.findRegion("stage_select_bg").split(200, 120)[0];
        menuExitButton = atlas.findRegion("menu_exit_button").split(14, 14);
        enterButton = atlas.findRegion("enter_button").split(79, 28);

        // light
        darkness = atlas.findRegion("darkness");

        // ui
        dirpad20x20 = atlas.findRegion("dir_pad").split(20, 20);
        dialogBox400x80 = atlas.findRegion("dialog_box");
        optionbutton32x32 = atlas.findRegion("option_buttons").split(16, 16);
        exitbutton18x18 = atlas.findRegion("exit_button").split(9, 9);
        invbuttons92x28 = atlas.findRegion("inv_buttons").split(46, 14);
        creditsicons = atlas.findRegion("creditsicons").split(17, 17)[0];
        smoveButtons = atlas.findRegion("smove_buttons").split(38, 18);
        
        // fix font spacing
        pixel10.setUseIntegerPositions(false);

        // load music
        menuTheme = assetManager.get("music/menu_theme.ogg", Music.class);
        slimeForestTheme = assetManager.get("music/slime_forest_theme.ogg", Music.class);
        spookyGraveyardTheme = assetManager.get("music/spooky_graveyard_theme.ogg", Music.class);
        frostyCaveTheme = assetManager.get("music/frosty_cave_theme.ogg", Music.class);
        battleTheme = assetManager.get("music/battle_theme.ogg", Music.class);

        // load sfx
        buttonclick0 = assetManager.get("sfx/button_click0.ogg", Sound.class);
        buttonclick1 = assetManager.get("sfx/button_click1.ogg", Sound.class);
        buttonclick2 = assetManager.get("sfx/button_click2.ogg", Sound.class);
        invselectclick = assetManager.get("sfx/inventory_select_click.ogg", Sound.class);
        moveselectclick = assetManager.get("sfx/move_select_click.ogg", Sound.class);
        textprogression = assetManager.get("sfx/text_progression.wav", Sound.class);
        thunder = assetManager.get("sfx/thunder.ogg", Sound.class);
        lightrain = assetManager.get("sfx/light_rain.ogg", Sound.class);
        heavyrain = assetManager.get("sfx/heavy_rain.ogg", Sound.class);
        teleport = assetManager.get("sfx/teleport.ogg", Sound.class);
        movement = assetManager.get("sfx/movement.ogg", Sound.class);
        finish = assetManager.get("sfx/finish.ogg", Sound.class);

        loadWorlds();
        // loadMoves(); // REMOVED: No longer loading moves system
        // loadItems(); // REMOVED: Inventory system disabled

        // set smove icons
        // REMOVED: Commented out since no moves system
        // for (int i = 0; i < Util.SMOVES_ORDER_BY_ID.length; i++) {
        //     Util.SMOVES_ORDER_BY_ID[i].icon = new Image(smoveicons[i]);
        // }
    }

    /**
     * Loads the ImageButton styles for buttons with up and down image effects
     *
     * @param numButtons
     * @param sprites
     * @return a style array for ImageButtons
     */
    public ImageButton.ImageButtonStyle[] loadImageButtonStyles(int numButtons, TextureRegion[][] sprites) {
        ImageButton.ImageButtonStyle[] ret = new ImageButton.ImageButtonStyle[numButtons];
        for (int i = 0; i < numButtons; i++) {
            ret[i] = new ImageButton.ImageButtonStyle();
            ret[i].imageUp = new TextureRegionDrawable(sprites[0][i]);
            ret[i].imageDown = new TextureRegionDrawable(sprites[1][i]);
        }
        return ret;
    }

    private void loadWorlds() {
        // parse worlds.json
        JsonValue base = jsonReader.parse(Gdx.files.internal("maps/worlds.json"));

        int worldIndex = 0;
        for (JsonValue world : base.get("worlds")) {
            String worldName = world.getString("name");
            String shortDesc = world.getString("shortDesc");
            String longDesc = world.getString("longDesc");
            int numLevels = world.getInt("numLevels");

            int levelIndex = 0;
            Level[] temp = new Level[numLevels];
            // load levels
            for (JsonValue level : world.get("levels")) {
                temp[levelIndex] = new Level(worldIndex, levelIndex, level.getString("name"), level.getInt("avgLevel"));
                levelIndex++;
            }
            worlds.add(new World(worldName, shortDesc, longDesc, numLevels, temp));

            worldIndex++;
        }
    }

    /**
     * Sets the volume of all music in the game
     * @param volume
     */
    public void setMusicVolume(float volume) {
        menuTheme.setVolume(volume);
        slimeForestTheme.setVolume(volume);
        spookyGraveyardTheme.setVolume(volume);
        frostyCaveTheme.setVolume(volume);
        battleTheme.setVolume(volume);
    }

    public void dispose() {
        assetManager.dispose();
        pixel10.dispose();
        atlas.dispose();
        skin.dispose();
        dialogSkin.dispose();

        menuTheme.dispose();
        slimeForestTheme.dispose();
        spookyGraveyardTheme.dispose();
        frostyCaveTheme.dispose();
        battleTheme.dispose();

        buttonclick0.dispose();
        buttonclick1.dispose();
        buttonclick2.dispose();
        invselectclick.dispose();
        moveselectclick.dispose();
        textprogression.dispose();
        thunder.dispose();
        lightrain.dispose();
        heavyrain.dispose();
        teleport.dispose();
        movement.dispose();
        finish.dispose();
    }

}

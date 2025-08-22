package com.unlucky.ui.battleui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.unlucky.animation.AnimationManager;
import com.unlucky.effects.Moving;
import com.unlucky.effects.Particle;
import com.unlucky.effects.ParticleFactory;
import com.unlucky.entity.Entity;
import com.unlucky.entity.Player;
import com.unlucky.event.Battle;
import com.unlucky.map.TileMap;
import com.unlucky.map.WeatherType;
import com.unlucky.resource.ResourceManager;
import com.unlucky.resource.Util;
import com.unlucky.screen.GameScreen;
import com.unlucky.ui.MovingImageUI;

/**
 * Displays health bars, battle animations, move animations, etc.
 *
 * @author Ming Li
 */
public class BattleScene extends BattleUI {

    private Stage stage;

    // Health bars
    // player
    private MovingImageUI playerHud;
    private HealthBar playerHpBar;
    public Label playerHudLabel;
    // enemy
    private MovingImageUI enemyHud;
    private HealthBar enemyHpBar;
    private Label enemyHudLabel;

    // Battle scene sprite positions
    private Moving playerSprite;
    private Moving enemySprite;
    private boolean renderPlayer = true;
    private boolean renderEnemy = true;
    private final Vector2 PLAYER_ORIGIN = new Vector2(-48, 50);
    private final Vector2 ENEMY_ORIGIN = new Vector2(200, 50);

    // battle animations
    private AnimationManager[] attackAnims;
    private AnimationManager healAnim;

    // blinking hit animation
    private boolean showHitAnim = false;
    private float hitAnimDurationTimer = 0;
    private float hitAnimAlternateTimer = 0;
    private int lastHit = -1;

    // name colors based on enemy level
    /**
     * 3 or more levels lower than player = gray
     * 1 or 2 levels lower than player = green
     * same level as player = white
     * 1 or 2 levels higher than player = orange
     * 3 or more levels higher than player = red
     */
    private Label.LabelStyle weakest;
    private Label.LabelStyle weaker;
    private Label.LabelStyle same;
    private Label.LabelStyle stronger;
    private Label.LabelStyle strongest;

    // weather conditions
    private ParticleFactory factory;

    private boolean sfxPlaying = false;

    public BattleScene(GameScreen gameScreen, TileMap tileMap, Player player, Battle battle,
                       BattleUIHandler uiHandler, Stage stage, ResourceManager rm) {
        super(gameScreen, tileMap, player, battle, uiHandler, rm);

        this.stage = stage;

        BitmapFont font = rm.pixel10;
        Label.LabelStyle ls = new Label.LabelStyle(font, new Color(255, 255, 255, 255));

        weakest = new Label.LabelStyle(font, new Color(200 / 255.f, 200 / 255.f, 200 / 255.f, 1));
        weaker = new Label.LabelStyle(font, new Color(0, 225 / 255.f, 0, 1));
        same = new Label.LabelStyle(font, new Color(1, 1, 1, 1));
        stronger = new Label.LabelStyle(font, new Color(1, 175 / 255.f, 0, 1));
        strongest = new Label.LabelStyle(font, new Color(225 / 255.f, 0, 0, 1));

        // create player hud
        playerHud = new MovingImageUI(rm.playerhpbar145x40, new Vector2(-72, 100), new Vector2(0, 100), 100.f, 72, 20);
        playerHpBar = new HealthBar(player, stage, shapeRenderer, 48, 4, new Vector2(), new Color(0, 225 / 255.f, 0, 1));
        playerHudLabel = new Label("", ls);
        playerHudLabel.setFontScale(0.5f);
        playerHudLabel.setSize(49, 6);
        playerHudLabel.setTouchable(Touchable.disabled);

        // create enemy hud
        enemyHud = new MovingImageUI(rm.enemyhpbar145x40, new Vector2(200, 100), new Vector2(128, 100), 100.f, 72, 20);
        enemyHpBar = new HealthBar(null, stage, shapeRenderer, 48, 4, new Vector2(), new Color(225 / 255.f, 0, 0, 1));
        enemyHudLabel = new Label("", ls);
        enemyHudLabel.setFontScale(0.5f);
        enemyHudLabel.setSize(49, 6);
        enemyHudLabel.setTouchable(Touchable.disabled);

        // create player sprite
        playerSprite = new Moving(PLAYER_ORIGIN, new Vector2(35, 50), 75.f);
        // create enemy sprite
        enemySprite = new Moving(ENEMY_ORIGIN, new Vector2(120, 50), 75.f);

        // create animations
        attackAnims = new AnimationManager[3];
        for (int i = 0; i < 3; i++) {
            attackAnims[i] = new AnimationManager(rm.battleAttacks64x64, 3, i, 1 / 6f);
        }
        healAnim = new AnimationManager(rm.battleHeal96x96, 3, 0, 1 / 5f);

        factory = new ParticleFactory((OrthographicCamera) stage.getCamera(), rm);

        stage.addActor(playerHud);
        stage.addActor(playerHudLabel);
        stage.addActor(enemyHud);
        stage.addActor(enemyHudLabel);
    }

    public void toggle(boolean toggle) {
        gameScreen.getGame().fps.setPosition(5, 115);
        stage.addActor(gameScreen.getGame().fps);

        playerHud.setVisible(toggle);
        playerHudLabel.setVisible(toggle);
        playerHudLabel.setText("HP: " + player.getHp() + "/" + player.getMaxHp());
        playerHud.start();

        if (toggle) {
            enemyHpBar.setEntity(battle.opponent);

            if (battle.opponent.isBoss()) {
                // boss's name is always red
                enemyHudLabel.setStyle(strongest);
            }
            else {
                int diff = battle.opponent.getLevel() - player.getLevel();
                if (diff <= -3) enemyHudLabel.setStyle(weakest);
                else if (diff == -1 || diff == -2) enemyHudLabel.setStyle(weaker);
                else if (diff == 0) enemyHudLabel.setStyle(same);
                else if (diff == 1 || diff == 2) enemyHudLabel.setStyle(stronger);
                else if (diff >= 3) enemyHudLabel.setStyle(strongest);
            }
            enemyHudLabel.setText(battle.opponent.getId());
        }

        enemyHud.setVisible(toggle);
        enemyHudLabel.setVisible(toggle);
        enemyHud.start();

        playerSprite.start();
        enemySprite.start();

        if (gameScreen.gameMap.weather == WeatherType.RAIN) {
            factory.set(Particle.STATIC_RAINDROP, 40, new Vector2(Util.RAINDROP_X, -100));
        } else if (gameScreen.gameMap.weather == WeatherType.HEAVY_RAIN ||
                gameScreen.gameMap.weather == WeatherType.THUNDERSTORM) {
            factory.set(Particle.STATIC_RAINDROP, 75, new Vector2(Util.RAINDROP_X, -120));
        } else if (gameScreen.gameMap.weather == WeatherType.SNOW) {
            factory.set(Particle.SNOWFLAKE, 100, new Vector2(Util.SNOWFLAKE_X, -60));
        } else if (gameScreen.gameMap.weather == WeatherType.BLIZZARD) {
            factory.set(Particle.SNOWFLAKE, 300, new Vector2(Util.SNOWFLAKE_X + 50, -80));
        }

    }

    /**
     * Resets all UI back to their starting point so the animations can begin
     * for a new battle
     */
    public void resetPositions() {
        playerHud.moving.origin.set(-72, 100);
        enemyHud.moving.origin.set(200, 100);

        playerSprite.origin.set(-48, 50);
        enemySprite.origin.set(200, 50);

        playerHud.setPosition(playerHud.moving.origin.x, playerHud.moving.origin.y);
        enemyHud.setPosition(enemyHud.moving.origin.x, enemyHud.moving.origin.y);
        playerSprite.position.set(playerSprite.origin);
        enemySprite.position.set(enemySprite.origin);
    }

    public void update(float dt) {
        playerHud.update(dt);
        enemyHud.update(dt);

        if (gameScreen.gameMap.weather != WeatherType.NORMAL) factory.update(dt);

        // entity sprite animations
        player.getBam().update(dt);
        if (battle.opponent.getBam() != null) battle.opponent.getBam().update(dt);

        playerSprite.update(dt);
        enemySprite.update(dt);

        // when enemy dies, its sprite falls off the screen
        if (player.isDead()) {
            float dy = playerSprite.position.y - 2;
            playerSprite.position.y = dy;
            if (playerSprite.position.y < -48) playerSprite.position.y = -48;
        }
        if (battle.opponent.isDead()) {
            float dy = enemySprite.position.y - 2;
            enemySprite.position.y = dy;
            if (enemySprite.position.y < -48) enemySprite.position.y = -48;
        }

        // render player and enemy sprites based on moving positions
        // hit animation
        if (showHitAnim) {
            hitAnimDurationTimer += dt;
            if (hitAnimDurationTimer < 0.7f) {
                hitAnimAlternateTimer += dt;
                if (hitAnimAlternateTimer > 0.1f) {
                    if (lastHit == 1) renderPlayer = !renderPlayer;
                    else renderEnemy = !renderEnemy;
                    hitAnimAlternateTimer = 0;
                }
            } else {
                hitAnimDurationTimer = 0;
                showHitAnim = false;
            }
        }
        else {
            renderPlayer = renderEnemy = true;
        }

        playerHudLabel.setText("HP: " + player.getHp() + "/" + player.getMaxHp());
        if (player.settings.showEnemyLevels) {
            enemyHudLabel.setText("LV." + battle.opponent.getLevel() + " " + battle.opponent.getId());
        }
        else {
            enemyHudLabel.setText(battle.opponent.getId());
        }

        // show health bar animation after an entity uses its move
        playerHpBar.update(dt);
        enemyHpBar.update(dt);

        if (playerHud.getX() != playerHud.moving.target.x - 1 &&
            enemyHud.getX() != enemyHud.moving.target.x - 1) {
            // set positions relative to hud position
            playerHpBar.setPosition(playerHud.getX() + 20, playerHud.getY() + 4);
            playerHudLabel.setPosition(playerHud.getX() + 20, playerHud.getY() + 10);
            enemyHpBar.setPosition(enemyHud.getX() + 4, enemyHud.getY() + 4);
            enemyHudLabel.setPosition(enemyHud.getX() + 6, enemyHud.getY() + 10);
        }

        if (player.getMoveUsed() != -1) updateBattleAnimations(player, dt);
        if (battle.opponent.getMoveUsed() != -1) updateBattleAnimations(battle.opponent, dt);
    }

    /**
     * Update attack and heal animations after a move is used and its dialogue is finished
     *
     * @param entity either player or enemy
     * @param dt
     */
    private void updateBattleAnimations(Entity entity, float dt) {
        // damaging moves
        if (entity.getMoveUsed() < 3 && entity.getMoveUsed() >= 0) {
            if (attackAnims[entity.getMoveUsed()].currentAnimation.isAnimationFinished()) {
                attackAnims[entity.getMoveUsed()].currentAnimation.stop();
                entity.setMoveUsed(-1);
                sfxPlaying = false;
                // start hit animation
                showHitAnim = true;
                if (!player.settings.muteSfx) rm.hit.play(player.settings.sfxVolume);
                if (entity == player) lastHit = 0;
                else lastHit = 1;
            } else {
                if (entity.getMoveUsed() == 0) {
                    if (!player.settings.muteSfx && !sfxPlaying) {
                        rm.blueattack.play(player.settings.sfxVolume);
                        sfxPlaying = true;
                    }
                }
                else if (entity.getMoveUsed() == 1) {
                    if (!player.settings.muteSfx && !sfxPlaying) {
                        rm.redattack.play(player.settings.sfxVolume);
                        sfxPlaying = true;
                    }
                }
                else {
                    if (!player.settings.muteSfx && !sfxPlaying) {
                        rm.yellowattack.play(player.settings.sfxVolume);
                        sfxPlaying = true;
                    }
                }
                attackAnims[entity.getMoveUsed()].update(dt);
            }
        }
        // heal
        else if (entity.getMoveUsed() == 3 && entity.getMoveUsed() >= 0) {
            if (healAnim.currentAnimation.isAnimationFinished()) {
                sfxPlaying = false;
                healAnim.currentAnimation.stop();
                entity.setMoveUsed(-1);
            } else {
                if (!player.settings.muteSfx && !sfxPlaying) {
                    rm.heal.play(player.settings.sfxVolume);
                    sfxPlaying = true;
                }
                healAnim.update(dt);
            }
        }
    }

    public void render(float dt) {
        gameScreen.getBatch().begin();
        if (renderPlayer) {
            gameScreen.getBatch().draw(player.getBam().getKeyFrame(true), playerSprite.position.x, playerSprite.position.y);
        }
        if (renderEnemy) {
            TextureRegion r = battle.opponent.getBam().getKeyFrame(true);
            if (battle.opponent.isBoss()) {
                gameScreen.getBatch().draw(r, enemySprite.position.x + (48 - battle.opponent.battleSize) / 2, enemySprite.position.y,
                        battle.opponent.battleSize, battle.opponent.battleSize);
            }
            else {
                gameScreen.getBatch().draw(r, enemySprite.position.x, enemySprite.position.y);
            }
        }

        // render attack or heal animations
        // player side
        if (player.getMoveUsed() != -1) {
            if (player.getMoveUsed() < 3) {
                gameScreen.getBatch().draw(attackAnims[player.getMoveUsed()].getKeyFrame(false), 127, 57);
            } else if (player.getMoveUsed() == 3) {
                gameScreen.getBatch().draw(healAnim.getKeyFrame(false), 35, 50);
            }
        }
        // enemy side
        if (battle.opponent.getMoveUsed() != -1) {
            if (battle.opponent.getMoveUsed() < 3) {
                gameScreen.getBatch().draw(attackAnims[battle.opponent.getMoveUsed()].getKeyFrame(false), 42, 57);
            } else if (battle.opponent.getMoveUsed() == 3) {
                gameScreen.getBatch().draw(healAnim.getKeyFrame(false), 120, 50);
            }
        }

        // render weather and lighting conditions if any
        if (gameScreen.gameMap.weather != WeatherType.NORMAL) factory.render(gameScreen.getBatch());

        if (gameScreen.gameMap.isDark) {
            gameScreen.getBatch().setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ONE_MINUS_SRC_ALPHA);
            gameScreen.getBatch().draw(rm.battledarkness, 0, 0);
            gameScreen.getBatch().setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }

        // render player and enemy status icons
        player.statusEffects.render(gameScreen.getBatch());
        battle.opponent.statusEffects.render(gameScreen.getBatch());
        gameScreen.getBatch().end();

        playerHpBar.render(dt);
        enemyHpBar.render(dt);
    }

}

package com.loreJourney.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.loreJourney.animation.AnimationManager;
import com.loreJourney.entity.enemy.Enemy;
// REMOVED: Inventory system disabled
// import com.loreJourney.inventory.Equipment;
// import com.loreJourney.inventory.Inventory;
// import com.loreJourney.inventory.Item;
import com.loreJourney.map.Tile;
import com.loreJourney.resource.ResourceManager;
import com.loreJourney.resource.Statistics;
import com.loreJourney.resource.Util;
import com.loreJourney.save.Settings;

/**
 * The protagonist of the game.
 *
 * @author Ming Li
 */
public class Player extends Entity {

    /**
     * -1 - stop
     * 0 - down
     * 1 - up
     * 2 - right
     * 3 - left
     */
    public int moving = -1;
    // entity is in a continuous movement
    private float speed;
    // the Entity's current tile coordinates
    private int currentTileX;
    private int currentTileY;
    private int prevDir = -1;
    // tile causing a dialog event
    private boolean tileInteraction = false;
    // teleportation tiles
    private boolean teleporting = false;
    // end tiles
    public boolean completedMap = false;

    // Statistics
    public Statistics stats = new Statistics();

    // Battle
    private Enemy opponent;
    private boolean battling = false;

    // exp and level up
    private int exp;
    private int maxExp;

    private int hpIncrease = 0;
    private int maxExpIncrease = 0;

    // gold
    private int gold = 0;

    // REMOVED: inventory and equips disabled
    // public Inventory inventory;
    // public Equipment equips;

    // whether or not the player is currently in a map
    public boolean inMap = false;

    // player's level progress stored as a (world, level) key
    public int maxWorld = 0;
    public int maxLevel = 0;

    // the player's custom game settings
    public Settings settings = new Settings();

    public Player(String id, ResourceManager rm) {
        super(id, rm);

        // REMOVED: inventory and equips disabled
        // inventory = new Inventory();
        // equips = new Equipment();

        // attributes
        hp = maxHp = previousHp = Util.PLAYER_INIT_MAX_HP;

        level = 1;
        speed = 50.f;

        exp = 0;
        // offset between 3 and 5
        maxExp = Util.calculateMaxExp(1, MathUtils.random(3, 5));

        // create tilemap animation
        am = new AnimationManager(rm.sprites16x16, Util.PLAYER_WALKING, Util.PLAYER_WALKING_DELAY);
        // create battle scene animation
        bam = new AnimationManager(rm.battleSprites96x96, 2, Util.PLAYER_WALKING, 2 / 5f);
    }

    public void update(float dt) {
        super.update(dt);

        // movement
        handleMovement(dt);
        // special tile handling
        handleSpecialTiles();

        // DISABLED: Battle system - automatically remove enemy when encountered
        if (tileMap.containsEntity(tileMap.toTileCoords(position)) && canMove()) {
            opponent = (com.loreJourney.entity.enemy.Enemy) tileMap.getEntity(tileMap.toTileCoords(position));
            // Automatically remove enemy without battle
            tileMap.removeEntity(tileMap.toTileCoords(position));
            opponent = null;
            // Skip battle entirely - no battling flag set
        }
    }

    public void render(SpriteBatch batch) {
        // draw shadow
        batch.draw(rm.shadow11x6, position.x + 3, position.y - 3);
        batch.draw(am.getKeyFrame(true), position.x + 1, position.y);
    }

    /**
     * Moves an entity to a target position with a given magnitude.
     * Player movement triggered by input
     *
     * @param dir
     */
    public void move(int dir) {
        currentTileX = (int) (position.x / tileMap.tileSize);
        currentTileY = (int) (position.y / tileMap.tileSize);
        prevDir = dir;
        moving = dir;
        stats.numSteps++;
    }

    public boolean canMove() {
        return moving == -1;
    }

    /**
     * This method is to fix a problem where the player can reset their
     * movement magnitudes continuously on a blocked tile
     *
     * @param dir
     * @return
     */
    public boolean nextTileBlocked(int dir) {
        currentTileX = (int) (position.x / tileMap.tileSize);
        currentTileY = (int) (position.y / tileMap.tileSize);
        switch (dir) {
            case 0: // down
                return tileMap.getTile(currentTileX, currentTileY - 1).isBlocked();
            case 1: // up
                return tileMap.getTile(currentTileX, currentTileY + 1).isBlocked();
            case 2: // right
                return tileMap.getTile(currentTileX + 1, currentTileY).isBlocked();
            case 3: // left
                return tileMap.getTile(currentTileX - 1, currentTileY).isBlocked();
        }
        return false;
    }

    /**
     * Returns the next tile coordinate to move to either
     * currentPos +/- 1 or currentPos if the next tile is blocked
     *
     * @param dir
     * @return
     */
    public int nextPosition(int dir) {
        switch (dir) {
            case 0: // down
                Tile d = tileMap.getTile(currentTileX, currentTileY - 1);
                if (d.isBlocked() || currentTileY - 1 <= 0) {
                    return currentTileY;
                }
                return currentTileY - 1;
            case 1: // up
                Tile u = tileMap.getTile(currentTileX, currentTileY + 1);
                if (u.isBlocked() || currentTileY + 1 >= tileMap.mapHeight - 1) {
                    return currentTileY;
                }
                return currentTileY + 1;
            case 2: // right
                Tile r = tileMap.getTile(currentTileX + 1, currentTileY);
                if (r.isBlocked() || currentTileX + 1 >= tileMap.mapWidth - 1) {
                    return currentTileX;
                }
                return currentTileX + 1;
            case 3: // left
                Tile l = tileMap.getTile(currentTileX - 1, currentTileY);
                if (l.isBlocked() || currentTileX - 1 <= 0) {
                    return currentTileX;
                }
                return currentTileX - 1;
        }
        return 0;
    }

    /**
     * Handles the player's next movements when standing on a special tile
     */
    public void handleSpecialTiles() {
        int cx = (int) (position.x / tileMap.tileSize);
        int cy = (int) (position.y / tileMap.tileSize);
        Tile currentTile = tileMap.getTile(cx, cy);

        if (currentTile.isSpecial()) am.currentAnimation.stop();

        if (canMove()) {
            // Player goes forwards or backwards from the tile in the direction they entered
            if (currentTile.isChange()) {
                if (!settings.muteSfx) rm.movement.play(settings.sfxVolume);
                boolean k = MathUtils.randomBoolean();
                switch (prevDir) {
                    case 0: // down
                        if (k) changeDirection(1);
                        else changeDirection(0);
                        break;
                    case 1: // up
                        if (k) changeDirection(0);
                        else changeDirection(1);
                        break;
                    case 2: // right
                        if (k) changeDirection(3);
                        else changeDirection(2);
                        break;
                    case 3: // left
                        if (k) changeDirection(2);
                        else changeDirection(3);
                        break;
                }
            }
            // Player goes 1 tile in a random direction not the direction they entered the tile on
            else if (currentTile.isInAndOut()) {
                if (!settings.muteSfx) rm.movement.play(settings.sfxVolume);
                // output direction (all other directions other than input direction)
                int odir = MathUtils.random(2);
                switch (prevDir) {
                    case 0: // down
                        if (odir == 0) changeDirection(3);
                        else if (odir == 1) changeDirection(2);
                        else changeDirection(0);
                        break;
                    case 1: // up
                        if (odir == 0) changeDirection(3);
                        else if (odir == 1) changeDirection(2);
                        else changeDirection(1);
                        break;
                    case 2: // right
                        if (odir == 0) changeDirection(0);
                        else if (odir == 1) changeDirection(1);
                        else changeDirection(2);
                        break;
                    case 3: // left
                        if (odir == 0) changeDirection(0);
                        else if (odir == 1) changeDirection(1);
                        else changeDirection(3);
                        break;
                }
            }
            else if (currentTile.isDown()) {
                if (!settings.muteSfx) rm.movement.play(settings.sfxVolume);
                changeDirection(0);
            }
            else if (currentTile.isUp()) {
                if (!settings.muteSfx) rm.movement.play(settings.sfxVolume);
                changeDirection(1);
            }
            else if (currentTile.isRight()) {
                if (!settings.muteSfx) rm.movement.play(settings.sfxVolume);
                changeDirection(2);
            }
            else if (currentTile.isLeft()) {
                if (!settings.muteSfx) rm.movement.play(settings.sfxVolume);
                changeDirection(3);
            }
            // trigger dialog event
            else if (currentTile.isQuestionMark() || currentTile.isExclamationMark()) tileInteraction = true;
            // trigger teleport event
            else if (currentTile.isTeleport()) teleporting = true;
            // ice sliding
            else if (currentTile.isIce()) {
                if (!nextTileBlocked(prevDir)) {
                    move(prevDir);
                    am.setAnimation(prevDir);
                    am.stopAnimation();
                    pauseAnim = true;
                }
            }
            // map completed
            else if (currentTile.isEnd()) completedMap = true;
            else pauseAnim = false;
        }
    }

    public void changeDirection(int dir) {
        move(dir);
        prevDir = dir;
        am.setAnimation(dir);
    }

    /**
     * Updates every tick and moves an Entity if not on the tile map grid
     */
    public void handleMovement(float dt) {
        // down
        if (moving == 0) {
            int targetY = nextPosition(0);
            if (targetY == currentTileY) {
                moving = -1;
            } else {
                position.y -= speed * dt;
                if (Math.abs(position.y - targetY * tileMap.tileSize) <= speed * dt) {
                    position.y = targetY * tileMap.tileSize;
                    moving = -1;
                }
            }
        }
        // up
        if (moving == 1) {
            int targetY = nextPosition(1);
            if (targetY == currentTileY) {
                moving = -1;
            } else {
                position.y += speed * dt;
                if (Math.abs(position.y - targetY * tileMap.tileSize) <= speed * dt) {
                    position.y = targetY * tileMap.tileSize;
                    moving = -1;
                }
            }
        }
        // right
        if (moving == 2) {
            int targetX = nextPosition(2);
            if (targetX == currentTileX) {
                moving = -1;
            } else {
                position.x += speed * dt;
                if (Math.abs(position.x - targetX * tileMap.tileSize) <= speed * dt) {
                    position.x = targetX * tileMap.tileSize;
                    moving = -1;
                }
            }
        }
        // left
        if (moving == 3) {
            int targetX = nextPosition(3);
            if (targetX == currentTileX) {
                moving = -1;
            } else {
                position.x -= speed * dt;
                if (Math.abs(position.x - targetX * tileMap.tileSize) <= speed * dt) {
                    position.x = targetX * tileMap.tileSize;
                    moving = -1;
                }
            }
        }
    }

    /**
     * Increments level and recalculates max exp
     * Sets increase variables to display on screen
     * Recursively accounts for n consecutive level ups from remaining exp
     *
     * @param remainder the amount of exp left after a level up
     */
    public void levelUp(int remainder) {
        level++;

        hpIncrease += MathUtils.random(Util.PLAYER_MIN_HP_INCREASE, Util.PLAYER_MAX_HP_INCREASE);

        int prevMaxExp = maxExp;
        maxExp = Util.calculateMaxExp(level, MathUtils.random(3, 5));
        maxExpIncrease += (maxExp - prevMaxExp);

        // another level up
        if (remainder >= maxExp) {
            levelUp(remainder - maxExp);
        } else {
            exp = remainder;
        }
    }

    /**
     * Increases the actual stats by their level up amounts
     */
    public void applyLevelUp() {
        maxHp += hpIncrease;
        hp = maxHp;

        // reset variables
        hpIncrease = 0;
        maxExpIncrease = 0;
    }

    /**
     * REMOVED: Inventory system disabled
     * Applies the stats of an equipable item
     *
     * @param item
     */
    /*public void equip(Item item) {
        maxHp += item.mhp;
        hp = maxHp;
    }*/

    /**
     * REMOVED: Inventory system disabled
     * Removes the stats of an equipable item
     *
     * @param item
     */
    /*public void unequip(Item item) {
        maxHp -= item.mhp;
        hp = maxHp;
    }*/

    public Enemy getOpponent() {
        return opponent;
    }

    public void finishBattling() {
        battling = false;
        opponent = null;
        moving = -1;
    }

    public void finishTileInteraction() {
        tileInteraction = false;
        moving = -1;
    }

    /**
     * After teleportation is done the player is moved out of the tile in a random direction
     */
    public void finishTeleporting() {
        teleporting = false;
        changeDirection(MathUtils.random(3));
    }

    public void potion(int heal) {
        hp += heal;
        if (hp > maxHp) hp = maxHp;
    }

    /**
     * Applies a percentage health potion
     * @param php
     */
    public void percentagePotion(int php) {
        hp += (int) ((php / 100f) * maxHp);
        if (hp > maxHp) hp = maxHp;
    }

    /**
     * The purple exclamation mark tile is a destructive tile
     * that has a 60% chance to do damage to the player and
     * 40% chance to steal gold.
     *
     * @param mapLevel
     * @return
     */

    /**
     * Sets the player's position to another teleportation tile anywhere on the map
     */
    public void teleport() {
        Tile currentTile = tileMap.getTile(tileMap.toTileCoords(position));
        Array<Tile> candidates = tileMap.getTeleportationTiles(currentTile);
        Tile choose = candidates.get(MathUtils.random(candidates.size - 1));
        position.set(tileMap.toMapCoords(choose.tilePosition));
    }

    /**
     * Simple dialog for question mark tiles
     */
    public String[] getQuestionMarkDialog(int avgLevel, Object gameMap) {
        return new String[]{"You found something interesting!", "This is a question mark tile."};
    }

    /**
     * Simple dialog for exclamation mark tiles  
     */
    public String[] getExclamDialog(int avgLevel, Object gameMap) {
        return new String[]{"Warning!", "This is an exclamation mark tile."};
    }

    /**
     * Adds a given amount of exp to the player's current exp and checks for level up
     */
    public void addExp(int exp) {
        // level up with no screen
        if (this.exp + exp >= maxExp) {
            int remainder = (this.exp + exp) - maxExp;
            levelUp(remainder);
            applyLevelUp();
        }
        else if (this.exp + exp < 0) {
            this.exp = 0;
        }
        else {
            this.exp += exp;
        }
    }

    public boolean isBattling() {
        return battling;
    }

    /**
     * Skip enemy encounter - remove enemy and continue
     */
    public void skipEnemyEncounter() {
        if (battling && opponent != null) {
            // Remove enemy from tilemap using tile coordinates
            tileMap.removeEntity(tileMap.toTileCoords(position));
            // Reset battle state
            battling = false;
            opponent = null;
        }
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public void setMaxExp(int maxExp) {
        this.maxExp = maxExp;
    }

    public int getExp() {
        return exp;
    }

    public int getMaxExp() {
        return maxExp;
    }

    public int getHpIncrease() {
        return hpIncrease;
    }

    public void setHpIncrease(int hpIncrease) {
        this.hpIncrease = hpIncrease;
    }

    public int getMaxExpIncrease() { return maxExpIncrease; }

    public void addGold(int g) {
        if (this.gold + g < 0) this.gold = 0;
        else this.gold += g;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getGold() { return gold; }

    public int getCurrentTileX() {
        return currentTileX;
    }

    public int getCurrentTileY() {
        return currentTileY;
    }

    public boolean isMoving() {
        return moving != -1;
    }

    public boolean isTileInteraction() { return tileInteraction; }

    public boolean isTeleporting() { return teleporting; }

}
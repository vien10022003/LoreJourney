package com.loreJourney.save;

import com.loreJourney.entity.Player;
// REMOVED: Inventory system disabled
// import com.loreJourney.inventory.*;
import com.loreJourney.resource.Statistics;

/**
 * Provides a serializable object that represents the data of the player
 * that is considered important for save files.
 *
 * @author Ming Li
 */
public class PlayerAccessor  {

    // status fields
    public int hp;
    public int maxHp;
    public int level;
    public int exp;
    public int maxExp;
    public int gold;

    // level save
    public int maxWorld;
    public int maxLevel;

    // REMOVED: Inventory system disabled
    // inventory and equips consist of ItemAccessors to reduce unnecessary fields
    // public ItemAccessor[] inventory;
    // public ItemAccessor[] equips;

    // statistics
    public Statistics stats;

    // settings
    public Settings settings;

    public PlayerAccessor() {
        // REMOVED: Inventory system disabled
        // inventory = new ItemAccessor[Inventory.NUM_SLOTS];
        // equips = new ItemAccessor[Equipment.NUM_SLOTS];
    }

    /**
     * Updates the fields of this accessor with data from the player
     * @param player
     */
    public void load(Player player) {
        // load atomic fields
        this.hp = player.getHp();
        this.maxHp = player.getMaxHp();
        this.level = player.getLevel();
        this.exp = player.getExp();
        this.maxExp = player.getMaxExp();
        this.gold = player.getGold();
        this.maxWorld = player.maxWorld;
        this.maxLevel = player.maxLevel;

        // REMOVED: Inventory system disabled
        /*// load inventory and equips
        for (int i = 0; i < Inventory.NUM_SLOTS; i++) {
            if (!player.inventory.isFreeSlot(i)) {
                Item item = player.inventory.getItem(i);
                if (item instanceof ShopItem) {
                    ShopItemAccessor sia = new ShopItemAccessor();
                    sia.load((ShopItem) item);
                    inventory[i] = sia;
                }
                else {
                    ItemAccessor ia = new ItemAccessor();
                    ia.load(item);
                    inventory[i] = ia;
                }
            }
            else {
                inventory[i] = null;
            }
        }
        for (int i = 0; i < Equipment.NUM_SLOTS; i++) {
            if (player.equips.getEquipAt(i) != null) {
                Item equip = player.equips.getEquipAt(i);
                if (equip instanceof ShopItem) {
                    ShopItemAccessor sia = new ShopItemAccessor();
                    sia.load((ShopItem) equip);
                    equips[i] = sia;
                }
                else {
                    ItemAccessor ia = new ItemAccessor();
                    ia.load(equip);
                    equips[i] = ia;
                }
            }
            else {
                equips[i] = null;
            }
        }*/

        // statistics
        this.stats = player.stats;

        // settings
        this.settings = player.settings;
    }

}

package com.loreJourney.ui.battleui;

import com.loreJourney.entity.Player;
import com.loreJourney.event.Battle;
import com.loreJourney.map.TileMap;
import com.loreJourney.resource.ResourceManager;
import com.loreJourney.screen.GameScreen;
import com.loreJourney.ui.UI;

/**
 * Superclass for all UI related to battle events
 *
 * @author Ming Li
 */
public abstract class BattleUI extends UI {

    protected Battle battle;
    protected BattleUIHandler uiHandler;

    public BattleUI(GameScreen gameScreen, TileMap tileMap, Player player, Battle battle,
                    BattleUIHandler uiHandler, ResourceManager rm) {
        super(gameScreen, tileMap, player, rm);
        this.battle = battle;
        this.uiHandler = uiHandler;
    }

}

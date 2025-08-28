package com.loreJourney.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.math.Vector2;
import com.loreJourney.entity.Player;

public class Save {
    private Player player;
    private String filename;
    
    public Save(Player player, String filename) {
        this.player = player;
        this.filename = filename;
    }
    
    public void save() {
        saveData();
    }
    
    public void load(Object rm) {
        loadData();
    }
    
    public void saveData() {
        try {
            Json json = new Json();
            PlayerData data = new PlayerData();
            data.x = player.getPosition().x;
            data.y = player.getPosition().y;
            data.level = player.getLevel();
            data.exp = player.getExp();
            data.hp = player.getHp();
            data.maxHp = player.getMaxHp();
            data.gold = player.getGold();
            data.maxWorld = player.maxWorld;
            data.maxLevel = player.maxLevel;
            
            FileHandle file = Gdx.files.local(filename);
            file.writeString(json.toJson(data), false);
        } catch (Exception e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
    
    public void loadData() {
        try {
            FileHandle file = Gdx.files.local(filename);
            if (file.exists()) {
                Json json = new Json();
                PlayerData data = json.fromJson(PlayerData.class, file.readString());
                
                player.setPosition(new Vector2(data.x, data.y));
                player.setLevel(data.level);
                player.setExp(data.exp);
                player.setHp(data.hp);
                player.setMaxHp(data.maxHp);
                player.setGold(data.gold);
                player.maxWorld = data.maxWorld;
                player.maxLevel = data.maxLevel;
            }
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }
    
    public static class PlayerData {
        public float x, y;
        public int level, exp, hp, maxHp, gold, maxWorld, maxLevel;
    }
}

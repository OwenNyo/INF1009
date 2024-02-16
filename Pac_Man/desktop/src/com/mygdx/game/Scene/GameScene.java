package com.mygdx.game.Scene;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mygdx.game.GameMaster;
import com.mygdx.game.Engine.Collectible;
import com.mygdx.game.Engine.CollisionManager;
import com.mygdx.game.Engine.Entity;
import com.mygdx.game.Engine.EntityManager;
import com.mygdx.game.Engine.Ghost;
import com.mygdx.game.Engine.IOManager;
import com.mygdx.game.Engine.Player;
import com.mygdx.game.Engine.Score;

public class GameScene extends ScreenAdapter{
    
    private Player player;
    private Ghost ghost;
    private Collectible collectibles[];
    private Score score;
    private List<Entity> entityList;
    private EntityManager entityManager;
    
    private IOManager ioManager;
    private CollisionManager cManager;
    private GameMaster gameMaster;

    public GameScene(GameMaster gameMaster, SceneManager sceneManager) {
    	this.gameMaster = gameMaster;
        // Initialize EntityManager and entities
        entityManager = new EntityManager();
        entityManager.initEntities();
        
        // Fetch Entity List
        entityList = entityManager.getEntityList();
        
        // Initialize Scoring System
        score = new Score();

        // Initialize Collision Manager
        cManager = new CollisionManager();
        // Initialize IO Manager
        ioManager = new IOManager();
    }

    @Override
    public void render(float delta) {
        
        // This line is used to ensure that the screen is blank and set to a dark blue background
        ScreenUtils.clear(0 , 0, 0.2f, 1); 

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Texture Drawing 
        score.draw();
        entityManager.drawEntities();
        entityManager.moveEntities();
    
        // Create a new list to hold the collectible
        List<Collectible> collectibleList = new ArrayList<>();

        // Loop through the entity list to find the entities
        for (Entity entity : entityList) {
            if (entity instanceof Collectible) {
                collectibleList.add((Collectible) entity);
            }
            if (entity instanceof Player) {
                player = (Player) entity;
            } else if (entity instanceof Ghost) {
                ghost = (Ghost) entity;
            }
        }
        
        // Convert the list of collectible back to an array
        collectibles = collectibleList.toArray(new Collectible[collectibleList.size()]);

        if (cManager.checkCollectibleCollision(player, collectibles)) {
            score.calculateScore();
        }

        // Check ghost collision
        if (player != null && ghost != null) {
            cManager.checkGhostCollision(player, ghost);
            player.drawRemainingHealth();
        }

        // Play BG music on start
        ioManager.playBG();
    }

    @Override
    public void dispose() {
        // Properly dispose textures
        entityManager.disposeEntities();
    }
}
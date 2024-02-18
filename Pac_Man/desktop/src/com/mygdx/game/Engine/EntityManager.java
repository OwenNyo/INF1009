package com.mygdx.game.Engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class EntityManager {
	
	// Class Attributes
	private List<Entity> entityList;
	private SpriteBatch batch;
	private ShapeRenderer shape;
	
	// Object Declaration
	private Player player;
	private Ghost ghost;
	private Collectible collectibles[];

	// Static Attributes
	private int MAXPELLET = 5;
	
	// Ghost Attributes
	private int GhostSpeed = 10;
	private int GhostDamage = 10;
	
	// Player Attributes
	private float Player1SpawnX = 400;
	private float Player1SpawnY = 300;
	private float PlayerSpeed = 300;
	private int PlayerHealth = 100;
	private int PlayerPoints = 0;
	
	// Manager Attributes
	
	
	// Initialize all Entities
	public void initEntities() {
		
		// Create Array List
		entityList = new ArrayList<>();
		batch = new SpriteBatch();
    	shape = new ShapeRenderer();
		
		// Create Player Object
		player = new Player("player", "PacMan.png", Player1SpawnX, Player1SpawnY
				, PlayerSpeed, PlayerHealth, PlayerPoints, 50, 50, false, true);
		
		// Create Ghost Object
		ghost = new Ghost("ghost", "ghost.png", 0, 0, GhostSpeed, GhostDamage, 60, 60, true);
		ghost.GenerateSpawnPoint(player.getX(), player.getY());
		
		// Create Collectible Objects
		collectibles = new Collectible[MAXPELLET];
	    Random random = new Random();
		
		for (int i = 0; i < collectibles.length; i++) {
            float randomX = random.nextInt(Gdx.graphics.getWidth());
            float randomY = random.nextInt(Gdx.graphics.getHeight());

            collectibles[i] = new Collectible("collectible", randomX, randomY, Color.RED, 15, false);
            collectibles[i].setShape(shape);
           entityList.add(collectibles[i]);
        }	
		
		// Add entities to the list
		entityList.add(player);
		entityList.add(ghost);
       
	}
	
	public List<Entity> getEntityList() {
	    return entityList;
	}
	

	// Draw all Entities
	public void drawEntities() {
		for (Entity entity : entityList) {
			entity.draw();
		}
	}
	
	// Movement logic for all Entities
	public void moveEntities() {
        for (Entity entity : entityList) {
            if (entity instanceof Player) {
                PlayerControlManager playerControlManager = new PlayerControlManager((Player) entity);
                playerControlManager.handleMovement();
            } else if (entity instanceof Ghost) {
                AIManager aiManager = new AIManager((Ghost) entity, player.getX(), player.getY()); // Pass player coordinates
                aiManager.handleMovement();
            }
        }
    }
	
	// Get Entity from Entity List
	public List<Collectible> getCollectibles() {
	    List<Collectible> collectibleList = new ArrayList<>();
	    for (Entity entity : entityList) {
	        if (entity instanceof Collectible) {
	            collectibleList.add((Collectible) entity);
	        }
	    }
	    return collectibleList;
	}

	public Player getPlayer() {
	    for (Entity entity : entityList) {
	        if (entity instanceof Player) {
	            return (Player) entity;
	        }
	    }
	    return null; // Return null if player is not found
	}

	public Ghost getGhost() {
	    for (Entity entity : entityList) {
	        if (entity instanceof Ghost) {
	            return (Ghost) entity;
	        }
	    }
	    return null; // Return null if ghost is not found
	}

	public Collectible[] getCollectiblesArray() {
	    List<Collectible> collectibleList = getCollectibles();
	    return collectibleList.toArray(new Collectible[0]);
	}
    
    
	// Dispose of all Entities
	public void disposeEntities() {
        batch.dispose();
        shape.dispose();
        for (Entity entity : entityList) {
        	if (entity.getTex() != null) {
        		entity.getTex().dispose();
            }  
        }
    }
	
	public Ghost gameOverDispose() {
    	for (Entity entity : entityList) {
        	if (entity.getTex() != null) {
        		entity.getTex().dispose();
            }   
        }
        return null; // Return null if ghost is not found
    }

}

package com.mygdx.game.Scene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.mygdx.game.GameMaster;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.mygdx.game.Engine.Collectible;
import com.mygdx.game.Engine.CollisionManager;
import com.mygdx.game.Engine.Entity;
import com.mygdx.game.Engine.EntityManager;
import com.mygdx.game.Engine.Enemy;
import com.mygdx.game.Engine.IOManager;
import com.mygdx.game.Engine.Player;
import com.mygdx.game.Engine.SceneManager;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.Engine.HUD;
import com.mygdx.game.Engine.TimerClass;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameScene extends ScreenAdapter {

    // Entities
    private Player player;
    private Enemy enemy;
    private Collectible collectibles[], asteroids[];
    private List<Entity> entityList;

    // Initialize Managers and classes
    private EntityManager entityManager;
    private IOManager ioManager;
    private CollisionManager cManager;
    private SceneManager sceneManager;
    private GameMaster gameMaster;
    
    // Static Tables
    private Label BlackholeLabel, AsteroidLabel, AstronautLabel, StageLabel;
    private Table BHTable, AsteroidTable, ATable, StageTable;
    private AtomicBoolean showingBlackholePopup;
    private AtomicBoolean showingAsteroidPopup;
    private AtomicBoolean showingAstronautPopup;
    private AtomicBoolean showingStagePopup;

    // Texture and Drawings
    private Texture backgroundTexture, astronautVeteran, astronautCommander, astronautMaster;
    private SpriteBatch batch;
    private HUD hud;
    private TimerClass timer;
    private Stage overlayStage;
    private OrthographicCamera camera;
    private float backgroundSpeed = 6;
    
    // Variable to track if the player collided with an asteroid before
    private boolean firstAsteroidCollision = true;
    private boolean achievementUnlock = true;
    
    // Variable to track if second stage of game is initialized
    private boolean isSecondStageInitialized = false;
    
    // Game State management
    public enum GameState {
        RUNNING,
        PAUSED,
        GAME_OVER;
    }
    
    // Initialize current game state to running
    private GameState gameState = GameState.RUNNING;

    public GameScene(GameMaster gameMaster, SceneManager sceneManager) {
        this.gameMaster = gameMaster;
        this.sceneManager = sceneManager;
        
        // Create Instances
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        overlayStage = new Stage(new ScreenViewport());
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        // Class & Manager Initialization
        hud = new HUD();
        timer = new TimerClass();
        cManager = new CollisionManager();
        ioManager = new IOManager();
        entityManager = new EntityManager();
        entityManager.initEntities(1);
        entityList = entityManager.getEntityList();
        
        // Load background texture
        backgroundTexture = new Texture("space.jpg");  
        astronautVeteran = new Texture("astronaut.png");  
        astronautCommander = new Texture("astronautCommander.png");
        astronautMaster = new Texture("astronautRocket.png");

        // Initialize Labels & Tables
        Skin skin = new Skin(Gdx.files.internal("freezing-ui.json")); 
        
        BHTable = new Table();
        AsteroidTable = new Table();
        
        BlackholeLabel = new Label("Welcome New Astronaut!\n Let's Discover the Space Together!", skin);
        BlackholeLabel.setAlignment(Align.center);
        BlackholeLabel.setFontScale(2.5f);
        
        AsteroidLabel = new Label("You collided with an asteroid!!!", skin);
        AsteroidLabel.setAlignment(Align.center);
        AsteroidLabel.setFontScale(2.5f);
        
        BHTable = new Table();
        BHTable.setFillParent(true);
        BHTable.add(BlackholeLabel).expand().center();
        
        AsteroidTable = new Table();
        AsteroidTable.setFillParent(true);
        AsteroidTable.add(AsteroidLabel).expand().center();
        
        AstronautLabel = new Label("You've become a \n Astronaut Veteran!!!", skin);
        AstronautLabel.setAlignment(Align.right);
        AstronautLabel.setFontScale(2.5f);
        
        ATable = new Table();
        ATable.setFillParent(true);
        ATable.add(AstronautLabel).expand().bottom().right();
        
        StageLabel = new Label("Stage over!", skin);
        StageLabel.setAlignment(Align.right);
        StageLabel.setFontScale(2.5f);
        
        StageTable = new Table();
        StageTable.setFillParent(true);
        StageTable.add(StageLabel).expand().center();
        
        // Initialize popup visibility
        // Set true explicitly
        
        // Initialize popup visibility
        showingBlackholePopup = new AtomicBoolean(true);
        showingAsteroidPopup = new AtomicBoolean(false);
        showingAstronautPopup = new AtomicBoolean(false);
        showingStagePopup = new AtomicBoolean(false);

        // Schedule tasks to hide the popups after 3 seconds
        timer.timerCountdown(3, showingBlackholePopup);    
        
    }

    @Override
    public void render(float delta) {
        // Clear screen with dark blue background
        ScreenUtils.clear(0 , 0, 0.2f, 1); 

        // Clear OpenGL color buffer
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Update camera position based on background scrolling speed
        camera.position.x += backgroundSpeed * Gdx.graphics.getDeltaTime();
        
        
        // Render game elements based on game state
        switch (gameState) {
        case RUNNING:
            renderRunningState();
            break;
        case GAME_OVER:
            renderGameOverState();
            break;
        case PAUSED:
            // Keep the game in its current visual state, but do not update entities
            overlayStage.act(delta);
            overlayStage.draw();
            break;
        }
        
        camera.update();
        
        // Draw Pop Up Messages
        if (showingBlackholePopup.get()) {
            overlayStage.addActor(BHTable);
        } else {
            BHTable.remove(); // Remove the table if the popup is not showing
        }
        
        if (showingAsteroidPopup.get()) {
            overlayStage.addActor(AsteroidTable);
        } else {
            AsteroidTable.remove(); // Remove the table if the popup is not showing
        }
        
        if (showingAstronautPopup.get()) {
            overlayStage.addActor(ATable);
        } else {
        	ATable.remove(); // Remove the table if the popup is not showing
        }
        
        if (showingStagePopup.get()) {
        	overlayStage.addActor(StageTable);
        } else {
        	StageTable.remove();
        }
        
        overlayStage.act(delta);
        overlayStage.draw();
        
    }
    
    // Render game elements when game state is RUNNING
    private void renderRunningState() {
        // Draw background texture
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth() * 2 , Gdx.graphics.getHeight());
        batch.end();

        // Draw timer and entities
        timer.draw();
        timer.resumeTimer();
        entityManager.drawEntities();
        entityManager.moveEntities();
        
        // Get Entities Instances
        player = entityManager.getPlayer();
        enemy = entityManager.getEnemy();
        collectibles = entityManager.getCollectiblesArray();
        asteroids = entityManager.getAsteroidArray();

        // Update and draw score
        hud.drawScore(player.getPoints());
        
        if (player.getPoints() > 200 && player.getPoints() < 400) {
            AstronautLabel.setText("You've become a \n Rookie Astronaut!!!");
            if (!achievementUnlock) {
                player.setTex(astronautVeteran);
                showingAstronautPopup.set(true);
                timer.timerCountdown(4, showingAstronautPopup);
                achievementUnlock = true; // Indicate that an achievement has been unlocked
            }
        } else if (player.getPoints() >= 400 && player.getPoints() < 700) {
            if (!achievementUnlock) {
                AstronautLabel.setText("You've become a \n Astronaut Commander!!!");
                player.setTex(astronautCommander);
                showingAstronautPopup.set(true);
                timer.timerCountdown(4, showingAstronautPopup);
                achievementUnlock = true;
            }
        } else if (player.getPoints() >= 700) {
            if (!achievementUnlock) {
                AstronautLabel.setText("You've become a \n Master Astronomer!!!");
                player.setTex(astronautMaster);
                showingAstronautPopup.set(true);
                timer.timerCountdown(4, showingAstronautPopup);
                achievementUnlock = true;
            }
        }

        // After displaying any achievement and starting the countdown, reset the flag
        if (achievementUnlock) {
            achievementUnlock = false; // Ensure this is set after the popup logic
        }

        // Check for player collision with asteroids
        if (cManager.checkCollectibleCollision(player, asteroids)) {
            // Handle collectible collision
        	// Handle asteroid collision
        	if (firstAsteroidCollision) {
                // Handle asteroid collision
        		// Set true explicitly
                showingAsteroidPopup.set(true);
                
                // Schedule tasks to hide the popups after 2 seconds
                timer.timerCountdown(2, showingAsteroidPopup);
                // Set to false after the first collision
                firstAsteroidCollision = false; 
            }
        }
        
        // Check for player collision with planets
        if (cManager.checkCollectibleCollision(player, collectibles)) {
        	player.PlayerScorePoints(110);
        	
        	// Set screen to planet fun fact scene
            sceneManager.setPlanetScreen(cManager.getLastCollidedPlanetName());
            
        	// Remove collided planet from collectibles array
        	entityManager.removePlanetFromCollectibles(cManager.getLastCollidedPlanetName());
        	
        	if (entityManager.countRemainingPlanets() == 0) {
                // Pause game when player collides with a planet
                gameState = GameState.PAUSED;
                
                // Stop BG music
                ioManager.stopBG();
                
                // Pause Timer
                timer.pauseTimer();
        		
                StageLabel.setText("Current Stage over!");
                showingStagePopup.set(true);
                timer.timerCountdown(3, showingStagePopup);
                
                // Set the timer to 6 seconds if all planets are collected
        		timer.setTime(6); 
        	} else {
                // Pause game when player collides with a planet
                gameState = GameState.PAUSED;
                
                // Stop BG music
                ioManager.stopBG();
                
                // Pause Timer
                timer.pauseTimer();
        	}
        }

        // Check for collision with enemy
        if (player != null && enemy != null) {
            int remainingHealth = player.getHealth();
            if (remainingHealth > 0) {
                cManager.checkEnemyCollision(player, enemy);
                hud.drawRemainingHealth(player.getHealth());
            } else {
                gameState = GameState.GAME_OVER;
            }
        }
        
        // Timer Over change to scene 2
		if(timer.getTime() == 0 && !isSecondStageInitialized) {
			timer.setTime(30);
	        entityManager.disposeEntities();
	        entityManager.initEntities(2);
	        camera.position.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0);
	        isSecondStageInitialized = true;
		}
		else if(timer.getTime() == 0 && isSecondStageInitialized) {
            gameState = GameState.GAME_OVER;
		}

        // Play background music
		if (gameState != GameState.PAUSED) {
			ioManager.playBG();
		}
    }
    
    // Render game elements when game state is GAME_OVER
    private void renderGameOverState() {
        // Dispose resources and switch to end screen
        backgroundTexture.dispose();
        entityManager.disposeEntities();
        ioManager.dispose();
        hud.dispose();
        overlayStage.dispose();
        AsteroidLabel.clear();
        BlackholeLabel.clear();
        sceneManager.setEndScreen(player.getPoints());
    }
    
    // Method to update game state
    public void updateGameState(GameState newState) {
        this.gameState = newState;
    }
        
    @Override
    public void dispose() {
        // Properly dispose of textures
        backgroundTexture.dispose();
        entityManager.disposeEntities();
        ioManager.dispose();
        hud.dispose();
        overlayStage.dispose();
        AsteroidLabel.clear();
        BlackholeLabel.clear();
        StageLabel.clear();
    }
    
}

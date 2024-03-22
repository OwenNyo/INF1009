package com.mygdx.game.Scene;

import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.mygdx.game.GameMaster;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.mygdx.game.Engine.IOManager;
import com.mygdx.game.Engine.Player;
import com.mygdx.game.Engine.SceneManager;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.Engine.HUD;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public class TriviaScene extends ScreenAdapter {
	
	// Stage Drawn
	private Stage stage;
	private SpriteBatch batch;
    private BitmapFont scoreFont;
    private Texture background;
	private HUD hud;
	
	//Manager 
	private GameMaster gameMaster;
	private SceneManager sceneManager;
	private IOManager ioManager;
	
	// Player
	private Player player;
	private Timer questionTimer;
	private Timer answerTimer;

    // Quiz 
    private String[][] planetsList;
    private String answer;
    private String answerText;
    private boolean showAnswer = false;
    
    // Static Variables
    private int finalScore; 
    private int currentBackgroundIndex = 0; 
    
    // Buttons
    private TextButton TrueButton;
    private TextButton FalseButton;
    
    public TriviaScene(GameMaster gameMaster, SceneManager sceneManager, Player player) {
    	this.gameMaster = gameMaster;
    	this.sceneManager = sceneManager;
    	this.player = player; 
    	
    	// Create a new SpriteBatch instance
        batch = new SpriteBatch();
        hud = new HUD();
        questionTimer = new Timer();
        answerTimer = new Timer();
        
        // Button Initialization
        // Load atlas file to create skin for UI elements
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("freezing-ui.atlas"));
        Skin skin = new Skin(Gdx.files.internal("freezing-ui.json"), atlas);
        TextButton.TextButtonStyle buttonStyle = skin.get("default", TextButton.TextButtonStyle.class);
        TrueButton = new TextButton("True", buttonStyle);
        FalseButton = new TextButton("False", buttonStyle);
        
        // Class & Manager Initialization
        ioManager = new IOManager();
          
        // Questions list and answers 
        String[][] planetsListlocal = {
            {"quiz/earthquiz.png", "False", "Water covers 71% of Earth surface."},
            {"quiz/venusquiz.png", "False", "Venus is similar to Earth size"},
            {"quiz/mecuryquiz.png", "True", " "},
            {"quiz/uranusquiz.png", "True", " "},
            {"quiz/jupiterquiz.png", "False", "Jupiter is the largest planet in our Solar System."},
            {"quiz/marsquiz.png", "True", " "},
            {"quiz/neptunequiz.png", "False", "Neptune is the farthest planet to the Sun."},
            {"quiz/moonquiz.png", "True", " "},
            {"quiz/saturnquiz.png", "True", " "},
        };
        
        planetsList = planetsListlocal;
       
        this.scoreFont = new BitmapFont();
        this.scoreFont.getData().setScale(3);
        
        finalScore = player.getPoints();

        
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);


        // Initialize button - "True" Option
        TrueButton.setPosition(Gdx.graphics.getWidth() /4 - TrueButton.getWidth() /4 ,
                                    Gdx.graphics.getHeight() /4 - TrueButton.getHeight() /2);
        // Inside True button click listener
        TrueButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (answer.equals("True")) {
                    ioManager.playSECollect();
                    finalScore += 100;
                } else {
                    ioManager.playSE();
                    showAnswer = true;
                }
                startQuestionTimer();
                
             // Hide buttons after answering
                TrueButton.setVisible(false);
                FalseButton.setVisible(false);
            }
        });
        
        
        // Initialize button - "False" Option
        FalseButton.setPosition(Gdx.graphics.getWidth() / 2 - FalseButton.getWidth() / 2,
                                   Gdx.graphics.getHeight() / 4 - FalseButton.getHeight() / 2);
        // Inside False button click listener
        FalseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (answer.equals("False")) {
                    ioManager.playSECollect();
                    finalScore += 100;
                } else {
                    ioManager.playSE();
                    showAnswer = true;
                }
                startQuestionTimer();
                
                // Hide buttons after answering
                TrueButton.setVisible(false);
                FalseButton.setVisible(false);
            }
        });

        // Add buttons to the stage
        stage.addActor(TrueButton);
        stage.addActor(FalseButton);
        
    }
    
    private void startQuestionTimer() {
    	// Initialize the timer if it's null
    	if (questionTimer == null) {
            questionTimer = new Timer();
        } else {
            // Cancel the previous task if it exists
            questionTimer.clear();
        }

        // Schedule the task for the next question after 5 seconds
        questionTimer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                // Switch to the next question
                nextQuestion();
             // Hide buttons after answering
                TrueButton.setVisible(true);
                FalseButton.setVisible(true);
            }
        }, 2); // Delay for 5 seconds
    }

    
    
    @Override
    public void render(float delta) {
    	Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        List<String[]> responses = askQuestion(planetsList);

        for (String[] response : responses) {
            background = new Texture(response[0]);
            answer = response[1];
            answerText = response[2];
            hud.drawBackground(background);
        }

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

        drawPlayerScore();

        // Draw the answer if it needs to be shown
        if (showAnswer) {
            drawAnswer(answerText);
        	
        	answerTimer.scheduleTask(new Timer.Task() {
                @Override
                public void run() {
                    // Switch to the next question
                    showAnswer = false;
                }
            }, 2);
        }
        
        
        
    }
    
    private void drawPlayerScore() {
        batch.begin();
        scoreFont.draw(batch, String.valueOf(finalScore), 600, 1000);
        batch.end();
    }
    
    private void drawAnswer(String a) {
        batch.begin(); 
        scoreFont.draw(batch, a, 1200, 300);
        batch.end();
    }
    
    // Method to load next question
    private void nextQuestion() {
        if (currentBackgroundIndex < 8) {
            currentBackgroundIndex++;
            List<String[]> responses = askQuestion(planetsList);
            for (String[] response : responses) {
                background = new Texture(response[0]);
                answer = response[1];
                answerText = response[2];
                System.out.println("Background: " + response[0]);
                System.out.println("Answer: " + answer);
                System.out.println("Answer Text: " + answerText);
                System.out.println();
            }
            hud.drawBackground(background);
        } else {
            sceneManager.setEndScreen(finalScore);
        }
    }
    
    
    public List<String[]> askQuestion(String[][] planetsList) {
        List<String[]> questionResponses = new ArrayList<>();
        
        // Retrieve question details 
        if (currentBackgroundIndex < planetsList.length) {
        	String[] question = planetsList[currentBackgroundIndex];
            String backgroundpath = question[0];
            String answer = question[1];
            String answerText = question[2];
            
            // Store in new list to pass to main
            String[] response = {backgroundpath, answer, answerText};
            questionResponses.add(response);
        }
        return questionResponses;
    }
    

    
    @Override
    public void dispose() {
        batch.dispose();
        scoreFont.dispose();
        background.dispose();
    }

}

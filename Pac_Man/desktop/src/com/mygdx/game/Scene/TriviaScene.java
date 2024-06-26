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
import com.mygdx.game.Engine.SceneManager;
import com.mygdx.game.Logic.HUD;
import com.mygdx.game.Logic.Player;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
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
    private boolean showWrongAnswer = false;
    private boolean showCorrectAnswer = false;
    
    // Static Variables
    private int finalScore; 
    private int currentBackgroundIndex = 0; 
    
    // Buttons
    private TextButton TrueButton;
    private TextButton FalseButton;
    
    public TriviaScene(GameMaster gameMaster, SceneManager sceneManager, Player player) {
    	// Initialize variables
    	this.gameMaster = gameMaster;
    	this.sceneManager = sceneManager;
    	this.player = player; 
    	
    	// Create a new SpriteBatch instance
        batch = new SpriteBatch();
        hud = new HUD();
        questionTimer = new Timer();
        answerTimer = new Timer();
        this.scoreFont = new BitmapFont();
        this.scoreFont.getData().setScale(3);
        
        // Set Stage
        this.stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        
        // Load atlas file to create skin for UI elements
        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("freezing-ui.atlas"));
        Skin skin = new Skin(Gdx.files.internal("freezing-ui.json"), atlas);
        TextButton.TextButtonStyle buttonStyle = skin.get("default", TextButton.TextButtonStyle.class);
        TrueButton = new TextButton("True", buttonStyle);
        FalseButton = new TextButton("False", buttonStyle);
        
        // Class & Manager Initialization
        ioManager = new IOManager();
         

        // Load Quiz Questions and Answers
        String[][] planetsListlocal = {
            {"quiz/earthquiz.png", "False", "Water covers 71% of Earth surface."},
            {"quiz/venusquiz.png", "False", "Venus is similar to Earth size."},
            {"quiz/mecuryquiz.png", "True", "Mecury is the smallest planet in our Solar System."},
            {"quiz/uranusquiz.png", "True", "Uranus has a cold and icy atmosphere."},
            {"quiz/jupiterquiz.png", "False", "Jupiter is the largest planet in our Solar System."},
            {"quiz/marsquiz.png", "True", "Mar's reddish appearance is caused by iron oxide (rust)."},
            {"quiz/neptunequiz.png", "False", "Neptune is the farthest planet to the Sun."},
            {"quiz/moonquiz.png", "True", "Space rocks form craters when it crashes into Moon's surface."},
            {"quiz/saturnquiz.png", "True", "Saturn's ring is made of ice and rock."},
        };
        planetsList = planetsListlocal;
        
        // Get score from player class 
        finalScore = player.getPoints();
        
        // Initialize button - "True" Option
        TrueButton.setPosition(Gdx.graphics.getWidth() /4 - TrueButton.getWidth() /4 ,
                                    Gdx.graphics.getHeight() /4 - TrueButton.getHeight() /2);
        // Inside True button click listener
        TrueButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (answer.equals("True")) {
                	// If correct answer is chosen, play "correct" sound effect and add score
                    ioManager.playSECollect();
                    finalScore += 50;
                    showCorrectAnswer = true;
                } else {
                	// If wrong answer is chosen, play "wrong" sound effect and show correct answer (answerText)
                    ioManager.playSE();
                    showWrongAnswer = true;
                }
                // Timer for each question 
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
                	// If correct answer is chosen, play "correct" sound effect and add score
                    ioManager.playSECollect();
                    finalScore += 50;
                    showCorrectAnswer = true;
                } else {
                	// If wrong answer is chosen, play "wrong" sound effect and show correct answer (answerText)
                    ioManager.playSE();
                    showWrongAnswer = true;
                }
                // Timer for each question 
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
    
    @Override
    public void render(float delta) {
    	// Clear OpenGL color buffer
    	Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Load questions
        List<String[]> responses = askQuestion(planetsList);

        // Adjust background accordingly
        for (String[] response : responses) {
            background = new Texture(response[0]);
            answer = response[1];
            answerText = response[2];
            hud.drawBackground(background);
        }

        // Draw Stage
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

        // Draw Player Score
        drawPlayerScore();

        // Draw the Asnwer for when they select correctly
        if (showCorrectAnswer) {
        	String isCorrect = "Correct Answer! ";
            answerText = isCorrect;
            drawAnswer(answerText);
            
            answerTimer.scheduleTask(new Timer.Task() {
                @Override
                public void run() {
                    showCorrectAnswer = false;
                    answerTimer.clear();
                }
            }, 2);
        }
        
        // Draw the Answer for when they select wrongly
        if(showWrongAnswer) {
        	String isWrong = "Wrong Answer! ";
        	answerText = isWrong + answerText;
        	drawAnswer(answerText);
        	
        	answerTimer.scheduleTask(new Timer.Task() {
                @Override
                public void run() {
                    showWrongAnswer = false;
                    answerTimer.clear();
                }
            }, 2);
        }
    }
    
    @Override
    public void dispose() {
        batch.dispose();
        scoreFont.dispose();
        background.dispose();
    }
    
    
    // Class Methods //
    
    private void startQuestionTimer() {
    	// Initialize the timer if it's null
    	if (questionTimer == null) {
            questionTimer = new Timer();
        } else {
            // Cancel the previous task if it exists
            questionTimer.clear();
        }

        // Schedule the task for the next question after 2 seconds
        questionTimer.scheduleTask(new Timer.Task() {
            @Override
            public void run() {
                // Switch to the next question
                nextQuestion();
             // Hide buttons after answering
                TrueButton.setVisible(true);
                FalseButton.setVisible(true);
            }
        }, 2); // Delay for 2 seconds
    }

    
    
    
    
    // Method to draw player score
    private void drawPlayerScore() {
        batch.begin();
        // Draw player score
        scoreFont.draw(batch, "Player Score: " + String.valueOf(finalScore), 50, 1050);
        batch.end();
    }
    
    // Answer display 
    private void drawAnswer(String a) {
        batch.begin(); 
        // change coordinates to show full text wxh
        scoreFont.draw(batch, a, 500, 300);
        batch.end();
    }
    
    // Load next question
    private void nextQuestion() {
    	// Each time nextQuestion called 
        if (currentBackgroundIndex < 8) {
            currentBackgroundIndex++;
            List<String[]> responses = askQuestion(planetsList);
            
            // Set question details
            for (String[] response : responses) {
                background = new Texture(response[0]);
                answer = response[1];
                answerText = response[2];
            }
            hud.drawBackground(background);
        } else {
            sceneManager.setEndScreen(finalScore);
        }
    }
    
    
    // Select which question to display
    public List<String[]> askQuestion(String[][] planetsList) {
        List<String[]> questionResponses = new ArrayList<>();
        
        // Retrieve list of question details 
        if (currentBackgroundIndex < planetsList.length) {
        	// Set planetsList[currentBackgroundIndex] as a new "question" list 
        	String[] question = planetsList[currentBackgroundIndex];
        	// Set question variables 
            String backgroundpath = question[0];
            String answer = question[1];
            String answerText = question[2];
            
            // Store in new list to pass to main
            String[] response = {backgroundpath, answer, answerText};
            questionResponses.add(response);
        }
        return questionResponses;
    }
    
}

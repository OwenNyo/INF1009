package com.mygdx.game.Engine;

public class Player extends Entity{

		/// Class Attributes
		private float Speed;
		private int Health;
		private boolean IsFirstPlayer;
		
		
		
		//	Constructors
		public Player() {
			this.Speed = 0;
			this.Health = 0;
		}
		
		// Player to earn points via pellet eating -> win cond = survive longer than other player while stacking
		public Player(String type, String filename, float x, float y, float speed, int health, float height, float width, boolean AIControlled, boolean fp) {
			super(type, filename, x, y, width, height, AIControlled);
			this.Speed = speed;
			this.Health = health;
			this.IsFirstPlayer = fp;
		}
		
		
		
		//	Getters and Setters
		public float getSpeed() {
			return Speed;
		}
		public void setSpeed(float speed) {
			Speed = speed;
		}
		public int getHealth() {
			return Health;
		}
		public void setHealth(int health) {
			Health = health;
		}
		
		public boolean isFirstPlayer() {
	        return IsFirstPlayer;
	    }
		
		
		// Class Methods
		public void PlayerPoints(int points) {
			System.out.println("Player <> earned points");
		}
		
		public void PlayerDamageTaken(int damage) {
			Health = getHealth();
			Health -= damage;
			
			if (Health > 0) {
				System.out.println("Damage Taken by Player, Health left:" + getHealth());
			}
			else {
				System.out.println("Player has lost all his health!");
			}
		}
}

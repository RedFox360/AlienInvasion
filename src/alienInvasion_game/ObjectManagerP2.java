package alienInvasion_game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.Timer;

public class ObjectManagerP2 implements ActionListener {
	public static BufferedImage image;
	public static boolean needImage = true;
	public static boolean gotImage = false;
	public static boolean scoreSet = false;
	public static boolean terminalSpawnAliens = true;
	public static boolean spawnPowerups = true;
	Car car;
	int speed;
	public static int score;
	public static int aliensKilled = 0;
	int aliensWhoGotAway = 0;
	public static Timer increaseSpeed;
	public static Timer alienSpawn;
	public static Timer powerupSpawn;
	public static Timer timeLimit;
	int secondsUntilGameOver = 0;
	Random random = new Random();
	ArrayList<Alien2> aliens = new ArrayList<Alien2>();
	ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	ArrayList<Alien> verticalAlien = new ArrayList<Alien>();
	ArrayList<TimerPowerup> powerups = new ArrayList<TimerPowerup>();

	public ObjectManagerP2(Car c) {
		car = c;
		alienSpawn = new Timer(1000, this);
		increaseSpeed = new Timer(20000, this);
		speed = 1;
		powerupSpawn = new Timer(15000, this);
		timeLimit = new Timer(100000, this);
		if (needImage) {
			loadImage("phase2background.jpg");
		}
	}

	public void start() {
		increaseSpeed.start();
		alienSpawn.start();
		powerupSpawn.start();
		timeLimit.start();
	}
	public void stop() {
		increaseSpeed.stop();
		alienSpawn.stop();
		powerupSpawn.stop();
		timeLimit.stop();
	}

	void addPowerup() {
		if (spawnPowerups) {
			powerups.add(new TimerPowerup(random.nextInt(AlienInvasion.WIDTH), random.nextInt(AlienInvasion.HEIGHT),
					100, 100));
		}
	}

	void setScore(int x) {
		scoreSet = true;
		score = x;
	}

	public void clearAll() {
		for (int i = aliens.size() - 1; i >= 0; i++) {
			aliens.remove(i);
		}
		for (int i = bullets.size() - 1; i >= 0; i++) {
			bullets.remove(i);
		}
	}

	int getScore() {
		return score;
	}

	int getSpeed() {
		return speed;
	}

	int getTime() {
		return 100 - secondsUntilGameOver;
	}

	public void draw(Graphics g) {
		if (gotImage) {
			g.drawImage(image, 0, 0, AlienInvasion.WIDTH, AlienInvasion.HEIGHT, null);
		} else {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, AlienInvasion.WIDTH, AlienInvasion.HEIGHT);
		}
		car.draw(g);
		for (Iterator<Alien2> iterator = aliens.iterator(); iterator.hasNext();) {
			iterator.next().draw(g);
		}
		for (Iterator<Bullet> iterator = bullets.iterator(); iterator.hasNext();) {
			iterator.next().draw(g);
		}
		for (Iterator<TimerPowerup> iterator = powerups.iterator(); iterator.hasNext();) {
			iterator.next().draw(g);
		}
	}

	public void update() {
		for (Alien2 alien : aliens) {
			alien.update(car);
			if (alien.x < 0) {
				alien.isActive = false;
				aliensWhoGotAway += 1;
			}
		}
		for (Bullet projectile : bullets) {
			projectile.update();
			if (projectile.x < 0 | projectile.y < 0 | projectile.x > AlienInvasion.WIDTH | projectile.y > AlienInvasion.HEIGHT) {
				projectile.isActive = false;
			}
		}
		for (TimerPowerup powerup : powerups) {
			powerup.update();
		}
		car.update();
		if (aliensWhoGotAway >= 20) {
			GamePanel.endText = "Too many Aliens slipped away!";
			car.isActive = false;
			GamePanel.currentState++;
		}
		if (aliensKilled >= 40) {
			car.isActive = false;
			GamePanel.currentState++;
			String[] options = { "Java", "Python", "C++", "C", "Swift", "Javascript", "Ruby", "Perl" };
			JOptionPane.showMessageDialog(null,
					"The alien leader sees you are a great warrior, but he will not leave Earth alone until you prove your intelligence.");
			int x = JOptionPane.showOptionDialog(null, "What is the best coding language?", "Click a button",
					JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE, null, options, null);
			if (x == 0 || x == 1) {
				JOptionPane.showMessageDialog(null, "You're correct!");
				GamePanel.currentState++;
			} else {
				GamePanel.endText = "You answered the question incorrectly";
			}
			aliensKilled = 0;
		}
		int counter;
		for (counter = 1; counter <= 10; counter++) {

		}
		checkCollision();
		purgeObjects();
	}

	public void purgeObjects() {
		for (int i = aliens.size() - 1; i >= 0; i--) {
			Alien2 alien = aliens.get(i);
			if (!alien.isActive) {
				aliens.remove(i);
			}
		}
		for (int i = bullets.size() - 1; i >= 0; i--) {
			Bullet bullet = bullets.get(i);
			if (!bullet.isActive) {
				bullets.remove(i);
			}
		}
		for (int i = powerups.size() - 1; i >= 0; i--) {
			TimerPowerup powerup = powerups.get(i);
			if (!powerup.isActive) {
				powerups.remove(i);
			}
		}
	}

	public void addAlien() {
		if (terminalSpawnAliens) {
			aliens.add(new Alien2(0, random.nextInt(AlienInvasion.WIDTH), 50, 50, speed));
		}
	}

	public void addBullet(Bullet b) {
		bullets.add(b);
	}

	public void checkCollision() {
		for (Alien2 alien : aliens) {
			if (car.collisionBox.intersects(alien.collisionBox)) {
				car.isActive = false;
				GamePanel.endText = "Your car was hit by an alien.";
				GamePanel.currentState = GamePanel.GAME3;
				alien.isActive = false;
			}
			for (Bullet projectile : bullets) {
				if (projectile.collisionBox.intersects(alien.collisionBox)) {
					projectile.isActive = false;
					alien.isActive = false;
					score += 1;
					aliensKilled += 1;
				}
			}
		}
		for (TimerPowerup powerup : powerups) {
			if (powerup.collisionBox.intersects(car.collisionBox)) {
				powerup.isActive = false;
				car.canShootUpAndDown = true;
				GamePanel.halfTimer();
			}
		}
	}

	void loadImage(String imageFile) {
		if (needImage) {
			try {
				image = ImageIO.read(this.getClass().getResourceAsStream(imageFile));
				gotImage = true;
			} catch (Exception e) {

			}
			needImage = false;
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (!GamePanel.gamePaused) {
			if (arg0.getSource() == increaseSpeed) {
				speed += 1;
			}
			if (arg0.getSource() == alienSpawn) {
				addAlien();
				secondsUntilGameOver += 1;
			}
			if (arg0.getSource() == powerupSpawn) {
				addPowerup();
			}
			if (arg0.getSource() == timeLimit) {
				car.isActive = false;
				GamePanel.endText = "You ran out of time!";
				GamePanel.currentState++;
			}
		}
	}

}

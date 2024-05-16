import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

//its a feature the all new text update soon to come to other images who needs pictures when you have text
//removed debug stuff 
class GameOfLife extends FlexiblePictureExplorer implements ImageObserver {
	/*
	 * default settings
	 */

	final int DEFAULT_PERIOD = 200;

	/*
	 * Timers
	 */
	private Timer turnLoop = new Timer(true);
	private TimerTask nextTurn = null;
	private boolean running = false;
	/*
	 * Active settings
	 */
	private long periodMilis = DEFAULT_PERIOD;
	// timerMode is false if it is on pause and true if it is auto next turn
	private boolean timerMode = false;
	// yesTimer is true for automatic nextTurn false for manual
	private boolean yesTimer = true;
	/*
	 * bucha other stuff
	 */
	/*
	 * which screen it is relates to which stuff to present and how to interact
	 * Options: mainScreen, tutorial, deadtutorial, alivetutorial, walltutorial,
	 * randomtutorial, level, levelChooser
	 * note: level is for testing purposes only will not be used for actual levels
	 */
	private String screenName = "mainScreen";
  private String pastScreenName = null;
	// idk
	private JFrame mainFrame = null;
	private JFrame settingsPopup;
	// dimsions of the screen and stuff based on it
	private int rows;
	private int cols;
	private int animationRow = 0;
	private int animationCol = 0;
	private int trueNumberOfCols;
	private int halftNumberOfCols;
	private int pictureRows;
	private int pictureCols;
	// number status which block place down
	private int numberStatus = 0;
	// which color of block to put on main scren
	private int animationStatus = 0;
	// all the pictures
	private final Picture next = new Picture("images/GREENNEXT.png"), sandbox = new Picture("images/SANDBOX.png"),
			tutorial = new Picture("images/TUTORIAL.png"), back = new Picture("images/BIDABACK.png"),
			leftBack = new Picture("images/LEFTBACK.png"), rightBack = new Picture("images/RIGHTBACK.png"),
			trash = new Picture("images/TRASH.png"), dead = new Picture("images/DEAD.png"),
			alive = new Picture("images/ALIVE.png"), wall = new Picture("images/WALL.png"),
			random = new Picture("images/RANDOM.png"), random50 = new Picture("images/RANDOM50.png"),
			dead50 = new Picture("images/DEAD50.png"), alive50 = new Picture("images/ALIVE50.png"),
			wall50 = new Picture("images/WALL50.png"), level = new Picture("images/LEVEL.png"),
			down = new Picture("images/BIDADOWN.png"), up = new Picture("images/BIDAUP.png"),
			red = new Picture("images/RED.png"), orange = new Picture("images/ORANGE.png"),
			yellow = new Picture("images/YELLOW.png"), green = new Picture("images/GREEN.png"),
			blue = new Picture("images/BLUE.png"), purple = new Picture("images/PURPLE.png"),
			brown = new Picture("images/BROWN.png"), black = new Picture("images/BLACK.png"),
			pause = new Picture("images/PAUSE.png"), nextBlue = new Picture("images/NEXT.png"),
			white = new Picture("images/WHITE.png"), border = new Picture("images/border.png"),
			tutAlive = new Picture("images/TUTALIVE.png"), leftLevel = new Picture("images/LEVELLEFT.png"),
			rightLevel = new Picture("images/LEVELRIGHT.png"), tutWall = new Picture("images/TUTWALL.png"),
			tutRandom = new Picture("images/TUTRANDOM.png"), glider = new Picture("images/GLIDER.png"),
      statusAD = new Picture("images/STATUSAD.png"), settings50 = new Picture("images/settings50.png");
	// some other pictures that are for some reason 60 by 60
	private final File HOME = new File("images/home.png"), SETTINGS = new File("images/settings.png");
	// arrays with pictures for certain uses
	private final Picture[] status = { dead, alive, wall, random, tutAlive, tutWall, tutRandom};
	private final Picture[] tutorialIcons = { dead50, alive50, wall50, random50, glider };
	private final Picture[] animationIcons = { white, red, brown, orange, yellow, green, blue, purple, black };
	// display
	private Picture disp;
	// graphics
	private Graphics2D graphics;
	// current game in sandbox mode
	private Game currentGame,
				levelZero;
	private int levelNumber = 0;
	// picture size of one block
	private final int pictureSize = 20;
	// sleection true for all choices and selector false for just making dead alive
	// and alive dead
	private boolean selection = true;
	// color of the text
	private final Color stringColor = Color.CYAN;
	// background color
	private final Color backgroundColor = Color.DARK_GRAY;
	// idk
	private static final boolean SELFINIT = false;
	public GameOfLife(int inputRows, int inputCols, String inputScreenName) {
 
		/*
		 * setting up main screen
		 */
		super(new Picture(inputRows, inputCols + 50), SELFINIT);
		mainFrame = getFrame();

		createInfoPanel();

		mainFrame.pack();
		mainFrame.setVisible(true);

		/*
		 * setting the boards
		 */
		rows = inputRows;
		cols = inputCols;
		trueNumberOfCols = cols + 50;
		halftNumberOfCols = trueNumberOfCols / 2;
		pictureRows = (rows / pictureSize);
		pictureCols = (cols / pictureSize);
		disp = new Picture(rows, cols + 50);
		graphics = disp.createGraphics();
		currentGame = new Game(pictureRows, pictureCols);
		currentGame.setWarped(true);
		levelZero = new Game(pictureRows, pictureCols);
		setMainScreen();
	}

	/*
	 * pauses the current task
	 */
	private void pauseTask() {
		if (nextTurn != null) {
			nextTurn.cancel();
			nextTurn = null;
			turnLoop.purge();
		}
	}

	/*
	 * starts the current task using the current timer value
	 */
	private void startTask() {
		if (nextTurn == null) {
			nextTurn = new TimerTask() {
				@Override
				public void run() {
					if (mainFrame != null) {
						nextTurn();
					}
				}
			};
		} else {
			System.out.println("warning, start called while nextTurn may be active");
		}
		turnLoop.schedule(nextTurn, 0, periodMilis);
	}

	/**
	 * Creates the North JPanel with back to menu button, setting, and other quality
	 * of life features
	 */
	private void createInfoPanel() {
		JPanel infoPanel = new JPanel();
		infoPanel.setBackground(backgroundColor);
		infoPanel.setLayout(new BorderLayout());
		JButton home;
		try {
			Image homeImage = ImageIO.read(HOME);
			homeImage = getScaledImage(homeImage, 60, 60);
			Icon icon = new ImageIcon(homeImage);// change url as needed
			home = new JButton(icon);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.out.println("There was an error trying to initialize Home image");
			home = new JButton("Home");
		}
		setInvisible(home);
		home.addActionListener(e -> {
			// insert action here
			setMainScreen();
			System.out.println("Home pressed");
		});
		infoPanel.add(BorderLayout.WEST, home);

/*		JButton settings;
		try {
			Image settingsImage = ImageIO.read(SETTINGS);
			settingsImage = getScaledImage(settingsImage, 60, 60);
			Icon icon = new ImageIcon(settingsImage);// change url as needed
			settings = new JButton(icon);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.out.println("There was an error trying to initialize Settings image");
			settings = new JButton("Settings");
		}
		setInvisible(settings);
		settings.addActionListener(e -> {
			// insert action here
			System.out.println("Settings pressed");
			if (settingsPopup == null) {
				// new settings
				makeSettingsPopup();
			} else {
				// close settings
				settingsPopup.setVisible(false);
				settingsPopup = null;
			}
		});
		infoPanel.add(BorderLayout.EAST, settings);*/

		mainFrame.getContentPane().add(BorderLayout.NORTH, infoPanel);
	}

	// idk
	private void setInvisible(JButton button) {
		button.setOpaque(false);
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		button.setFocusPainted(false);
	}

	// idk
	private Image getScaledImage(Image srcImg, int w, int h) {
		BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = resizedImg.createGraphics();

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, w, h, null);
		g2.dispose();

		return resizedImg;
	}

	// makes the settings menu appear
	private void makeSettingsPopup() {
		settingsPopup = new JFrame();
		settingsPopup.setResizable(false);
		settingsPopup.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel inner = new JPanel();

		JPanel panel = new JPanel();

		inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
		inner.setBorder(new EmptyBorder(10, 10, 10, 10));
		inner.add(panel);

		JButton closer = new JButton("Close");
		closer.addActionListener(e -> {
			settingsPopup.setVisible(false);
			// TODO insert settings config getter here

			settingsPopup = null;
		});
		closer.setFocusPainted(false);
		inner.add(closer);

		settingsPopup.add(inner);
		settingsPopup.pack();
		settingsPopup.setVisible(true);

		settingsPopup.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				if (settingsPopup == null) {
					System.out.println("Closed");
				} else {
					System.out.println("Not Closed");
				}
			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

		});
	}

	// sets the mainScreen
	private void setMainScreen() {
		screenName = "mainScreen";
		disp.setAllPixelsToAColor(backgroundColor);
		graphics.drawImage(sandbox.getBufferedImage(), 25, 150, this);
		graphics.drawImage(tutorial.getBufferedImage(), trueNumberOfCols - 225, 150, this);
		graphics.drawImage(level.getBufferedImage(), halftNumberOfCols - 100, 150, this);
		graphics.setColor(stringColor);
		graphics.setFont(new Font("Times", Font.BOLD, 30));
		graphics.drawString("Welcome to our version of game of life", halftNumberOfCols - 325, 50);
		graphics.setFont(new Font("Times", Font.PLAIN, 20));
		graphics.drawString("Sandbox", 82, 130);
		graphics.drawString("Block information", trueNumberOfCols - 212, 130);
		graphics.drawString("Levels", halftNumberOfCols - 35, 130);
		graphics.drawString("Credit to Conway's Game of Life for inspiration for this project", halftNumberOfCols - 309,
				395);
		graphics.drawString("Credit to Conway's Game of Life again for the rules for alive and dead block",
				halftNumberOfCols - 377, 415);
		graphics.drawString("Credit to pixlart.com for the application in which the blocks were drawn",
				halftNumberOfCols - 358, 435);
		setImage(disp);
	}

	// sets tutorialScreen
	private void settutorialScreen() {
		disp.setAllPixelsToAColor(backgroundColor);
		graphics.setColor(stringColor);
		graphics.setFont(new Font("Times", Font.BOLD, 26));
		graphics.drawString("Click a block to learn more", halftNumberOfCols - 195, 26);
		Picture pict = null;
		for (int i = 0; i < 4; i++) {
			pict = tutorialIcons[i];
			graphics.drawImage(pict.getBufferedImage(), (i * 55) + halftNumberOfCols - 85, 50, this);
		}
		graphics.drawImage(leftBack.getBufferedImage(), 0, 0, this);
		setImage(disp);
	}

	// setsboard at default status of dead and sets the screen with dead picture
	private void setBoard() {
		disp.setAllPixelsToAColor(backgroundColor);
		if (yesTimer) {
			graphics.drawImage(nextBlue.getBufferedImage(), cols, 0, this);
		} else if (!yesTimer) {
			graphics.drawImage(next.getBufferedImage(), cols, 0, this);
		}
		graphics.drawImage(up.getBufferedImage(), cols, 50, this);
    if (selection){
      graphics.drawImage(tutorialIcons[numberStatus].getBufferedImage(), cols, 100, this);
    } else if (!selection){
      graphics.drawImage(statusAD.getBufferedImage(), cols, 100, this);
    }
		graphics.drawImage(down.getBufferedImage(), cols, 150, this);
		graphics.drawImage(rightBack.getBufferedImage(), cols, 200, this);
		graphics.drawImage(trash.getBufferedImage(), cols, 250, this);
    graphics.drawImage(settings50.getBufferedImage(), cols, 300, this);
		for (int row = 0; row < pictureRows; row++) {
			for (int col = 0; col < pictureCols; col++) {
				Block currentBlock = currentGame.getBlock(row, col);
				Picture pict = status[currentBlock.getStatus()];
				graphics.drawImage(pict.getBufferedImage(), col * pictureSize, row * pictureSize, this);
			}
		}
		setImage(disp);
	}

	// sets the testing level with one tut block
	private void setLevel() {
		disp.setAllPixelsToAColor(backgroundColor);
		graphics.drawImage(next.getBufferedImage(), cols, 0, this);
		graphics.drawImage(up.getBufferedImage(), cols, 50, this);
		graphics.drawImage(tutorialIcons[numberStatus].getBufferedImage(), cols, 100, this);
		graphics.drawImage(down.getBufferedImage(), cols, 150, this);
		graphics.drawImage(rightBack.getBufferedImage(), cols, 200, this);
		for (int row = 0; row < pictureRows; row++) {
			for (int col = 0; col < pictureCols; col++) {
				Block currentBlock = levelZero.getBlock(row, col);
				Picture pict = status[currentBlock.getStatus()];
				graphics.drawImage(pict.getBufferedImage(), col * pictureSize, row * pictureSize, this);
			}
		}
		setImage(disp);
	}

	// sets the level chooser and icons for levels (incomplete)
	private void setLevelChooser() {
		levelStatus();
		disp.setAllPixelsToAColor(backgroundColor);
		graphics.drawImage(leftBack.getBufferedImage(), 0, 0, this);
		graphics.drawImage(tutorialIcons[levelNumber].getBufferedImage(), halftNumberOfCols - 25, 200, this);
		graphics.drawImage(leftLevel.getBufferedImage(), halftNumberOfCols - 75, 200, this);
		graphics.drawImage(rightLevel.getBufferedImage(), halftNumberOfCols + 25, 200, this);
		int actualNum = levelNumber + 1;
		graphics.drawString("Level " + actualNum, halftNumberOfCols - 45, 100);
		graphics.drawImage(next.getBufferedImage(), halftNumberOfCols - 25, 300, this);
		setImage(disp);
	}

	private void levelChooserClick(DigitalPicture pict, Pixel pix) {
		if (pix.getRow() >= 200 && pix.getRow() <= 250 && pix.getCol() >= halftNumberOfCols - 75
				&& pix.getCol() <= halftNumberOfCols - 25) {
			levelNumber--;
			setLevelChooser();
		} else if (pix.getRow() >= 200 && pix.getRow() <= 250 && pix.getCol() >= halftNumberOfCols + 25
				&& pix.getCol() <= halftNumberOfCols + 75) {
			levelNumber++;
			setLevelChooser();
		} else if (pix.getRow() <= 50 && pix.getCol() <= 50) {
			screenName = "mainScreen";
			setMainScreen();
		} else if (pix.getCol() >= halftNumberOfCols - 25 && pix.getCol() <= halftNumberOfCols + 25
				&& pix.getRow() >= 300 && pix.getRow() <= 350) {
			screenName = "levelZero";
			levelSelect(levelNumber);
		}
	}

	private void levelSelect(int levelNumber) {
		for (int row = 0; row < pictureRows; row++) {
			for (int col = 0; col < pictureCols; col++) {
				Block currentBlock = levelZero.getBlock(row, col);
				Picture pict = status[currentBlock.getStatus()];
				currentBlock.setReplaceable(false);
			}
		}
		if (levelNumber == 0) {
			setTutBlock(0,0,4);
			setLevel();
		} else if (levelNumber == 1) {
			setTutBlock(0,0,4);
			setTutBlock(0,2,4);
			setTutBlock(2,0,4);
			setLevel();
		} else if (levelNumber == 2) {
			setTutBlock(0,0,4);
			setTutBlock(0,2,4);
			setTutBlock(2,0,4);
			setTutBlock(1,1,5);
			setLevel();
		} else if (levelNumber == 3) {
			for (int i = 0; i < pictureCols; i++) {
				setTutBlock(0,i,6);
			}
			setLevel();
		} else if (levelNumber == 4) {
			setTutBlock(0,1,4);
			setTutBlock(1,2,4);
			setTutBlock(2,0,4);
			setTutBlock(2,1,4);
			setTutBlock(2,2,4);
			setLevel();
		}
	}
	private void setTutBlock (int row, int col, int status){
		Block currentBlock = levelZero.getBlock(row, col);
		currentBlock.setReplaceable(true);
		levelZero.addTutBlocks(currentBlock);
		if (status == 4){
			levelZero.setTutAlive(row, col);
		} else if (status == 5){
			levelZero.setTutWall(row, col);
		} else if (status == 6){
			levelZero.setTutRandom(row, col);
		}
	}

	// quite literally does nothing only there to have this class as a subclass
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		return false;
	}

	// class nexTurn and sets the screen as the result
	public void nextTurn() {
		running = true;
		Picture pict = null;
		ArrayList<Block> changedBlocks = new ArrayList<Block>();
		if (screenName.equals("game")) {
			changedBlocks = currentGame.nextTurn();
		} else if (screenName.equals("levelZero")) {
			changedBlocks = levelZero.nextTurn();
		}
		int blockStatus;
		int row;
		int col;
		Block currentBlock;
		for (int i = 0; i < changedBlocks.size(); i++) {
			currentBlock = changedBlocks.get(i);
			blockStatus = currentBlock.getStatus();
			row = currentBlock.getRow() * pictureSize;
			col = currentBlock.getColumn() * pictureSize;
			pict = status[blockStatus];
			if (row < rows && col < cols) {
				graphics.drawImage(pict.getBufferedImage(), col, row, this);
			}
		}
		running = false;
		setImage(disp);
	}

	// sets the picture clicked on to the opposite status
	public void mouseClickedAction(DigitalPicture pict, Pixel pix) {
		if (screenName.equals("mainScreen")) {
			mainButtons(pict, pix);
		} else if (screenName.equals("game")) {
			game(pict, pix);
		} else if (screenName.equals("tutorial")) {
			tutorialButtons(pict, pix);
		} else if (screenName.equals("deadtutorial") || screenName.equals("alivetutorial")
				|| screenName.equals("walltutorial") || screenName.equals("randomtutorial")) {
			tutorialScreensButtons(pict, pix);
		} else if (screenName.equals("level")) {
			levelChooserClick(pict, pix);
		} else if (screenName.equals("levelZero")) {
			levelClick(pict, pix);
		} else if (screenName.equals("settings")){
      settingsClick(pict, pix);
    }
	}

	// all turorial interactions for the tutorial screen other than "tutorial"
	private void tutorialScreensButtons(DigitalPicture pict, Pixel pix) {
		if (pix.getRow() <= 50 && pix.getCol() <= 50) {
			screenName = "tutorial";
			settutorialScreen();
		}
	}

	// all main screen interactions
	private void mainButtons(DigitalPicture pict, Pixel pix) {
		if (pix.getCol() >= 25 && pix.getCol() <= 225 && pix.getRow() >= 150 && pix.getRow() <= 350) {
			pauseTask();
			screenName = "game";
			setBoard();
		} else if (pix.getCol() >= trueNumberOfCols - 225 && pix.getCol() <= trueNumberOfCols - 25
				&& pix.getRow() >= 150 && pix.getRow() <= 350) {
			pauseTask();
			screenName = "tutorial";
			settutorialScreen();
		} else if (pix.getCol() >= halftNumberOfCols - 100 && pix.getCol() <= halftNumberOfCols + 100
				&& pix.getRow() >= 150 && pix.getRow() <= 350) {
			pauseTask();
			setLevelChooser();
			// screenName = "levelChooser";
			screenName = "level";
			// setLevel();
		}
    if (!timerMode&&screenName.equals("mainScreen")) {
			timerMode = true;
			startAnimation();
		} else if (timerMode&&screenName.equals("mainScreen")) {
			timerMode = false;
			pauseTask();
		}
	}

	// animation automatic
	private void startAnimation() {
		if (nextTurn == null) {
			nextTurn = new TimerTask() {
				@Override
				public void run() {
					if (mainFrame != null) {
						clickAnimation();
					}
				}
			};
		} else {
			System.out.println("warning, start called while nextTurn may be active");
		}
		turnLoop.schedule(nextTurn, 0, periodMilis);
	}

	// all "tutorial" screen interactions
	private void tutorialButtons(DigitalPicture pict, Pixel pix) {
		if (pix.getCol() <= 50 && pix.getRow() <= 50) {
			screenName = "mainScreen";
			setMainScreen();
		} else if (pix.getCol() >= halftNumberOfCols - 85 && pix.getCol() <= halftNumberOfCols - 35
				&& pix.getRow() >= 50 && pix.getRow() <= 100) {
			screenName = "deadtutorial";
			setdeadtutorial();
		} else if (pix.getCol() >= halftNumberOfCols - 30 && pix.getCol() <= halftNumberOfCols + 20
				&& pix.getRow() >= 50 && pix.getRow() <= 100) {
			screenName = "alivetutorial";
			setalivetutorial();
		} else if (pix.getCol() >= halftNumberOfCols + 25 && pix.getCol() <= halftNumberOfCols + 75
				&& pix.getRow() >= 50 && pix.getRow() <= 100) {
			screenName = "walltutorial";
			setwalltutorial();
		} else if (pix.getCol() >= halftNumberOfCols + 80 && pix.getCol() <= halftNumberOfCols + 130
				&& pix.getRow() >= 50 && pix.getRow() <= 100) {
			screenName = "randomtutorial";
			setrandomtutorial();
		}
	}

	// sets the dead tutorial with icon and text
	private void setdeadtutorial() {
		disp.setAllPixelsToAColor(backgroundColor);
		graphics.setColor(stringColor);
		graphics.setFont(new Font("Times", Font.PLAIN, 15));
		graphics.drawString(
				"Dead Block: Becomes alive if next to three alive blocks this block will become alive otherwise it will stay dead",
				halftNumberOfCols - 396, 130);
		graphics.drawImage(leftBack.getBufferedImage(), 0, 0, this);
		graphics.drawImage(dead50.getBufferedImage(), halftNumberOfCols - 25, 57, this);
		setImage(disp);
	}

	// sets the alive tutorial with icon and text
	private void setalivetutorial() {
		disp.setAllPixelsToAColor(backgroundColor);
		graphics.setColor(stringColor);
		graphics.setFont(new Font("Times", Font.PLAIN, 15));
		graphics.drawString(
				"Alive Block: Becomes dead if not next to two or three alive neigbhors otherwise it will stay alive",
				halftNumberOfCols - 347, 130);
		graphics.drawImage(leftBack.getBufferedImage(), 0, 0, this);
		graphics.drawImage(alive50.getBufferedImage(), halftNumberOfCols - 25, 57, this);
		setImage(disp);
	}

	// sets the wall tutorial with icon and text
	private void setwalltutorial() {
		disp.setAllPixelsToAColor(backgroundColor);
		graphics.setColor(stringColor);
		graphics.setFont(new Font("Times", Font.PLAIN, 15));
		graphics.drawString("Wall Block: Remains a wall no matter what its the neighbors are", halftNumberOfCols - 233,
				130);
		graphics.drawImage(leftBack.getBufferedImage(), 0, 0, this);
		graphics.drawImage(wall50.getBufferedImage(), halftNumberOfCols - 25, 57, this);
		setImage(disp);
	}

	// sets the random tutorial with icon and text
	private void setrandomtutorial() {
		disp.setAllPixelsToAColor(backgroundColor);
		graphics.setColor(stringColor);
		graphics.setFont(new Font("Times", Font.PLAIN, 15));
		graphics.drawString("Random Block: Has a chance at becoming another block at random", halftNumberOfCols - 246,
				130);
		graphics.drawImage(leftBack.getBufferedImage(), 0, 0, this);
		graphics.drawImage(random50.getBufferedImage(), halftNumberOfCols - 25, 57, this);
		setImage(disp);
	}

	// calls the correct method, see timerMode and yesTimer at the top of the code
	private void timerControl() {
		if (!timerMode && yesTimer) {
			graphics.drawImage(pause.getBufferedImage(), cols, 0, this);
			timerMode = true;
			startTask();
		} else if (timerMode && yesTimer) {
			graphics.drawImage(nextBlue.getBufferedImage(), cols, 0, this);
			timerMode = false;
			pauseTask();
		} else if (!yesTimer) {
			nextTurn();
		}
	}

	// all game screen interactions
	private void game(DigitalPicture pict, Pixel pix) {
		if (pix.getRow() < rows && pix.getCol() < pictureCols * pictureSize
				&& (currentGame.getBlock(pix.getY() / pictureSize, pix.getX() / pictureSize)).getReplaceable()&&!running) {
			currentGame = blockClick(pict, pix);
		} else if (pix.getRow() <= 50 && pix.getCol() >= cols) {
			timerControl();
		} else if (pix.getRow() <= 100 && pix.getCol() >= cols &&selection) {
      numberStatus--;
			nextstatus();
			graphics.drawImage(tutorialIcons[numberStatus].getBufferedImage(), cols, 100, this);
		} else if (pix.getRow() <= 200 && pix.getRow() >= 150 && pix.getCol() >= cols&&selection) {
      numberStatus++;
			nextstatus();
			graphics.drawImage(tutorialIcons[numberStatus].getBufferedImage(), cols, 100, this);
		} else if (pix.getRow() >= 200 && pix.getRow() <= 250 && pix.getCol() >= cols) {
			pauseTask();
			screenName = "mainScreen";
			setMainScreen();
		} else if (pix.getRow() <= 300 && pix.getCol() >= cols) {
			pauseTask();
      resetBoard();
		} else if (pix.getRow() <= 350 && pix.getCol() >= cols) {
      pauseTask();
      screenName = "settings";
      setSettingsScreen();
    }
	}

	// temporary testing level interaction
	private void levelClick(DigitalPicture pict, Pixel pix) {
		// checks if the block exists and can be replaced
		if (pix.getRow() < rows && pix.getCol() < pictureCols * pictureSize
				&& (levelZero.getBlock(pix.getY() / pictureSize, pix.getX() / pictureSize).getReplaceable())) {
			levelZero = blockClick(pict, pix);
		} else if (pix.getRow() <= 50 && pix.getCol() >= cols && levelZero.getTutBlocksSize() <= 0) {
			nextTurn();
		} else if (pix.getRow() >= 50 && pix.getRow() <= 100 && pix.getCol() >= cols) {
			numberStatus--;
			nextstatus();
			graphics.drawImage(tutorialIcons[numberStatus].getBufferedImage(), cols, 100, this);
		} else if (pix.getRow() <= 200 && pix.getRow() >= 150 && pix.getCol() >= cols) {
			numberStatus++;
			nextstatus();
			graphics.drawImage(tutorialIcons[numberStatus].getBufferedImage(), cols, 100, this);
		} else if (pix.getRow() >= 200 && pix.getRow() <= 250 && pix.getCol() >= cols) {
			levelZero = new Game(pictureRows, pictureCols);
			screenName = "level";
			setLevelChooser();
		}
	}

	// resest board to default game status (dead) and changes screen
	private void resetBoard() {
		currentGame = new Game(pictureRows, pictureCols);
		for (int row = 0; row < pictureRows; row++) {
			for (int col = 0; col < pictureCols; col++) {
				graphics.drawImage(dead.getBufferedImage(), col * pictureSize, row * pictureSize, this);
			}
		}
		setImage(disp);
	}

	// sets the block clicked to the correct status indicated by selection and
	// numberstatus
	private Game blockClick(DigitalPicture pict, Pixel pix) {
		int blockRow = (int) pix.getY() / pictureSize;
		int blockCol = (int) pix.getX() / pictureSize;
		Game blockGame = null;
		if (screenName.equals("game")) {
			blockGame = currentGame;
		} else if (screenName.equals("levelZero")) {
			blockGame = levelZero;
		}
		Block clickedBlock = blockGame.getBlock(blockRow, blockCol);
		if (selection && numberStatus == 0) {
			clickedBlock.setStatus(0);
			blockGame.setDead(blockRow, blockCol);
		} else if (selection && numberStatus == 1) {
			clickedBlock.setStatus(1);
			blockGame.setAlive(blockRow, blockCol);
		} else if (selection && numberStatus == 2) {
			clickedBlock.setStatus(2);
			blockGame.setWall(blockRow, blockCol);
		} else if (selection && numberStatus == 3) {
			clickedBlock.setStatus(3);
			blockGame.setRandom(blockRow, blockCol);
		} else if (clickedBlock.getStatus() == 0) {
			clickedBlock.setStatus(1);
			blockGame.setAlive(blockRow, blockCol);
		} else if (clickedBlock.getStatus() == 1) {
			clickedBlock.setStatus(0);
			blockGame.setDead(blockRow, blockCol);
		}
		graphics.drawImage(status[clickedBlock.getStatus()].getBufferedImage(), blockCol * pictureSize,
				blockRow * pictureSize, this);
		setImage(disp);
		return blockGame;
	}

	// switchs the selection status to wrap around the edges preventing errors
	private void nextstatus() {
		if (numberStatus < 0) {
			numberStatus = tutorialIcons.length - 1;
		} else if (numberStatus == 4) {
			numberStatus = 0;
		}
	}

	private void levelStatus() {
		if (levelNumber < 0) {
			levelNumber = 5;
		} else if (levelNumber == 5) {
			levelNumber = 0;
		}
	}

	// displays color blocks which appear surroding the edges of the screen (best
	// for click and drag may add animation)
	private void clickAnimation() {
		if (animationStatus == animationIcons.length) {
			animationStatus = 0;
		}
		if (animationCol < trueNumberOfCols && animationRow <= 0) {
			graphics.drawImage(animationIcons[animationStatus].getBufferedImage(), animationCol, 0, this);
			animationCol = animationCol + 20;
		} else if (animationRow < rows && animationCol >= trueNumberOfCols) {
			graphics.drawImage(animationIcons[animationStatus].getBufferedImage(), trueNumberOfCols - 20, animationRow,
					this);
			animationRow = animationRow + 20;
		} else if (animationCol > 0) {
			graphics.drawImage(animationIcons[animationStatus].getBufferedImage(), animationCol, rows - 20, this);
			animationCol = animationCol - 20;
		} else {
			graphics.drawImage(animationIcons[animationStatus].getBufferedImage(), 0, animationRow - 10, this);
			animationRow = animationRow - 20;
		}
		animationStatus++;
		setImage(disp);
	}
  private void setSettingsScreen(){
    disp.setAllPixelsToAColor(backgroundColor);
    graphics.drawImage(leftBack.getBufferedImage(), 0, 0, this);
    if (yesTimer){
      graphics.drawImage(pause.getBufferedImage(), 200, 0, this);
    } else if (!yesTimer){
      graphics.drawImage(next.getBufferedImage(), 200, 0, this);
    }
    if (selection){
      graphics.drawImage(up.getBufferedImage(), 100, 0, this);
    } else if (!selection){
      graphics.drawImage(statusAD.getBufferedImage(), 100, 0, this);
    }
    setImage(disp);
  }
  private void settingsClick(DigitalPicture pict, Pixel pix){
    if (pix.getCol()<=50&&pix.getRow()<=50){
      screenName = "game";
      setBoard();
    } else if (pix.getCol()>=100&&pix.getCol()<=150&&pix.getRow()<=50){
      selection = !selection;
      setSettingsScreen();
    } else if (pix.getCol()>=200&&pix.getCol()<=250&&pix.getRow()<=50){
      yesTimer = !yesTimer;
      setSettingsScreen();
    } 
  }
	// main starts the entire procress
	public static void main(String args[]) {
		// credit for screen size code from
		// https://alvinalexander.com/blog/post/jfc-swing/how-determine-get-screen-size-java-swing-app/#:~:text=The%20following%20Java%20code%20demonstrates,getScreenSize()%3B
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) screenSize.getWidth() - 4;
		int height = (int) screenSize.getHeight() - 155;
		new GameOfLife(height + 50, width - 50, "mainScreen");
	}
}
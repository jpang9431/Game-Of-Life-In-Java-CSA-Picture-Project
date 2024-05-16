import java.util.ArrayList;

class Game {
	private boolean warped = false;
	private ArrayList<Block> alive = new ArrayList<Block>();
	private ArrayList<Block> dice = new ArrayList<Block>();
	private ArrayList<Block> changed = new ArrayList<Block>();
	private ArrayList<Block> tutBlocks = new ArrayList<Block>();
	private Block[][] board;

	/**
	 * Constructor that takes in two integers
	 * 
	 * @param x the horizontal size of the board
	 * @param y the vertical size of the board
	 */
	Game(int rows, int cols) {
		board = new Block[rows][cols]; // display system goes from top left (0,0) to bottom right
		setBoard(Block.DEAD);
	}

	/**
	 * Constructor that takes in two integers and the default
	 * 
	 * @param x             the horizontal size of the board
	 * @param y             the vertical size of the board
	 * @param defaultStatus The default status of the initiated blocks(Either ALIVE
	 *                      or DEAD)
	 */
	Game(int rows, int cols, int defaultStatus) {
		board = new Block[rows][cols];
		setBoard(defaultStatus);
	}

	public void addTutBlocks(Block inputBlock) {
		tutBlocks.add(inputBlock);
	}

	public void removeTutBlocks(Block inputBlock) {
		if (tutBlocks.indexOf(inputBlock) > -1) {
			tutBlocks.remove(tutBlocks.indexOf(inputBlock));
		}
	}

	public int getTutBlocksSize() {
		return tutBlocks.size();
	}

	/*
	 * Sets the warped variable
	 * 
	 * @param val true or false to set warped to
	 * 
	 */
	public void setWarped(boolean val) {
		warped = val;
	}

	/*
	 * Returns the block at the given row and col
	 * getBlock
	 * 
	 * @param row the row to retrieve
	 * 
	 * @param col the column of the array
	 */
	public Block getBlock(int row, int col) {
		return board[row][col];
	}

	/**
	 * Set the board with the passed in initial status
	 * 
	 * @param initStat can be either Block.ALIVE, Block.DEAD or any other status
	 *                 added
	 */
	private void setBoard(int initStat) {
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				board[i][j] = new Block(this, i, j, initStat);
			}
		}
	}

	/**
	 * Getter for board
	 * 
	 * @return returns a 2D array of blocks
	 */
	public Block[][] getBlocks() {
		return board;
	}

	/**
	 * Set (x,y) block to alive, prints out error message if block is already alive.
	 * 
	 * @param x horizontal position of the block
	 * @param y vertical position of the block
	 */
	public void setAlive(int row, int col) {
		if (alive.contains(board[row][col])) {
			return;
		}
		board[row][col].setStatus(Block.ALIVE);
		alive.add(board[row][col]);
	}

	/**
	 * A getter for the private alive block ArrayList
	 * 
	 * @return alive ArrayList
	 */
	public ArrayList<Block> getAlive() {
		return alive;
	}

	/**
	 * Set (x,y) block to dead, prints out error message if block is already dead.
	 * 
	 * @param x horizontal position of the block
	 * @param y vertical position of the block
	 */
	public void setDead(int row, int col) {
		board[row][col].setStatus(Block.DEAD);
	}

	public void setWall(int row, int col) {
		board[row][col].setStatus(Block.WALL);
	}

	public void setRandom(int row, int col) {
		board[row][col].setStatus(Block.RANDOM);
		dice.add(board[row][col]);
	}

	public void setTutAlive(int row, int col) {
		board[row][col].setStatus(Block.TUTALIVE);
		board[row][col].setSpecialBlock(Block.ALIVE, Block.ALIVE);
		tutBlocks.add(board[row][col]);
	}

	public void setTutWall(int row, int col) {
		board[row][col].setStatus(Block.TUTWALL);
		board[row][col].setSpecialBlock(Block.WALL, Block.WALL);
		tutBlocks.add(board[row][col]);
	}

	public void setTutRandom(int row, int col) {
		board[row][col].setStatus(Block.TUTRANDOM);
		board[row][col].setSpecialBlock(Block.RANDOM, Block.RANDOM);
		tutBlocks.add(board[row][col]);
	}

	/**
	 * Moves the game to the next turn, calling the game logic (changed only works
	 * for one
	 * 
	 * @return the changed blocks for easier changing of graphical display of the
	 *         board
	 */
	public ArrayList<Block> nextTurn() { // logic loop
		ArrayList<Block> check = new ArrayList<Block>();
		changed = new ArrayList<Block>();
		int row, col;
		for (Block i : alive) {// adds all possible changeable blocks to check without repetition
			col = i.getColumn();
			row = i.getRow();
			for (int j = col - 1; j <= col + 1; j++) {
				for (int k = row - 1; k <= row + 1; k++) {
					if ((j >= 0 && k >= 0) && (k < board.length && j < board[k].length)) {
						if (!check.contains(board[k][j])) {
							check.add(board[k][j]);
						}
					} else {
						int cRow = k, cCol = j, rowLen = board.length, colLen = board[0].length;
						if (k < 0) {
							// case row is below zero, set check to largest row
							// i = -1
							cRow = rowLen + k;
						} else if (k >= rowLen) {
							// case row is above max row, set row to min row
							// i-rowLen >= 0, i >= rowLen
							cRow = k - rowLen;
						}
						if (j < 0) {
							// j=-1
							cCol = colLen + j;
						} else if (j >= colLen) {
							// j-colLen >= 0, j >=rowLen

							cCol = j - colLen;
						}
						if (!check.contains(board[cRow][cCol])) {
							check.add(board[cRow][cCol]);
						}
					}
				}
			}
		}
		int[] neighbors = new int[check.size()];
		for (int i = 0; i < check.size(); i++) {
			neighbors[i] = getNeighbor(check.get(i));
		}
		for (int i = 0; i < check.size(); i++) {
			change(check.get(i), neighbors[i]);
		}
		ArrayList<Block> newDice = new ArrayList<Block>();
		for (int i = 0; i < dice.size(); i++) {
			double randomDecimal = Math.random();
			randomDecimal = randomDecimal * 3.0;
			int randomNumber = (int) Math.round(randomDecimal);
			Block currentBlock = dice.get(i);
			currentBlock.setStatus(randomNumber);
			if (randomNumber == 3) {
				newDice.add(currentBlock);
			} else if (randomNumber == 1) {
				alive.add(currentBlock);
				changed.add(currentBlock);
			} else {
				changed.add(currentBlock);
			}
		}
		dice.clear();
		for (int i = 0; i < newDice.size(); i++) {
			Block currentBlock = newDice.get(i);
			dice.add(currentBlock);
		}
		return changed;
	}

	/**
	 * Game logic method, moves on to the next turn Updated for the wall block
	 * 
	 * @param obj      block to execute the method on
	 * @param neighbor the amount of neighbors this block has
	 */
	private void change(Block obj, int neighbor) {
		if (obj.getStatus() == Block.ALIVE) {
			if (!((neighbor == 2) || (neighbor == 3))) {
				obj.setStatus(Block.DEAD);
				changed.add(obj);
				alive.remove(obj);
			}
		} else if (obj.getStatus() == Block.DEAD) { // block dead
			if (neighbor == 3) {
				obj.setStatus(Block.ALIVE);
				changed.add(obj);
				alive.add(obj);
			}
		}
	}

	/**
	 * get the amount of neighbors next to a block
	 * 
	 * @param obj the block to find the amount of neighbors on
	 */
	private int getNeighbor(Block obj) {
		int row = obj.getRow(), col = obj.getColumn(), count = 0, rowLen = board.length, colLen = board[0].length;
		for (int i = row - 1; i <= row + 1; i++) {
			for (int j = col - 1; j <= col + 1; j++) {
				if ((i >= 0 && j >= 0) && (i < rowLen && j < colLen)) {
					// block inside of arrayList
					if (!(i == row && j == col) && board[i][j].getStatus() == Block.ALIVE) {
						count++;
					}
				} else if (warped) {// not in bounds and warp is on
					int cRow = i, cCol = j;
					if (i < 0) {
						// case row is below zero, set check to largest row
						// i = -1
						cRow = rowLen + i;
					} else if (i >= rowLen) {
						// case row is above max row, set row to min row
						// i-rowLen >= 0, i >= rowLen
						cRow = i - rowLen;
					}
					if (j < 0) {
						// j=-1
						cCol = colLen + j;
					} else if (j >= colLen) {
						// j-colLen >= 0, j >=rowLen

						cCol = j - colLen;
					}
					if (board[cRow][cCol].getStatus() == Block.ALIVE) {
						count++;
					}
				}
			}
		}
		return count;
	}

	/**
	 * prints out the blocks' status in a grid format
	 */
	public void print() {
		System.out.println();
		for (int i = 0; i < board.length; i++) {
			for (int j = 0; j < board[i].length; j++) {
				System.out.print(board[i][j].getStatus() + " ");
			}
			System.out.println();
		}
	}
}
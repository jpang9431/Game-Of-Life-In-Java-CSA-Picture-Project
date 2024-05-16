class Block {
	final static int DEAD = 0, // inert white
			ALIVE = 1, // infect dead, die 1 turn, black
			WALL = 2, // inert grey
			RANDOM = 3,
			TUTALIVE = 4,
			TUTWALL = 5,
			TUTRANDOM = 6;
	private int row, column, status, required, current;
	private boolean replaceable = true;
	@SuppressWarnings("unused")
	/** the "parent" board of this block */
	private Game board;
	/** default value of the initiated block */
	private static int def = DEAD;
	private int goal = 0;

	Block(Game board, int inputRow, int inputColumn, int inputStatus) { // set case (testing?)
		this.board = board;
		row = inputRow;
		column = inputColumn;
		status = inputStatus;
	}

	Block(Game board, int inputRow, int inputColumn) { // default case
		this.board = board;
		row = inputRow;
		column = inputColumn;
		status = def;
	}

	public int getStatus() {// return block status
		return status;
	}

	public void setStatus(int inputStatus) {// sets the status as inputStatus
		if (required == 0 || board.getTutBlocksSize() == 0) {
			status = inputStatus;
		} else if (required == inputStatus) {
			status = inputStatus;
			board.removeTutBlocks(this);
		}
	}

	public void setReplaceable(Boolean inputBoolean) {
		replaceable = inputBoolean;
	}

	public void setSpecialBlock(int inputRequired, int inputCurrent) {
		required = inputRequired;
		current = inputCurrent;
	}

	public Boolean getReplaceable() {
		return replaceable;
	}

	public int getRow() {
		return row;
	}

	public int getColumn() {
		return column;
	}

}
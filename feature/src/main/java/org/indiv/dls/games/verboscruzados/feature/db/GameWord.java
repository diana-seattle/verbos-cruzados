package org.indiv.dls.games.verboscruzados.feature.db;

public class GameWord {
    private String word;
    private String clue;
    private String secondaryClue;
    private int row;
    private int col; 
    private boolean across; 
    private String userText; 

    /**
     * Constructor
     *
     * @param word conjugated verb used in the puzzle.
     * @param clue clue given to user (e.g. "(Yo) ___________ yesterday (preterit)").
     * @param secondaryClue secondary clue (e.g. "hablar (to speak)").
     * @param gameNo
     * @param row
     * @param col
     * @param across
     */
	public GameWord(String word, String clue, String secondaryClue, int gameNo, int row, int col, boolean across) {
		super();
		this.word = word;
		this.clue = clue;
		this.secondaryClue = secondaryClue;
		this.row = row;
		this.col = col;
		this.across = across;
	}

    // getters and setters
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}

	public String getClue() {
		return clue;
	}

	public String getSecondaryClue() {
		return secondaryClue;
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}

	public boolean isAcross() {
		return across;
	}

	public String getUserText() {
		return userText;
	}
	public void setUserText(String userText) {
		this.userText = userText;
	}

	public String toString() {
		return getWord();
	}

}

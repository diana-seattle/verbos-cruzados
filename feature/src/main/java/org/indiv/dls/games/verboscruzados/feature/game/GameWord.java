package org.indiv.dls.games.verboscruzados.feature.game;

public class GameWord {
    private String word;
    private String sentenceClue;
    private String infinitiveClue;
    private int row;
    private int col; 
    private boolean across; 
    private String userText; 

    /**
     * Constructor
     *
     * @param word conjugated verb used in the puzzle.
     * @param sentenceClue clue as example sentence (e.g. "(Yo) ___________ yesterday (preterit)").
     * @param infinitiveClue infinitive clue (e.g. "hablar (to speak)").
     * @param row
     * @param col
     * @param across
     */
	public GameWord(String word, String sentenceClue, String infinitiveClue, int row, int col, boolean across) {
		super();
		this.word = word;
		this.sentenceClue = sentenceClue;
		this.infinitiveClue = infinitiveClue;
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

	public String getSentenceClue() {
		return sentenceClue;
	}

	public String getInfinitiveClue() {
		return infinitiveClue;
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

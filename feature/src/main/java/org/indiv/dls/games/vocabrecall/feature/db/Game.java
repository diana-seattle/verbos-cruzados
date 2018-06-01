package org.indiv.dls.games.vocabrecall.feature.db;

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

public class Game {

    public static final String TABLE_NAME = "GAME";
    public static final String GAME_NO = "GAME_NO";  // PK
    public static final String MINI_CLUES = "MINI_CLUES";   
    public static final String FULL_CLUES = "FULL_CLUES";  
    public static final String TOTAL_WORDS = "TOTAL_WORDS";
    public static final String SOLVED_WORDS = "SOLVED_WORDS"; 
	
    public static final String TABLE_DELETE = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
			            		GAME_NO	    	+ " INTEGER NOT NULL PRIMARY KEY, " +
			            		MINI_CLUES		+ " INTEGER NOT NULL DEFAULT 0, " +
			            		FULL_CLUES		+ " INTEGER NOT NULL DEFAULT 0, " +
			            		TOTAL_WORDS		+ " INTEGER NOT NULL DEFAULT 0, " +
			            		SOLVED_WORDS 	+ " INTEGER NOT NULL DEFAULT 0 " +
    		");";
    
  
    private int gameNo; // PK
    private int miniClues; 
    private int fullClues; 
    private int totalWords; 
    private int solvedWords; 
    private List<GameWord> gameWords; 
    

    private int maxMiniClues = 10;
    private int maxFullClues = 3;
    
    
    // constructor
	public Game() {}
	
    // constructor
	public Game(int gameNo) {
		this.gameNo = gameNo;
	}

    // constructor
    public Game(Cursor c) {
    	this.setGameNo(c);
        this.setMiniClues(c);
        this.setFullClues(c);
        this.setTotalWords(c);
        this.setSolvedWords(c);
    }    	

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(GAME_NO, getGameNo());
        values.put(MINI_CLUES, getMiniClues());
        values.put(FULL_CLUES, getFullClues());
        values.put(TOTAL_WORDS, getTotalWords());
        values.put(SOLVED_WORDS, getSolvedWords());
        return values;
    }    
    
    public ContentValues getInsertionContentValues() {
        ContentValues values = new ContentValues();
        values.put(GAME_NO, getGameNo());
        values.put(TOTAL_WORDS, getTotalWords());
        return values;
    }    
    
    
	public List<GameWord> getGameWords() {
		return gameWords;
	}

	public void setGameWords(List<GameWord> gameWords) {
		this.gameWords = gameWords;
		if (gameWords != null) {
			this.totalWords = gameWords.size();  // note that this doesn't handle case where calling method gets list and adds to it
			for (GameWord gw : gameWords) {
				gw.setGame(this);
			}
		} else {
			this.totalWords = 0;
		}
	}

	// game no
	public int getGameNo() {
		return gameNo;
	}
	public void setGameNo(int gameNo) {
		this.gameNo = gameNo;
	}
	public void setGameNo(Cursor c) {
		int i = c.getColumnIndex(GAME_NO);
    	if (i != -1) { setGameNo(c.getInt(i)); }
	}

	// mini clues
	public int getMiniClues() {
		return miniClues;
	}
	public void setMiniClues(int miniClues) {
		this.miniClues = miniClues;
	}
	public void setMiniClues(Cursor c) {
		int i = c.getColumnIndex(MINI_CLUES);
    	if (i != -1) { setMiniClues(c.getInt(i)); }
	}
	public String getMiniCluesMenuText() {
		return " (" + (maxMiniClues - miniClues) + " left)";
	}
	public boolean isMiniClueRemaining() {
		return miniClues < maxMiniClues;
	}
	public void setMaxMiniClues(int maxMiniClues) {
		this.maxMiniClues = maxMiniClues; 
	}
	
	// full clues
	public int getFullClues() {
		return fullClues;
	}
	public void setFullClues(int fullClues) {
		this.fullClues = fullClues;
	}
	public void setFullClues(Cursor c) {
		int i = c.getColumnIndex(FULL_CLUES);
    	if (i != -1) { setFullClues(c.getInt(i)); }
	}
	public String getFullCluesMenuText() {
		return " (" + (maxFullClues - fullClues)  + " left)";
	}
	public boolean isFullClueRemaining() {
		return fullClues < maxFullClues;
	}
	public void setMaxFullClues(int maxFullClues) {
		this.maxFullClues = maxFullClues; 
	}

	// total words
	public int getTotalWords() {
		return totalWords;
	}
	public void setTotalWords(int totalWords) {
		this.totalWords = totalWords;
	}
	public void setTotalWords(Cursor c) {
		int i = c.getColumnIndex(TOTAL_WORDS);
    	if (i != -1) { setTotalWords(c.getInt(i)); }
	}

	// solved words
	public int getSolvedWords() {
		return solvedWords;
	}
	public void setSolvedWords(int solvedWords) {
		this.solvedWords = solvedWords;
	}
	public void setSolvedWords(Cursor c) {
		int i = c.getColumnIndex(SOLVED_WORDS);
    	if (i != -1) { setSolvedWords(c.getInt(i)); }
	}
	public boolean isGameComplete() {
		return (solvedWords == totalWords);
	}
	
}

package org.indiv.dls.games.verboscruzados.feature.db;

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

public class Word {
    public static final String TABLE_NAME = "WORD";
    public static final String WORD = "WORD";  // PK
    public static final String RANDOMIZER = "RANDOMIZER";
    public static final String GAME_DISCARDED = "GAME_DISCARDED";
    public static final String PLAY_SOON = "PLAY_SOON";  // 0 or 1 for false or true
    public static final String NEVER_PLAY = "NEVER_PLAY";  // 0 or 1 for false or true
    public static final String DEF_NOT_FOUND = "DEF_NOT_FOUND";  // 0 or 1 for false or true
    public static final String DATE_DEFS_LOADED = "DATE_DEFS_LOADED";
    public static final String DATE_LAST_PLAYED = "DATE_LAST_PLAYED";
    public static final String TIMES_PLAYED = "TIMES_PLAYED";
    public static final String TIMES_SOLVED = "TIMES_SOLVED";

    public static final String TABLE_DELETE = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    public static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
			            		WORD 				+ " TEXT 	NOT NULL PRIMARY KEY, " +
			            		RANDOMIZER 	    	+ " INTEGER NOT NULL, " +
			            		GAME_DISCARDED 		+ " INTEGER NOT NULL DEFAULT 0 CHECK (" + GAME_DISCARDED + " in (0,1)), " +
			            		PLAY_SOON 	 		+ " INTEGER NOT NULL DEFAULT 0 CHECK (" + PLAY_SOON + " in (0,1)), " +
			            		NEVER_PLAY 		    + " INTEGER NOT NULL DEFAULT 0 CHECK (" + NEVER_PLAY + " in (0,1)), " +
			            		DEF_NOT_FOUND 	    + " INTEGER NOT NULL DEFAULT 0 CHECK (" + DEF_NOT_FOUND + " in (0,1)), " +
			            		DATE_DEFS_LOADED 	+ " INTEGER NOT NULL DEFAULT 0, " +
			            		DATE_LAST_PLAYED 	+ " INTEGER NOT NULL DEFAULT 0, " +
			            		TIMES_PLAYED 		+ " INTEGER NOT NULL DEFAULT 0, " +
			            		TIMES_SOLVED 		+ " INTEGER NOT NULL DEFAULT 0);";

    private String word; // PK
    private int randomizer = 0;
    private boolean gameDiscarded = false; 
    private boolean playSoon = false; 
    private boolean neverPlay = false; 
    private boolean defNotFound = false; 
    private Long dateDefinitionsLastLoaded; 
    private Long dateLastPlayed; 
    private int timesPlayed = 0;
    private int timesSolved = 0;
    private List<Definition> definitions;

    // variables used by word placement algorithm to place word in puzzle
    private int lastAcrossPositionTried = -1;
    private int lastDownPositionTried = -1;
    
    // constructor
	public Word() {
		super();
	}
	
    // constructor
	public Word(String word) {
		this.word = word;
	}

	// constructor
	public Word(String word, int randomizer) {
		this.word = word;
		this.randomizer = randomizer;
	}

    // constructor
    public Word(Cursor c) {
    	this.setWord(c);
    	this.setRandomizer(c);
        this.setGameDiscarded(c);
        this.setPlaySoon(c);
        this.setNeverPlay(c);
        this.setDefNotFound(c);
    	this.setDateDefinitionsLastLoaded(c);
        this.setDateLastPlayed(c);
        this.setTimesPlayed(c);
        this.setTimesSolved(c);
    }    

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(WORD, getWord());
        values.put(RANDOMIZER, getRandomizer());
        values.put(GAME_DISCARDED, isGameDiscarded());
        values.put(PLAY_SOON, isPlaySoon());
        values.put(NEVER_PLAY, isNeverPlay());
        values.put(DEF_NOT_FOUND, isDefNotFound());
        values.put(DATE_DEFS_LOADED, getDateDefinitionsLastLoaded());
        values.put(DATE_LAST_PLAYED, getDateLastPlayed());
        values.put(TIMES_PLAYED, getTimesPlayed());
        values.put(TIMES_SOLVED, getTimesSolved());
        return values;
    }    
	
    public ContentValues getInsertionContentValues() {
        ContentValues values = new ContentValues();
        values.put(WORD, getWord());
        values.put(RANDOMIZER, getRandomizer());
        return values;
    }    
 
    
	// getters and setters
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public void setWord(Cursor c) {
		int i = c.getColumnIndex(WORD);
		if (i != -1) { setWord(c.getString(i)); }
	}
	
	
	public int getRandomizer() {
		return randomizer;
	}
	public void setRandomizer(int randomizer) {
		this.randomizer = randomizer;
	}
	public void setRandomizer(Cursor c) {
		int i = c.getColumnIndex(RANDOMIZER);
        if (i != -1) { setRandomizer(c.getInt(i)); }
	}

	public boolean isGameDiscarded() {
		return gameDiscarded;
	}
	public void setGameDiscarded(boolean gameDiscarded) {
		this.gameDiscarded = gameDiscarded;
	}
	public void setGameDiscarded(Cursor c) {
		int i = c.getColumnIndex(GAME_DISCARDED);
		if (i != -1) { setGameDiscarded(c.getInt(i)==1); }
	}
	
	public boolean isPlaySoon() {
		return playSoon;
	}
	public void setPlaySoon(boolean playSoon) {
		this.playSoon = playSoon;
	}
	public void setPlaySoon(Cursor c) {
		int i = c.getColumnIndex(PLAY_SOON);
		if (i != -1) { setPlaySoon(c.getInt(i)==1); }
	}
	
	public boolean isNeverPlay() {
		return neverPlay;
	}
	public void setNeverPlay(boolean neverPlay) {
		this.neverPlay = neverPlay;
	}
	public void setNeverPlay(Cursor c) {
		int i = c.getColumnIndex(NEVER_PLAY);
        if (i != -1) { setNeverPlay(c.getInt(i)==1); }
	}
	
	public boolean isDefNotFound() {
		return defNotFound;
	}
	public void setDefNotFound(boolean defNotFound) {
		this.defNotFound = defNotFound;
	}
	public void setDefNotFound(Cursor c) {
		int i = c.getColumnIndex(DEF_NOT_FOUND);
        if (i != -1) { setDefNotFound(c.getInt(i)==1); }
	}
	
	public Long getDateDefinitionsLastLoaded() {
		return dateDefinitionsLastLoaded;
	}
	public void setDateDefinitionsLastLoaded(Long dateDefinitionsLastLoaded) {
		this.dateDefinitionsLastLoaded = dateDefinitionsLastLoaded;
	}
	public void setDateDefinitionsLastLoaded(Cursor c) {
		int i = c.getColumnIndex(DATE_DEFS_LOADED);
    	if (i != -1) { setDateDefinitionsLastLoaded(c.getLong(i)); }
	}
	
	public Long getDateLastPlayed() {
		return dateLastPlayed;
	}
	public void setDateLastPlayed(Long dateLastPlayed) {
		this.dateLastPlayed = dateLastPlayed;
	}
	public void setDateLastPlayed(Cursor c) {
		int i = c.getColumnIndex(DATE_LAST_PLAYED);
        if (i != -1) { setDateLastPlayed(c.getLong(i)); }
	}
	
	public int getTimesPlayed() {
		return timesPlayed;
	}
	public void setTimesPlayed(int timesPlayed) {
		this.timesPlayed = timesPlayed;
	}
	public void setTimesPlayed(Cursor c) {
		int i = c.getColumnIndex(TIMES_PLAYED);
        if (i != -1) { setTimesPlayed(c.getInt(i)); }
	}
	
	public int getTimesSolved() {
		return timesSolved;
	}
	public void setTimesSolved(int timesSolved) {
		this.timesSolved = timesSolved;
	}
	public void setTimesSolved(Cursor c) {
		int i = c.getColumnIndex(TIMES_SOLVED);
        if (i != -1) { setTimesSolved(c.getInt(i)); }
	}

	
	public List<Definition> getDefinitions() {
		return definitions;
	}
	public void setDefinitions(List<Definition> definitions) {
		this.definitions = definitions;
	}
	
	public String toString() {
		return getWord();
	}
	
	public int getLastPositionTried(boolean across) {
		return across? this.lastAcrossPositionTried : this.lastDownPositionTried;
	}
   
	public void setLastPositionTried(boolean across, int position) {
		if (across) {
			this.lastAcrossPositionTried = position;
		} else {
			this.lastDownPositionTried = position;
		}
	}
}


package org.indiv.dls.games.vocabrecall.feature.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

@SuppressLint("DefaultLocale")  // because using toUpperCase() with default locale
public class DictionaryDbOpenHelper extends SQLiteOpenHelper {

    public static final String KEY_ID = "rowid"; // content provider requires an integer PK, but since we don't have one, just use built-in rowid
    public static final String TABLE_WORD = Word.TABLE_NAME;
    public static final String TABLE_DEFINITION = Definition.TABLE_NAME;
    public static final String TABLE_GAMEWORD = GameWord.TABLE_NAME;
    public static final String TABLE_GAME = Game.TABLE_NAME;


    private static final String TAG = DictionaryDbOpenHelper.class.getSimpleName();
    private static final int DB_VERSION = 11; // 7in emulator is on v11
    private static final String DB_NAME = "DICTIONARY";
    private final Context mContext;
    private SQLiteDatabase mDatabase;
    private boolean mDbLoaded = true; // init to true, set to false only on create or upgrade

    // constructor
    public DictionaryDbOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        mDatabase = db;
        createDatabase();
        mDbLoaded = false;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        mDatabase = db;

        //enable fk constraints
        db.execSQL("PRAGMA foreign_keys=ON;");
//        db.setForeignKeyConstraintsEnabled(true); // use this instead when min API level is >= 16


//		deleteDatabase();
//		createDatabase();
//        mDbLoaded = false;

//        clearOutDatabase();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        mDatabase = db;
//		deleteDatabase();
//		createDatabase();

//        clearOutDatabase();
//        mDbLoaded = false;

/*
		mDatabase.beginTransaction();
		try {
//	        mDatabase.delete(Definition.TABLE_NAME, Definition.WORD + " not in (select "+GameWord.WORD+" from "+GameWord.TABLE_NAME+")", null);
	        mDatabase.delete(Definition.TABLE_NAME, null, null);
	        ContentValues values = new ContentValues();
	        values.put(Word.DATE_DEFS_LOADED, 0);
	        values.put(Word.GAME_DISCARDED, 0);
	        mDatabase.update(Word.TABLE_NAME, values, Word.WORD + " not in (select word from DEFINITION)", null);
		    mDatabase.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		} finally {
		    mDatabase.endTransaction();
		}
*/
    }


    //---------------------------------------------------//
    //------------ end of overridden methods ------------//
    //---------------------------------------------------//

    private void clearOutDatabase() {
//  try {
//  	// delete all defs except those used by current game, delete unused words (keep Game and GameWord records to retain current game, and used Word records for stats)
//  	mDatabase.delete(Definition.TABLE_NAME, Definition.WORD + " not in (select "+GameWord.WORD+" from "+GameWord.TABLE_NAME+")", null);
//  	mDatabase.delete(Word.TABLE_NAME, Word.TIMES_PLAYED+"=0", null);
//  } catch (SQLException e) {
//  	Log.e(TAG, "error upgrading database: " + e.getMessage());
//  	throw e;
//  }

        mDatabase.beginTransaction();
        try {
//	        mDatabase.delete(Definition.TABLE_NAME, Definition.WORD + " not in (select "+GameWord.WORD+" from "+GameWord.TABLE_NAME+")", null);
            mDatabase.delete(Definition.TABLE_NAME, null, null);
            ContentValues values = new ContentValues();
            values.put(Word.DEF_NOT_FOUND, 0);
            values.put(Word.DATE_DEFS_LOADED, 0);
            values.put(Word.GAME_DISCARDED, 0);
            mDatabase.update(Word.TABLE_NAME, values, Word.WORD + " not in (select word from DEFINITION)", null);
            mDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            mDatabase.endTransaction();
        }
    }


    public boolean isDbLoaded() {
        return mDbLoaded;
    }

    public void setDbLoaded(boolean mDbLoaded) {
        this.mDbLoaded = mDbLoaded;
    }

//
//    /**
//     * load definitions into Word object
//     * @return list
//     */
//    public void loadWordDefinitions(Word word) {
//    	List<Definition> definitions = new ArrayList<Definition>();
//    	String[] args = { word.getWord() };
//    	Cursor c = mDatabase.query(Definition.TABLE_NAME, null, GameWord.WORD + "=?", args, null, null, Definition.DEF_ORDER);
//    	while (c.moveToNext()) {
//    		definitions.add(new Definition(c));
//    	}
//    	c.close();
//    	word.setDefinitions(definitions);
//    }
//
//
//    /**
//     * save definitions of Word objects
//     *
//     * @return list
//     */
//    public void saveWordDefinitions(List<Word> words, boolean deletePreviousDefs) {
//		// perform as transaction
//		mDatabase.beginTransaction();
//		try {
//			for (Word word: words) {
//		    	if (word.getDefinitions() == null || word.getDefinitions().size() == 0) {
//		    		setWordDefNotFound(word.getWord(), true);
//		    	} else {
//        			if (deletePreviousDefs) {
//        				deleteWordDefinitions(word.getWord());
//        			}
//            		for (Definition definition : word.getDefinitions()) {
//            	        mDatabase.insertOrThrow(Definition.TABLE_NAME, null, definition.getContentValues());
//            		}
//            		setDateDefsLoaded(word.getWord());
//    			}
//			}
//		    mDatabase.setTransactionSuccessful();
//		} catch (Exception e) {
//			Log.e(TAG, e.getMessage());
//		} finally {
//		    mDatabase.endTransaction();
//		}
//    }
//
//    /**
//     * release definitions (e.g. for words that won't be played again for awhile)
//     * @return list
//     */
//    public void releaseDefinitions(List<Word> words) {
//		// perform as transaction
//		mDatabase.beginTransaction();
//		try {
//			for (Word word : words) {
//	    		deleteWordDefinitions(word.getWord());
//	    	    setDefsDiscarded(word.getWord());
//			}
//		    mDatabase.setTransactionSuccessful();
//		} catch (Exception e) {
//			Log.e(TAG, e.getMessage());
//		} finally {
//		    mDatabase.endTransaction();
//		}
//    }
//
//    /**
//     * Add a game word to the database
//     * @return rowId or -1 if failed
//     */
//    public void saveGame(Game game) {
//		// perform as transaction
//		mDatabase.beginTransaction();
//		try {
//	        mDatabase.insertOrThrow(Game.TABLE_NAME, null, game.getInsertionContentValues());
//			for (GameWord gameWord : game.getGameWords()) {
//		        mDatabase.insertOrThrow(GameWord.TABLE_NAME, null, gameWord.getContentValues());
//				updateTimesWordPlayed(gameWord.getWordInfo());
//			}
//			setWordsDiscarded(game.getGameNo(), false); // in case we're playing words that have been marked as discarded
//		    mDatabase.setTransactionSuccessful();
//		} catch (Exception e) {
//			Log.e(TAG, e.getMessage());
//		} finally {
//		    mDatabase.endTransaction();
//		}
//    }
//
//    /**
//     * update a word in the dictionary.
//     * @return number of rows updated
//     */
//    public long updateGameWordUserEntry(GameWord gameWord) {
//    	String[] args = { gameWord.getWord() };
//        return mDatabase.update(GameWord.TABLE_NAME, gameWord.getUserEntryContentValues(),
//        						GameWord.WORD + "=?", args);
//    }
//
//
//    /**
//     * get list of game words from the database
//     * @return list
//     */
//    public Game getCurrentGame() {
//    	int gameNo = getCurrentGameNo();
//    	Game game = null;
//    	if (gameNo > 0) {
//
//    		// load game words
//	    	List<GameWord> gameWords = new ArrayList<GameWord>();
//	    	String[] args = { String.valueOf(gameNo) };
//	    	Cursor c = mDatabase.query(GameWord.TABLE_NAME, null, GameWord.GAME_NO + "=?", args, null, null, null);
//	    	while (c.moveToNext()) {
//	    		gameWords.add(new GameWord(c));
//	    	}
//	    	c.close();
//
//	    	// if any game words found
//	    	if (gameWords.size() > 0) {
//
//	    		// load game
//		    	c = mDatabase.query(Game.TABLE_NAME, null, GameWord.GAME_NO + "=?", args, null, null, null);
//		    	if (c.moveToNext()) {
//		    		game = new Game(c);
//		    		game.setGameWords(gameWords);
//		    	}
//		    	c.close();
//
//		    	// load Word record with definitions into each gameWord
//		    	for (GameWord gameWord : gameWords) {
//		    		Word word = new Word(gameWord.getWord());
//		    		gameWord.setWordInfo(word);
//		    		loadWordDefinitions(word);
//		    	}
//	    	} else {
//	    		// this will only happen if there was a problem saving the last game.
//	    		// create empty game object to contain game #
//	    		game = new Game(gameNo);
//	    	}
//    	}
//    	return game;
//    }
//
//
//    /**
//     * delete game from the database
//     */
//    public void deleteGame(int gameNo) {
//    	String[] gameNoArgs = { String.valueOf(gameNo) };
//
//		// perform as transaction
//		mDatabase.beginTransaction();
//		try {
//			int lastCompletedGameNo = getGameNoLastCompleted();
//			boolean gameCompleted = (gameNo == lastCompletedGameNo);
//
//			// only mark words for discard if game was completed
//			if (gameCompleted) {
//				setWordsDiscarded(gameNo, true);
//			}
//
//	        // delete game words
//	        mDatabase.delete(GameWord.TABLE_NAME, GameWord.GAME_NO + "=?", gameNoArgs);
//
//	        // delete game if incomplete, otherwise keep record of it for stats
//			if (!gameCompleted) {
//				mDatabase.delete(Game.TABLE_NAME, Game.SOLVED_WORDS + "!=" + Game.TOTAL_WORDS + " AND " + Game.GAME_NO + "=?", gameNoArgs);
//			}
//
//		    mDatabase.setTransactionSuccessful();
//		} finally {
//		    mDatabase.endTransaction();
//		}
//    }
//
//
//	  /**
//	  * get count of games completed
//	  */
//	 public int getGamesCompleted() {
//	  	 String[] columns = { "count(*)" };
//	  	 Cursor c = mDatabase.query(Game.TABLE_NAME, columns, Game.SOLVED_WORDS + "=" + Game.TOTAL_WORDS, null, null, null, null);
//	     c.moveToFirst();
//	     int count = c.getInt(0);
//	     c.close();
//	     return count;
//	 }

	 public int getGameNoLastCompleted() {
	  	 String[] columns = { "max(" + Game.GAME_NO + ")" };
	  	 Cursor c = mDatabase.query(Game.TABLE_NAME, columns, Game.SOLVED_WORDS + "=" + Game.TOTAL_WORDS, null, null, null, null);
	     c.moveToFirst();
	     int gameNo = 0;
	     if (!c.isNull(0)) {
		     gameNo = c.getInt(0);
	     }
	     c.close();
	     return gameNo;
	 }

//	  /**
//	  * get count of words of games completed
//	  */
//	 public int getWordCountOfGamesCompleted() {
//	 	 String[] columns = { "sum(" + Game.TOTAL_WORDS + ")" };
//	 	 Cursor c = mDatabase.query(Game.TABLE_NAME, columns, Game.SOLVED_WORDS + "=" + Game.TOTAL_WORDS, null, null, null, null);
//	     c.moveToFirst();
//	     int count = c.getInt(0);
//	     c.close();
//	     return count;
//	 }


	  /**
	  * get count of words not yet solved
	  */
	 private int countWordsNotYetSolved() {
		 // query: select count(*) from Word where TIMES_SOLVED=0 and NEVER_PLAY=0
	 	 String[] columns = { "count(*)" };
	 	 Cursor c = mDatabase.query(Word.TABLE_NAME, columns, Word.TIMES_SOLVED + "=0 AND " + Word.NEVER_PLAY + "=0", null, null, null, null);
	     c.moveToFirst();
	     int count = c.getInt(0);
	     c.close();
	     return count;
	 }

	  /**
	  * get count of words not yet solved
	  */
	 public WordsSolvedStats getWordsSolvedStats() {
		 WordsSolvedStats stats = new WordsSolvedStats();
		 stats.wordsNotYetSolved = countWordsNotYetSolved();

		 // query: select TIMES_SOLVED, count(*) from WORD group by TIMES_SOLVED order by TIMES_SOLVED
	 	 String[] columns = { Word.TIMES_SOLVED, "count(*)" };
	 	 Cursor c = mDatabase.query(Word.TABLE_NAME, columns, Word.TIMES_SOLVED + ">0", null, Word.TIMES_SOLVED, null, Word.TIMES_SOLVED);
    	 while (c.moveToNext()) {
    		 int timesSolved = c.getInt(0);
    		 int wordCount = c.getInt(1);
    		 switch (timesSolved) {
    		 	case 1: stats.wordsSolvedOnce = wordCount; break;
    		 	case 2: stats.wordsSolvedTwice = wordCount; break;
    		 	case 3: stats.wordsSolved3Times = wordCount; break;
    		 	default: stats.wordsSolvedMoreThan3Times += wordCount; break;
    		 }
     	 }
	     c.close();

	     return stats;
	 }

	 public class WordsSolvedStats {
		 public int wordsNotYetSolved = 0;
		 public int wordsSolvedOnce = 0;
		 public int wordsSolvedTwice = 0;
		 public int wordsSolved3Times = 0;
		 public int wordsSolvedMoreThan3Times = 0;
	 }

//	 /**
//     * set game as complete
//     */
//    public void markGameComplete(Game game) {
//
// 		// perform as transaction
// 		mDatabase.beginTransaction();
// 		try {
// 			// mark game as complete
// 	    	game.setSolvedWords(game.getTotalWords());
// 	    	String[] args = { ""+game.getGameNo() };
// 	    	ContentValues values = new ContentValues();
// 	        values.put(Game.SOLVED_WORDS, game.getSolvedWords());
// 	        mDatabase.update(Game.TABLE_NAME, values, Game.GAME_NO + "=?", args);
//
// 	        // update times solved in Word records
//     		for (GameWord gameWord : game.getGameWords()) {
//      	       updateTimesWordSolved(gameWord.getWordInfo());
//     		}
//
//     		mDatabase.setTransactionSuccessful();
// 		} catch (Exception e) {
// 			Log.e(TAG, e.getMessage());
// 		} finally {
// 		    mDatabase.endTransaction();
// 		}
//    }
//
//    /**
//     * increment mini clues given
//     */
//    public void saveMiniClues(Game game) {
//    	String[] args = { ""+game.getGameNo() };
//    	ContentValues values = new ContentValues();
//        values.put(Game.MINI_CLUES, game.getMiniClues());
//        mDatabase.update(Game.TABLE_NAME, values, Game.GAME_NO + "=?", args);
//    }
//
//    /**
//     * increment full clues given
//     */
//    public void saveFullClues(Game game) {
//    	String[] args = { ""+game.getGameNo() };
//        ContentValues values = new ContentValues();
//        values.put(Game.FULL_CLUES, game.getFullClues());
//        mDatabase.update(Game.TABLE_NAME, values, Game.GAME_NO + "=?", args);
//    }
//
//
//
//    /**
//     * set play again soon
//     */
//    public void setWordPlaySoon(String word, boolean playSoon) {
//    	String[] args = { word };
//        ContentValues values = new ContentValues();
//        values.put(Word.PLAY_SOON, playSoon);
//        mDatabase.update(Word.TABLE_NAME, values, Word.WORD + "=?", args);
//    }
//
//    /**
//     * set never play again
//     */
//    public void setWordNeverPlay(String word, boolean neverPlay) {
//    	String[] args = { word };
//        ContentValues values = new ContentValues();
//        values.put(Word.NEVER_PLAY, neverPlay);
//        mDatabase.update(Word.TABLE_NAME, values, Word.WORD + "=?", args);
//    }
//
//
//    /**
//     * get list of game words from the database
//     * @return list
//     */
//    public List<Word> getNextWordsList(long minNumWords) {
//    	// make sure the ranges for these calls do not overlap or duplicates will occur
//    	List<Word> words = getNextWordsList(minNumWords, 6, 100); // get long words
//    	words.addAll(getNextWordsList(minNumWords, 2, 5)); // get short words
//    	return words;
//    }
//
//
//    /**
//	 * get list of words for which definitions need to be retrieved
//	 * @return list
//	 */
//	public List<Word> getWordsNeedingDefinitions(long numWords) {
//		List<Word> words = new ArrayList<Word>();
//		String[] resultWords = { Word.WORD };
//		Cursor c = mDatabase.query(Word.TABLE_NAME, resultWords,
//				Word.NEVER_PLAY + "=0 AND " + Word.DATE_DEFS_LOADED + "=0",
//	            null, null, null, Word.DEF_NOT_FOUND + ", " + Word.TIMES_SOLVED + ", " + Word.TIMES_PLAYED  + ", " + Word.RANDOMIZER, ""+numWords);
//		while (c.moveToNext()) {
//			words.add(new Word(c));
//		}
//		c.close();
//		return words;
//	}
//
//	/**
//	 * get list of words for which definitions can be released
//	 * @return list
//	 */
//	public List<Word> getWordsNotNeedingDefinitions(long numWords) {
//		List<Word> words = new ArrayList<Word>();
//		String[] resultWords = { Word.WORD };
//		Cursor c = mDatabase.query(Word.TABLE_NAME, resultWords,
//				Word.DATE_DEFS_LOADED + ">0 AND " + Word.GAME_DISCARDED + "=1 AND " + Word.PLAY_SOON + "=0", null, null, null,
//	            Word.TIMES_SOLVED + " desc, " + Word.TIMES_PLAYED + " desc, " + Word.DATE_LAST_PLAYED + " desc", ""+numWords);
//		while (c.moveToNext()) {
//			words.add(new Word(c));
//		}
//		c.close();
//		return words;
//	}
//
//	  /**
//	  * get count of words having definitions loaded
//	  * @return rowId or -1 if failed
//	  */
//	 public int getNumWordsWithDefinitions() {
//    	 String[] columns = { "count(*)" };
//    	 Cursor c = mDatabase.query(Word.TABLE_NAME, columns, Word.DATE_DEFS_LOADED + ">0 AND " + Word.NEVER_PLAY + "=0" , null, null, null, null);
//	     c.moveToFirst();
//	     int count = c.getInt(0);
//	     c.close();
//	     return count;
//	 }
//
//
//
//	/**
//     * Add a word to the dictionary. Persists word and randomizer values. Other values are default.
//     * @return rowId or -1 if failed
//     */
//     public long addWords(List<Word> words) {
// 		// perform as transaction
// 		mDatabase.beginTransaction();
// 		long wordsAdded = 0;
// 		try {
//     		for (Word word : words) {
//     			long rowId = mDatabase.insert(Word.TABLE_NAME, null, word.getInsertionContentValues());
//                if (rowId < 0) {
//	                //Log.e(TAG, "unable to add word: " + word.getWord());  // this will be normal during upgrade
//	            } else {
//     				wordsAdded++;
//     			}
//     		}
// 		    mDatabase.setTransactionSuccessful();
// 		} catch (Exception e) {
// 			Log.e(TAG, e.getMessage());
// 		} finally {
// 		    mDatabase.endTransaction();
// 		}
//    	return wordsAdded;
//     }
//
//
//     public List<Object> queryDebug(String tableName, String whereClause, String orderByClause, String limitClause) {
//     	List<Object> results = new ArrayList<Object>();
//     	Cursor c = mDatabase.query(tableName, null, whereClause, null, null, null, orderByClause, limitClause);
//     	while (c.moveToNext()) {
//     		if (Word.TABLE_NAME.equals(tableName)) {
//         		results.add(new Word(c));
//     		} else if (Definition.TABLE_NAME.equals(tableName)) {
//         		results.add(new Definition(c));
//     		} else if (GameWord.TABLE_NAME.equals(tableName)) {
//         		results.add(new GameWord(c));
//     		} else if (Game.TABLE_NAME.equals(tableName)) {
//         		results.add(new Game(c));
//     		}
//     	}
//     	c.close();
//
//     	return results;
//     }

    //---------------------------------------------------//
    //------------ private methods ------------//
    //---------------------------------------------------//
    private void createDatabase() {
        try {
            // create tables, indexes
            mDatabase.execSQL(Word.TABLE_CREATE);
            mDatabase.execSQL(Game.TABLE_CREATE);
            mDatabase.execSQL(GameWord.TABLE_CREATE);    // FKs to Word, Game
            mDatabase.execSQL(Definition.TABLE_CREATE); // FK to Word

            mDatabase.execSQL(Definition.FK_INDEX_CREATE);
            mDatabase.execSQL(GameWord.FK_INDEX_CREATE);
        } catch (SQLException e) {
            Log.e(TAG, "error creating database: " + e.getMessage());
            throw e;
        }
    }
//
//    private void deleteDatabase() {
//        try {
//            // delete tables, indexes
//            mDatabase.execSQL(Definition.TABLE_DELETE);
//            mDatabase.execSQL(GameWord.TABLE_DELETE);
//            mDatabase.execSQL(Word.TABLE_DELETE);
//            mDatabase.execSQL(Game.TABLE_DELETE);
//        } catch (SQLException e) {
//            Log.e(TAG, "error upgrading database: " + e.getMessage());
//            throw e;
//        }
//    }


    private int getCurrentGameNo() {
    	int gameNo = 0;
    	String[] columns = { "max(" + Game.GAME_NO + ")" };  // get max game_no from game table
    	Cursor c = mDatabase.query(Game.TABLE_NAME, columns, null, null, null, null, null);
    	try {
	    	if (c.moveToNext()) {
	    		gameNo = c.getInt(0); // may throw exception if no games
	    	}
    	} catch (Exception e) {
    		gameNo = 0;
    	}
    	c.close();
    	return gameNo;
    }

//    /**
//     * get list of game words from the database
//     * @return list
//     */
//    private List<Word> getNextWordsList(long minNumWords, int minLength, int maxLength) {
//    	List<Word> words = new ArrayList<Word>();
//    	String[] resultWords = { Word.WORD, Word.TIMES_PLAYED, Word.TIMES_SOLVED }; // need times played and solved so we can increment later
//    	String whereClause = Word.NEVER_PLAY + "=0 AND " + Word.DATE_DEFS_LOADED + ">0 and length(" + Word.WORD + ") between " + minLength + " and " + maxLength;
//    	String orderByClause = Word.PLAY_SOON + " desc, " + Word.GAME_DISCARDED + ", " + Word.TIMES_SOLVED + ", " + Word.TIMES_PLAYED + ", " +
//				Word.DATE_LAST_PLAYED + ", " + Word.RANDOMIZER;  // note that all but last sorting column may be zero, making randomizer significant
//    	Cursor c = mDatabase.query(Word.TABLE_NAME, resultWords, whereClause, null, null, null, orderByClause, ""+minNumWords); // with limit clause
//    	while (c.moveToNext()) {
//    		words.add(new Word(c));
//    	}
//    	c.close();
//
//    	return words;
//    }
//

    /**
     * update times word played
     */
    private void updateTimesWordPlayed(Word word) {
    	word.setTimesPlayed(word.getTimesPlayed() + 1);
    	String[] args = { word.getWord() };
        ContentValues values = new ContentValues();
        values.put(Word.PLAY_SOON, false); // considering doing this after word solved in subsequent game
        values.put(Word.DATE_LAST_PLAYED, new Date().getTime());
        values.put(Word.TIMES_PLAYED, word.getTimesPlayed());
        mDatabase.update(Word.TABLE_NAME, values, Word.WORD + "=?", args);
    }

//    /**
//     * update times word solved
//     */
//    private void updateTimesWordSolved(Word word) {
//    	word.setTimesSolved(word.getTimesSolved() + 1);
//    	String[] args = { word.getWord() };
//        ContentValues values = new ContentValues();
//        values.put(Word.TIMES_SOLVED, word.getTimesSolved());
//        mDatabase.update(Word.TABLE_NAME, values, Word.WORD + "=?", args);
//    }
//


    /**
     * set defs not loadable
     */
    private void setWordDefNotFound(String word, boolean notFound) {
    	String[] args = { word };
        ContentValues values = new ContentValues();
        values.put(Word.DEF_NOT_FOUND, notFound);
        mDatabase.update(Word.TABLE_NAME, values, Word.WORD + "=?", args);
    }

    /**
     * set words discarded
     */
    private void setWordsDiscarded(int gameNo, boolean discarded) {
    	// set game_discarded flag in WORDs that won't be played in a while (so definitions can be discarded from cache)
    	String[] gameNoArgs = { String.valueOf(gameNo) };
    	ContentValues values = new ContentValues();
        values.put(Word.GAME_DISCARDED, discarded);
        mDatabase.update(Word.TABLE_NAME, values, Word.PLAY_SOON + "=0 AND " + Word.WORD + " IN (SELECT WORD FROM GAME_WORD WHERE " + GameWord.GAME_NO + "=?)", gameNoArgs);
    }

    /**
     * set date defs loaded
     */
    private void setDateDefsLoaded(String word) {
    	String[] args = { word };
        ContentValues values = new ContentValues();
        values.put(Word.DEF_NOT_FOUND, 0);
        values.put(Word.DATE_DEFS_LOADED, new Date().getTime());
        mDatabase.update(Word.TABLE_NAME, values, Word.WORD + "=?", args);
    }

    /**
     * set defs discarded
     */
    private void setDefsDiscarded(String word) {
    	String[] args = { word };
        ContentValues values = new ContentValues();
        values.put(Word.DATE_DEFS_LOADED, 0);
        values.put(Word.GAME_DISCARDED, 0);
        mDatabase.update(Word.TABLE_NAME, values, Word.WORD + "=?", args);
    }



    /**
     * delete existing word definitions from the database
     */
    private void deleteWordDefinitions(String word) {
    	String[] args = { word };
    	mDatabase.delete(Definition.TABLE_NAME, Definition.WORD + "=?", args);
    }

//
//    /**
//     * get total count of definitions
//     */
//	 public int getTotalNumDefinitions() {
//	   	 String[] columns = { "count(*)" };
//	   	 Cursor c = mDatabase.query(Definition.TABLE_NAME, columns, null, null, null, null, null);
//	     c.moveToFirst();
//	     int count = c.getInt(0);
//	     c.close();
//	     return count;
//	 }
//
//    /**
//     * get total count of game words
//     */
//    public int getTotalNumGameWords() {
//        String[] columns = {"count(*)"};
//        Cursor c = mDatabase.query(GameWord.TABLE_NAME, columns, null, null, null, null, null);
//        c.moveToFirst();
//        int count = c.getInt(0);
//        c.close();
//        return count;
//    }
//
//    /**
//     * Add a word to the dictionary. Persists word and randomizer values. Other values are default.
//     *
//     * @return rowId or -1 if failed
//     */
//    public long addWord(Word word) {
//        return mDatabase.insert(Word.TABLE_NAME, null, word.getInsertionContentValues());
//    }

}

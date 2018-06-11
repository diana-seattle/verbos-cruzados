package org.indiv.dls.games.vocabrecall.feature.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.indiv.dls.games.vocabrecall.feature.content.VocabContentProvider;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;

public class ContentHelper {

	private static final String TAG = ContentHelper.class.getSimpleName();


	private final Context mContext;
	
    // constructor
	public ContentHelper(Context context) {
        mContext = context;
    }


    public boolean isDbLoaded() {
    	ContentResolver cr = mContext.getContentResolver();
    	Bundle b = cr.call(VocabContentProvider.CONTENT_URI, VocabContentProvider.METHOD_ISDBLOADED, null, null);
		return b.getBoolean(VocabContentProvider.METHOD_ISDBLOADED);
	}

	public void setDbLoaded() {
    	ContentResolver cr = mContext.getContentResolver();
    	Bundle b = cr.call(VocabContentProvider.CONTENT_URI, VocabContentProvider.METHOD_SETDBLOADED, null, null);
    	if (!b.getBoolean(VocabContentProvider.METHOD_SETDBLOADED)) {
    		throw new RuntimeException("Unable to set DB loaded");
    	}
	}
	
	
    /**
     * load definitions into Word object
     * @return list
     */
    public void loadWordDefinitions(Word word) {
    	ContentResolver cr = mContext.getContentResolver();
    	List<Definition> definitions = new ArrayList<Definition>();
    	String[] args = { word.getWord() };
    	Cursor c = cr.query(VocabContentProvider.CONTENT_URI_DEFS, null, Definition.WORD + "=?", args, Definition.DEF_ORDER);
    	while (c.moveToNext()) {
    		definitions.add(new Definition(c));
    	}
    	c.close();
    	word.setDefinitions(definitions);
    }
    
    
	/**
     * save definitions of Word objects
     * @return list
     */
    public void saveWordDefinitions(List<Word> words, boolean deletePreviousDefs) throws OperationApplicationException, RemoteException {
    	ContentResolver cr = mContext.getContentResolver();

    	// batch together operations for atomic transaction
    	ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		boolean onFirstDefinitionOfWord;
		for (Word word: words) {
	    	if (word.getDefinitions() == null || word.getDefinitions().size() == 0) {
	    		ops.add(getOpSetWordDefNotFound(word.getWord(), true));
	    	} else {
    			if (deletePreviousDefs) {
    				ops.add(getOpDeleteWordDefinitions(word.getWord()));
    			}
    			onFirstDefinitionOfWord = true;
        		for (Definition definition : word.getDefinitions()) {
        	        ops.add(ContentProviderOperation.newInsert(VocabContentProvider.CONTENT_URI_DEFS
        	        			.buildUpon().appendQueryParameter(VocabContentProvider.PARAM_INSERTORTHROW, "true").build())
        		    	.withValues(definition.getContentValues()).withYieldAllowed(onFirstDefinitionOfWord).build());
        	        onFirstDefinitionOfWord = false;
        		}
        		ops.add(getOpSetDateDefsLoaded(word.getWord()));
			}
		}
		cr.applyBatch(VocabContentProvider.AUTHORITY, ops);
    }
    
    
    /**
     * release definitions (e.g. for words that won't be played again for awhile) 
     * @return list
     */
    public void releaseDefinitions(List<Word> words) throws OperationApplicationException, RemoteException {
    	ContentResolver cr = mContext.getContentResolver();

    	// batch together operations for atomic transaction
    	ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		for (Word word : words) {
			ops.add(getOpDeleteWordDefinitions(word.getWord()));
			ops.add(getOpSetDefsDiscarded(word.getWord()));
		}
		cr.applyBatch(VocabContentProvider.AUTHORITY, ops);
    }
    
    
    /**
     * Add a game word to the database
     * @return rowId or -1 if failed
     */
    public void saveGame(Game game) throws OperationApplicationException, RemoteException {
    	ContentResolver cr = mContext.getContentResolver();
    	
    	// batch together operations for atomic transaction
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newInsert(VocabContentProvider.CONTENT_URI_GAMES
    			.buildUpon().appendQueryParameter(VocabContentProvider.PARAM_INSERTORTHROW, "true").build())
	    	.withValues(game.getInsertionContentValues()).build());
		for (GameWord gameWord : game.getGameWords()) {
	        ops.add(ContentProviderOperation.newInsert(VocabContentProvider.CONTENT_URI_GAMEWORDS
        			.buildUpon().appendQueryParameter(VocabContentProvider.PARAM_INSERTORTHROW, "true").build())
		    	.withValues(gameWord.getContentValues()).build());
			ops.add(getOpUpdateTimesWordPlayed(gameWord.getWordInfo()));
		}
		ops.add(getOpSetWordsDiscarded(game.getGameNo(), false)); // in case we're playing words that have been marked as discarded
		cr.applyBatch(VocabContentProvider.AUTHORITY, ops);
    }       

    
    
    /**
     * update a word in the dictionary.
     * @return number of rows updated
     */
    public long updateGameWordUserEntry(GameWord gameWord) {
    	ContentResolver cr = mContext.getContentResolver();
    	return cr.update(VocabContentProvider.CONTENT_URI_GAMEWORDS, gameWord.getUserEntryContentValues(), 
        						GameWord.WORD + "=?", new String[] { gameWord.getWord() });
    }    
    
    
    /**
     * get list of game words from the database
     * @return list
     */
    public Game getCurrentGame() {
    	ContentResolver cr = mContext.getContentResolver();
    	int gameNo = getCurrentGameNo();
    	Game game = null;
    	if (gameNo > 0) {
    		
    		// load game words
	    	List<GameWord> gameWords = new ArrayList<GameWord>();
	    	String[] args = { String.valueOf(gameNo) };
	    	Cursor c = cr.query(VocabContentProvider.CONTENT_URI_GAMEWORDS, null, GameWord.GAME_NO + "=?", args, null);
	    	while (c.moveToNext()) {
	    		gameWords.add(new GameWord(c));
	    	}
	    	c.close();

	    	// if any game words found
	    	if (gameWords.size() > 0) {
	    		
	    		// load game
		    	c = cr.query(VocabContentProvider.CONTENT_URI_GAMES, null, GameWord.GAME_NO + "=?", args, null);
		    	if (c.moveToNext()) {
		    		game = new Game(c);
		    		game.setGameWords(gameWords);
		    	}
		    	c.close();
		    	
		    	// load Word record with definitions into each gameWord
		    	for (GameWord gameWord : gameWords) {
		    		Word word = new Word(gameWord.getWord());
		    		gameWord.setWordInfo(word);
		    		loadWordDefinitions(word);
		    	}
	    	} else {
	    		// this will only happen if there was a problem saving the last game.
	    		// create empty game object to contain game #
	    		game = new Game(gameNo); 
	    	}
    	}    	
    	return game;
    }
    
    
    /**
     * delete game from the database
     */
    public void deleteGame(int gameNo) throws OperationApplicationException, RemoteException {
    	String[] gameNoArgs = { String.valueOf(gameNo) };

		int lastCompletedGameNo = getGameNoLastCompleted();
		boolean gameCompleted = (gameNo == lastCompletedGameNo);

    	ContentResolver cr = mContext.getContentResolver();

    	// batch together operations for atomic transaction
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		
		// only mark words for discard if game was completed
		if (gameCompleted) {
			ops.add(getOpSetWordsDiscarded(gameNo, true));
		}
    	
        // delete game words
		ops.add(ContentProviderOperation.newDelete(VocabContentProvider.CONTENT_URI_GAMEWORDS).withSelection(GameWord.GAME_NO + "=?", gameNoArgs).build());
        
        // delete game if incomplete, otherwise keep record of it for stats
		if (!gameCompleted) {
			ops.add(ContentProviderOperation.newDelete(VocabContentProvider.CONTENT_URI_GAMES)
					.withSelection(Game.SOLVED_WORDS + "!=" + Game.TOTAL_WORDS + " AND " + Game.GAME_NO + "=?", gameNoArgs).build());
		}
		cr.applyBatch(VocabContentProvider.AUTHORITY, ops);
    }
    
        
	  /**
	  * get count of games completed
	  */
	 public int getGamesCompleted() {
     	 ContentResolver cr = mContext.getContentResolver();
	  	 String[] columns = { "count(*)" };
	  	 Cursor c = cr.query(VocabContentProvider.CONTENT_URI_GAMES, columns, Game.SOLVED_WORDS + "=" + Game.TOTAL_WORDS, null, null);
	     c.moveToFirst();
	     int count = c.getInt(0);
	     c.close();
	     return count;
	 }   
	 
	 public int getGameNoLastCompleted() {
	     ContentResolver cr = mContext.getContentResolver();
	  	 String[] columns = { "max(" + Game.GAME_NO + ")" };
	  	 Cursor c = cr.query(VocabContentProvider.CONTENT_URI_GAMES, columns, Game.SOLVED_WORDS + "=" + Game.TOTAL_WORDS, null, null);
	     c.moveToFirst();
	     int gameNo = 0;
	     if (!c.isNull(0)) {
		     gameNo = c.getInt(0);
	     }
	     c.close();
	     return gameNo;
	 }
    
	  /**
	  * get count of words of games completed
	  */
	 public int getWordCountOfGamesCompleted() {
	     ContentResolver cr = mContext.getContentResolver();
	 	 String[] columns = { "sum(" + Game.TOTAL_WORDS + ")" };
	 	 Cursor c = cr.query(VocabContentProvider.CONTENT_URI_GAMES, columns, Game.SOLVED_WORDS + "=" + Game.TOTAL_WORDS, null, null);
	     c.moveToFirst();
	     int count = c.getInt(0);
	     c.close();
	     return count;
	 }   

	 
	  /**
	  * get count of words not yet solved
	  */
	 private int countWordsNotYetSolved() {
	     ContentResolver cr = mContext.getContentResolver();
		 // query: select count(*) from Word where TIMES_SOLVED=0 and NEVER_PLAY=0
	 	 String[] columns = { "count(*)" };
	 	 Cursor c = cr.query(VocabContentProvider.CONTENT_URI_WORDS, columns, Word.TIMES_SOLVED + "=0 AND " + Word.NEVER_PLAY + "=0", null, null);
	     c.moveToFirst();
	     int count = c.getInt(0);
	     c.close();
	     return count;
	 }   

	  /**
	  * get count of words not yet solved
	  */
	 public WordsSolvedStats getWordsSolvedStats() {
	     ContentResolver cr = mContext.getContentResolver();
		 WordsSolvedStats stats = new WordsSolvedStats();
		 stats.wordsNotYetSolved = countWordsNotYetSolved();

		 // query: select TIMES_SOLVED, count(*) from WORD group by TIMES_SOLVED order by TIMES_SOLVED
	 	 String[] columns = { Word.TIMES_SOLVED, "count(*)" };
	 	 Cursor c = cr.query(VocabContentProvider.CONTENT_URI_STATS, columns, Word.TIMES_SOLVED + ">0", null, Word.TIMES_SOLVED);
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
	 

	 /**
     * set game as complete
     */
    public void markGameComplete(Game game) throws OperationApplicationException, RemoteException {
    	ContentResolver cr = mContext.getContentResolver();

    	// batch together operations for atomic transaction
 		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
	
		// mark game as complete
    	game.setSolvedWords(game.getTotalWords());
        ops.add(ContentProviderOperation.newUpdate(VocabContentProvider.CONTENT_URI_GAMES)
        		.withValue(Game.SOLVED_WORDS, game.getSolvedWords())
        		.withSelection(Game.GAME_NO + "=?", new String[] { ""+game.getGameNo() }).build());

        // update times solved in Word records
 		for (GameWord gameWord : game.getGameWords()) {
 			ops.add(getOpUpdateTimesWordSolved(gameWord.getWordInfo()));
 		}

		cr.applyBatch(VocabContentProvider.AUTHORITY, ops);
    }    

    
    /**
     * increment mini clues given
     */
    public void saveMiniClues(Game game) {
    	ContentResolver cr = mContext.getContentResolver();
    	ContentValues values = new ContentValues();
        values.put(Game.MINI_CLUES, game.getMiniClues());
        cr.update(VocabContentProvider.CONTENT_URI_GAMES, values, Game.GAME_NO + "=?", new String[] { ""+game.getGameNo() });
    }    
    
    /**
     * increment full clues given
     */
    public void saveFullClues(Game game) {
    	ContentResolver cr = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(Game.FULL_CLUES, game.getFullClues());
        cr.update(VocabContentProvider.CONTENT_URI_GAMES, values, Game.GAME_NO + "=?", new String[] { ""+game.getGameNo() });
    }    

    

    /**
     * set play again soon
     */
    public void setWordPlaySoon(String word, boolean playSoon) {
    	ContentResolver cr = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(Word.PLAY_SOON, playSoon);
        cr.update(VocabContentProvider.CONTENT_URI_WORDS, values, Word.WORD + "=?", new String[] { word });
    }    

    /**
     * set never play again
     */
    public void setWordNeverPlay(String word, boolean neverPlay) {
    	ContentResolver cr = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(Word.NEVER_PLAY, neverPlay);
        cr.update(VocabContentProvider.CONTENT_URI_WORDS, values, Word.WORD + "=?", new String[] { word });
    }    

    
    /**
     * get list of game words from the database
     * @return list
     */
    public List<Word> getNextWordsList(int minNumWords) {
    	ContentResolver cr = mContext.getContentResolver();
    	// make sure the ranges for these calls do not overlap or duplicates will occur
    	List<Word> words = getNextWordsList(minNumWords, 6, 100); // get long words
    	words.addAll(getNextWordsList(minNumWords, 2, 5)); // get short words
    	return words;
    }
    

    /**
	 * get list of words for which definitions need to be retrieved
	 * @return list
	 */
	public List<Word> getWordsNeedingDefinitions(int numWords) {
    	ContentResolver cr = mContext.getContentResolver();
		List<Word> words = new ArrayList<Word>();
		String[] resultWords = { Word.WORD };
		Cursor c = cr.query(VocabContentProvider.CONTENT_URI_WORDS, resultWords, 
				Word.NEVER_PLAY + "=0 AND " + Word.DATE_DEFS_LOADED + "=0",
	            null, Word.DEF_NOT_FOUND + ", " + Word.TIMES_SOLVED + ", " + Word.TIMES_PLAYED  + ", " + Word.RANDOMIZER + " limit "+numWords);
		while (c.moveToNext()) {
			words.add(new Word(c));
		}
		c.close();
		return words;
	}
	
	/**
	 * get list of words for which definitions can be released
	 * @return list
	 */
	public List<Word> getWordsNotNeedingDefinitions(int numWords) {
    	ContentResolver cr = mContext.getContentResolver();
		List<Word> words = new ArrayList<Word>();
		String[] resultWords = { Word.WORD };
		Cursor c = cr.query(VocabContentProvider.CONTENT_URI_WORDS, resultWords, 
				Word.DATE_DEFS_LOADED + ">0 AND " + Word.GAME_DISCARDED + "=1 AND " + Word.PLAY_SOON + "=0", null,  
	            Word.TIMES_SOLVED + " desc, " + Word.TIMES_PLAYED + " desc, " + Word.DATE_LAST_PLAYED + " desc limit "+numWords);
		while (c.moveToNext()) {
			words.add(new Word(c));
		}
		c.close();
		return words;
	}

	  /**
	  * get count of words having definitions loaded
	  * @return rowId or -1 if failed
	  */
	 public int getNumWordsWithDefinitions() {
	     ContentResolver cr = mContext.getContentResolver();
    	 String[] columns = { "count(*)" };
    	 Cursor c = cr.query(VocabContentProvider.CONTENT_URI_WORDS, columns, Word.DATE_DEFS_LOADED + ">0 AND " + Word.NEVER_PLAY + "=0" , null, null);
	     c.moveToFirst();
	     int count = c.getInt(0);
	     c.close();
	     return count;
	 }   
  

	/**
     * Add a word to the dictionary. Persists word and randomizer values. Other values are default.
     * @return rowId or -1 if failed
     */
     public long addWords(List<Word> words) throws OperationApplicationException, RemoteException {
     	ContentResolver cr = mContext.getContentResolver();

//     	// batch together operations for atomic transaction
// 		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
// 		for (Word word : words) {
// 			ops.add(ContentProviderOperation.newInsert(VocabContentProvider.CONTENT_URI_WORDS).withValues(word.getInsertionContentValues()).build());
// 		}
//		ContentProviderResult[] results = cr.applyBatch(VocabContentProvider.AUTHORITY, ops);
//     
// 		long wordsAdded = 0;
// 		for (int i = 0;  i < results.length;  i++) {
// 			ContentProviderResult r = results[i];
// 			if (r.uri == null) {
//                Log.d(TAG, "unable to add word: " + words.get(i).getWord());  // this will be normal during upgrade
//            } else {
// 				wordsAdded++;
// 			}
// 		}
//    	return wordsAdded;
     
     	// do bulk insertion for performance, but no need to be atomic, so allow operation to continue when one insertion fails
 		ContentValues[] valuesArray = new ContentValues[words.size()];
 		for (int i = 0;  i < words.size();  i++) {
 			valuesArray[i] = words.get(i).getInsertionContentValues();
 		} 		
 		return cr.bulkInsert(VocabContentProvider.CONTENT_URI_WORDS, valuesArray); 
    }    

	 
     public List<Object> queryDebug(Uri uri, String whereClause, String orderByClause, String limitClause) {
     	ContentResolver cr = mContext.getContentResolver();
     	List<Object> results = new ArrayList<Object>();
     	Cursor c = cr.query(uri, null, whereClause, null, orderByClause + " " + limitClause); 
     	while (c.moveToNext()) {
     		if (VocabContentProvider.CONTENT_URI_WORDS.equals(uri)) {
         		results.add(new Word(c));
     		} else if (VocabContentProvider.CONTENT_URI_DEFS.equals(uri)) {
         		results.add(new Definition(c));
     		} else if (VocabContentProvider.CONTENT_URI_GAMEWORDS.equals(uri)) {
         		results.add(new GameWord(c));
     		} else if (VocabContentProvider.CONTENT_URI_GAMES.equals(uri)) {
         		results.add(new Game(c));
     		}
     	}
     	c.close();

     	return results;
     }
     
     
     
	//---------------------------------------------------//
	//------------ private methods ------------//
	//---------------------------------------------------//

    private int getCurrentGameNo() {
    	ContentResolver cr = mContext.getContentResolver();
    	int gameNo = 0;
//    	String[] columns = { "min(" + GameWord.GAME_NO + ")" };  // get min game_no from game words
//    	Cursor c = mDatabase.query(GameWord.TABLE_NAME, columns, null, null, null, null, null);
    	String[] columns = { "max(" + Game.GAME_NO + ")" };  // get max game_no from game table
    	Cursor c = cr.query(VocabContentProvider.CONTENT_URI_GAMES, columns, null, null, null);
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

    /**
     * get list of game words from the database
     * @return list
     */
    private List<Word> getNextWordsList(int minNumWords, int minLength, int maxLength) {
    	ContentResolver cr = mContext.getContentResolver();
    	List<Word> words = new ArrayList<Word>();
    	String[] resultWords = { Word.WORD, Word.TIMES_PLAYED, Word.TIMES_SOLVED }; // need times played and solved so we can increment later
    	String whereClause = Word.NEVER_PLAY + "=0 AND " + Word.DATE_DEFS_LOADED + ">0 and length(" + Word.WORD + ") between " + minLength + " and " + maxLength;
    	String orderByClause = Word.PLAY_SOON + " desc, " + Word.GAME_DISCARDED + ", " + Word.TIMES_SOLVED + ", " + Word.TIMES_PLAYED + ", " + 
				Word.DATE_LAST_PLAYED + ", " + Word.RANDOMIZER;  // note that all but last sorting column may be zero, making randomizer significant 
    	Cursor c = cr.query(VocabContentProvider.CONTENT_URI_WORDS, resultWords, whereClause, null, orderByClause + " limit " + minNumWords); // with limit clause
    	while (c.moveToNext()) {
    		words.add(new Word(c));
    	}
    	c.close();

    	return words;
    }
    
   
    /**
     * update times word played
     */
    private ContentProviderOperation getOpUpdateTimesWordPlayed(Word word) {
    	word.setTimesPlayed(word.getTimesPlayed() + 1);
        ContentValues values = new ContentValues();
        values.put(Word.PLAY_SOON, false); // considering doing this after word solved in subsequent game
        values.put(Word.DATE_LAST_PLAYED, new Date().getTime());
        values.put(Word.TIMES_PLAYED, word.getTimesPlayed());
        return ContentProviderOperation.newUpdate(VocabContentProvider.CONTENT_URI_WORDS).withExpectedCount(1)
	    	.withValues(values).withSelection(Word.WORD + "=?", new String[] { word.getWord() }).build();
    }    

    /**
     * update times word solved
     */
    private ContentProviderOperation getOpUpdateTimesWordSolved(Word word) {
    	word.setTimesSolved(word.getTimesSolved() + 1);
        ContentValues values = new ContentValues();
        values.put(Word.TIMES_SOLVED, word.getTimesSolved());
        return ContentProviderOperation.newUpdate(VocabContentProvider.CONTENT_URI_WORDS).withExpectedCount(1)
	    	.withValues(values).withSelection(Word.WORD + "=?", new String[] { word.getWord() }).build();
    }    
    
   

    /**
     * set defs not loadable
     */
    private ContentProviderOperation getOpSetWordDefNotFound(String word, boolean notFound) {
        return ContentProviderOperation.newUpdate(VocabContentProvider.CONTENT_URI_WORDS)
	    	.withValue(Word.DEF_NOT_FOUND, notFound)
	    	.withSelection(Word.WORD + "=?", new String[] { word })
	    	.build();
    }    
    

    /**
     * set words discarded
     */
    private ContentProviderOperation getOpSetWordsDiscarded(int gameNo, boolean discarded) {
    	// set game_discarded flag in WORDs that won't be played in a while (so definitions can be discarded from cache)
    	String[] gameNumArg = { String.valueOf(gameNo) };
        return ContentProviderOperation.newUpdate(VocabContentProvider.CONTENT_URI_WORDS)
	    	.withValue(Word.GAME_DISCARDED, discarded)
	    	.withSelection(Word.PLAY_SOON + "=0 AND " + Word.WORD + " IN (SELECT WORD FROM GAME_WORD WHERE " + GameWord.GAME_NO + "=?)", gameNumArg).build();
    }    
    
    /**
     * set date defs loaded
     */
    private ContentProviderOperation getOpSetDateDefsLoaded(String word) {
        return ContentProviderOperation.newUpdate(VocabContentProvider.CONTENT_URI_WORDS).withExpectedCount(1)
	    	.withValue(Word.DEF_NOT_FOUND, 0)
	    	.withValue(Word.DATE_DEFS_LOADED, new Date().getTime())
	    	.withSelection(Word.WORD + "=?", new String[] { word })
	    	.build();
    }   
    

    /**
     * set defs discarded
     */
    private ContentProviderOperation getOpSetDefsDiscarded(String word) {
        ContentValues values = new ContentValues();
        values.put(Word.DATE_DEFS_LOADED, 0);
        values.put(Word.GAME_DISCARDED, 0);
        return ContentProviderOperation.newUpdate(VocabContentProvider.CONTENT_URI_WORDS).withExpectedCount(1)
	    	.withValues(values).withSelection(Word.WORD + "=?", new String[] { word }).build();
        
    }    
    

    /**
     * delete existing word definitions from the database
     */
    private ContentProviderOperation getOpDeleteWordDefinitions(String word) {
        return ContentProviderOperation.newDelete(VocabContentProvider.CONTENT_URI_DEFS)
	    	.withSelection(Word.WORD + "=?", new String[] { word }).build();
    }    

}

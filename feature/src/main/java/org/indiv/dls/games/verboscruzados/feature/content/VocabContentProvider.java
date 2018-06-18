package org.indiv.dls.games.verboscruzados.feature.content;

import java.util.ArrayList;

import org.indiv.dls.games.verboscruzados.feature.db.DictionaryDbOpenHelper;
import org.indiv.dls.games.verboscruzados.feature.db.Word;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

public class VocabContentProvider extends ContentProvider {

	private static final String TAG = VocabContentProvider.class.getSimpleName();

	// contract string constants
	public final static String AUTHORITY = "org.indiv.dls.games.vocabproviderDELETE";
	public final static String PATH_INIT = "init";
	public final static String PATH_WORDS = "words";
	public final static String PATH_DEFS = "definitions";
	public final static String PATH_GAMEWORDS = "gamewords";
	public final static String PATH_GAMES = "games";
	public final static String PATH_STATS = "stats";
	public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	public final static Uri CONTENT_URI_WORDS = Uri.parse("content://" + AUTHORITY + "/" + PATH_WORDS);
	public final static Uri CONTENT_URI_DEFS = Uri.parse("content://" + AUTHORITY + "/" + PATH_DEFS);
	public final static Uri CONTENT_URI_GAMEWORDS = Uri.parse("content://" + AUTHORITY + "/" + PATH_GAMEWORDS);
	public final static Uri CONTENT_URI_GAMES = Uri.parse("content://" + AUTHORITY + "/" + PATH_GAMES);
	public final static Uri CONTENT_URI_STATS = Uri.parse("content://" + AUTHORITY + "/" + PATH_STATS);
	public final static String PARAM_INSERTORTHROW = "throw"; // set to "true" to specify that insertion should insert or throw exception
	public final static String METHOD_ISDBLOADED = "isDbLoaded";
	public final static String METHOD_SETDBLOADED = "setDbLoaded";

	// UriMatcher
	private final static int INIT = 0;
	private final static int ALL_WORDS = 1;
	private final static int SINGLE_WORD = 2;
	private final static int ALL_DEFS = 3;
	private final static int SINGLE_DEF = 4;
	private final static int ALL_GAMEWORDS = 5;
	private final static int SINGLE_GAMEWORD = 6;
	private final static int ALL_GAMES = 7;
	private final static int SINGLE_GAME = 8;
	private final static int ALL_STATS = 9;
	private final static int SINGLE_STAT = 10;
	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, PATH_INIT, INIT);
		uriMatcher.addURI(AUTHORITY, PATH_WORDS, ALL_WORDS);
		uriMatcher.addURI(AUTHORITY, PATH_WORDS + "/#", SINGLE_WORD);
		uriMatcher.addURI(AUTHORITY, PATH_DEFS, ALL_DEFS);
		uriMatcher.addURI(AUTHORITY, PATH_DEFS + "/#", SINGLE_DEF);
		uriMatcher.addURI(AUTHORITY, PATH_GAMEWORDS, ALL_GAMEWORDS);
		uriMatcher.addURI(AUTHORITY, PATH_GAMEWORDS + "/#", SINGLE_GAMEWORD);
		uriMatcher.addURI(AUTHORITY, PATH_GAMES, ALL_GAMES);
		uriMatcher.addURI(AUTHORITY, PATH_GAMES + "/#", SINGLE_GAME);
		uriMatcher.addURI(AUTHORITY, PATH_STATS, ALL_STATS);
		uriMatcher.addURI(AUTHORITY, PATH_STATS + "/#", SINGLE_STAT);
	}
	
	// mime types
	private static final String BASE_DIR_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.";
	private static final String BASE_ITEM_CONTENT_TYPE = "vnd.android.cursor.item/vnd.";
	public static final String CONTENT_TYPE_WORDS = BASE_DIR_CONTENT_TYPE + "dls.games.words";
	public static final String CONTENT_TYPE_WORD = BASE_ITEM_CONTENT_TYPE + "dls.games.words";
	public static final String CONTENT_TYPE_DEFS = BASE_DIR_CONTENT_TYPE + "dls.games.defs";
	public static final String CONTENT_TYPE_DEF = BASE_ITEM_CONTENT_TYPE + "dls.games.defs";
	public static final String CONTENT_TYPE_GAMEWORDS = BASE_DIR_CONTENT_TYPE + "dls.games.gamewords";
	public static final String CONTENT_TYPE_GAMEWORD = BASE_ITEM_CONTENT_TYPE + "dls.games.gamewords";
	public static final String CONTENT_TYPE_GAMES = BASE_DIR_CONTENT_TYPE + "dls.games.games";
	public static final String CONTENT_TYPE_GAME = BASE_ITEM_CONTENT_TYPE + "dls.games.games";
	public static final String CONTENT_TYPE_STATS = BASE_DIR_CONTENT_TYPE + "dls.games.stats";
	public static final String CONTENT_TYPE_STAT = BASE_ITEM_CONTENT_TYPE + "dls.games.stats";
	
	private DictionaryDbOpenHelper dbHelper;
	
	@Override
	public boolean onCreate() {
		
		// create db open helper, defer opening the db until later since onCreate called on main UI thread
		dbHelper = new DictionaryDbOpenHelper(getContext());

		return true;
	}
	
	
	@Override
	public void shutdown() {
		super.shutdown();
		dbHelper.close();
	}



	@Override
	public String getType(Uri uri) {
		switch(uriMatcher.match(uri)) {
			case SINGLE_WORD: return CONTENT_TYPE_WORD;
			case ALL_WORDS: return CONTENT_TYPE_WORDS;
			case SINGLE_DEF: return CONTENT_TYPE_DEF;
			case ALL_DEFS: return CONTENT_TYPE_DEFS;
			case SINGLE_GAMEWORD: return CONTENT_TYPE_GAMEWORD;
			case ALL_GAMEWORDS: return CONTENT_TYPE_GAMEWORDS;
			case SINGLE_GAME: return CONTENT_TYPE_GAME;
			case ALL_GAMES: return CONTENT_TYPE_GAMES;
			case SINGLE_STAT: return CONTENT_TYPE_STAT;
			case ALL_STATS: return CONTENT_TYPE_STATS;
			default: throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	
	@Override
	public Bundle call(String method, String arg, Bundle extras) {
		Bundle bundle = new Bundle();
		if (METHOD_ISDBLOADED.equals(method)) {
			// open the db to cause onCreate or onOpen to get called which will determine if db tables loaded
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			bundle.putBoolean(method, dbHelper.isDbLoaded());
		} else if (METHOD_SETDBLOADED.equals(method)) {
			dbHelper.setDbLoaded(true);
			bundle.putBoolean(method, true);
		}
		return bundle;
	}
	
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		// open the db
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		// replace these if necessary
		String groupBy = null;
		String having = null;
		
		// use query builder
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		// parse Uri
	    queryBuilder.setTables(getTableName(uri));
		switch(uriMatcher.match(uri)) {
			case SINGLE_WORD: 
			case SINGLE_DEF:
			case SINGLE_GAMEWORD:
			case SINGLE_GAME:
			case SINGLE_STAT:
			    queryBuilder.appendWhere(getIdSelection(uri));
				break;
			case ALL_STATS:
			    groupBy = Word.TIMES_SOLVED;
				break;
		}
		
		// execute the query
		return queryBuilder.query(db, projection, selection, selectionArgs, groupBy, having, sortOrder);
	}

	

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		boolean throwExc = "true".equalsIgnoreCase(uri.getQueryParameter(PARAM_INSERTORTHROW));
		
		// open the db
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		// parse Uri
		String tableName = getTableName(uri);
		Uri baseUri = getBaseUri(uri);  // not the same as sent in uri since it may have additional parameters
		switch(uriMatcher.match(uri)) {
		    case SINGLE_STAT: 
		    case ALL_STATS: 
			    throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	
		// perform the insertion
		long id = -1;
		if (throwExc) {
			try {
				id = db.insertOrThrow(tableName, null, values);
			} catch(SQLException e) {
	        	Log.d(TAG, "unable to insert into " + tableName + ": " + values.valueSet().toString());  
				throw e;
			}
		} else {
			id = db.insert(tableName, null, values);
			if (id < 0) {
	        	Log.d(TAG, "unable to insert into " + tableName + ": " + values.valueSet().toString());  
				return null;
			}
		}
		
		// construct URI of newly inserted row
		Uri insertionUri = ContentUris.withAppendedId(baseUri, id);  
		
		// notify observers of change
		getContext().getContentResolver().notifyChange(baseUri, null);

		return insertionUri;
	}

	

	@Override
	public int bulkInsert(Uri uri, ContentValues[] valuesArray) {

		boolean throwExc = "true".equalsIgnoreCase(uri.getQueryParameter(PARAM_INSERTORTHROW));
		
		// open the db
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		// parse Uri
		String tableName = getTableName(uri);
		Uri baseUri = getBaseUri(uri);  // not the same as sent in uri since it may have additional parameters
		switch(uriMatcher.match(uri)) {
			case SINGLE_STAT: 
			case ALL_STATS: 
				 throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	
		// perform the insertions
 		db.beginTransaction();
 		int rowsInserted = 0;
 		try {
     		for (ContentValues values : valuesArray) {
     			long id = -1;
     			if (throwExc) {
     				try {
     					id = db.insertOrThrow(tableName, null, values);
         				rowsInserted++;
     				} catch(SQLException e) {
     		        	Log.d(TAG, "unable to insert into " + tableName + ": " + values.valueSet().toString());  
     					throw e;
     				}
     			} else {
     				id = db.insert(tableName, null, values);
     				if (id < 0) {
     		        	Log.d(TAG, "unable to insert into " + tableName + ": " + values.valueSet().toString());  
     				} else {
         				rowsInserted++;
     				}
     			}
     		}
 		    db.setTransactionSuccessful();
 		} catch (SQLException e) {
 			Log.e(TAG, "unable to insert into " + tableName + ": " + e.getMessage());
 			throw e; // rethrow
 		} finally {
 		    db.endTransaction();
 		}
		
		// if successful
		if (rowsInserted > 0) {
			// notify observers of change
			getContext().getContentResolver().notifyChange(baseUri, null);
		}
		
		return rowsInserted;
	}

	// override to encapsulate operations in a transaction
	@Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws OperationApplicationException {
		ContentProviderResult[] results = null;
		
		// open the db
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
 		db.beginTransaction();
 		try {
 			results = super.applyBatch(operations);
		    db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e(TAG, "unable to perform batch operations: " + e.getMessage());
			throw e; // rethrow
		} finally {
		    db.endTransaction();
		}
		
        return results;
    }


	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		
		// open the db
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		// parse Uri
		String tableName = getTableName(uri);
		switch(uriMatcher.match(uri)) {
			case SINGLE_WORD: 
			case SINGLE_DEF: 
			case SINGLE_GAMEWORD:
			case SINGLE_GAME: 
				selection = appendIdSelection(uri, selection);
				break;
			case SINGLE_STAT:
			case ALL_STATS:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	
		// perform the update
		int updateCount = db.update(tableName, values, selection, selectionArgs);
		
		// notify observers of change
		if (updateCount > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		
		return updateCount;
	}

	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		// open the db
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		// parse Uri
		String tableName = getTableName(uri);
		switch(uriMatcher.match(uri)) {
			case SINGLE_WORD:
			case SINGLE_DEF: 
			case SINGLE_GAMEWORD: 
			case SINGLE_GAME:
				selection = appendIdSelection(uri, selection);
				break;
			case SINGLE_STAT:
			case ALL_STATS:
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	
		// if no where clause, then pass in "1" so we'll get a count if items deleted
		if (TextUtils.isEmpty(selection)){
			selection = "1"; 
		}
			
		// perform the deletion
		int deleteCount = db.delete(tableName, selection, selectionArgs);
		
		// notify observers of change
		if (deleteCount > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		
		return deleteCount;
	}
	
	
	private String appendIdSelection(Uri uri, String selection) {
		return getIdSelection(uri) + (!TextUtils.isEmpty(selection)? " AND (" + selection + ")"  : "");
	}
	
	private String getIdSelection(Uri uri) {
		return DictionaryDbOpenHelper.KEY_ID + "=" + uri.getLastPathSegment();
	}

	private String getTableName(Uri uri) {
		switch(uriMatcher.match(uri)) {
			case SINGLE_WORD: 
			case ALL_WORDS: 
			case SINGLE_STAT:   
			case ALL_STATS:   
				return DictionaryDbOpenHelper.TABLE_WORD;
			case SINGLE_DEF: 
			case ALL_DEFS: 
				return DictionaryDbOpenHelper.TABLE_DEFINITION;
			case SINGLE_GAMEWORD: 
			case ALL_GAMEWORDS: 
				return DictionaryDbOpenHelper.TABLE_GAMEWORD;
			case SINGLE_GAME: 
			case ALL_GAMES: 
				return DictionaryDbOpenHelper.TABLE_GAME;
			default: 
            	Log.e(TAG, "Unsupported URI: " + uri);  
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	// not the same as uri which may have additional parameters
	private Uri getBaseUri(Uri uri) {
		switch(uriMatcher.match(uri)) {
			case SINGLE_WORD: 
			case ALL_WORDS: 
				return CONTENT_URI_WORDS;
			case SINGLE_DEF: 
			case ALL_DEFS: 
				return CONTENT_URI_DEFS;
			case SINGLE_GAMEWORD: 
			case ALL_GAMEWORDS: 
				return CONTENT_URI_GAMEWORDS;
			case SINGLE_GAME: 
			case ALL_GAMES:
				return CONTENT_URI_GAMES;
			case SINGLE_STAT:   
			case ALL_STATS:   
				return CONTENT_URI_STATS;
			default: 
            	Log.e(TAG, "Unsupported URI: " + uri);  
				throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

}

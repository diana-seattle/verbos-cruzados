package org.indiv.dls.games.verboscruzados.feature.db;

import android.content.ContentValues;
import android.database.Cursor;

public class Definition {
    public static final String TABLE_NAME = "DEFINITION";
    public static final String WORD = "WORD";  // FK, part of PK
    public static final String DEF_ORDER = "DEF_ORDER";  // part of PK
    public static final String DEFINITION = "DEFINITION";
    public static final String SOURCE = "SOURCE";
    public static final String PART_OF_SPEECH = "PART_OF_SPEECH";
    
    public static final String TABLE_DELETE = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    public static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NAME + " (" +
			            		WORD 			+ " TEXT 	NOT NULL, " +
			            		DEF_ORDER 		+ " INTEGER NOT NULL, " +
			            		SOURCE 			+ " TEXT 	NOT NULL, " +
			            		PART_OF_SPEECH 	+ " TEXT, " +
			            		DEFINITION 		+ " TEXT 	NOT NULL, " +
    		"CONSTRAINT DEF_PK PRIMARY KEY (" + WORD + "," + DEF_ORDER + "), " +
    		"CONSTRAINT DEF_WORD_FK FOREIGN KEY (" + WORD + ") REFERENCES " + Word.TABLE_NAME + "(" + Word.WORD + ")" +
    		");";
    public static final String FK_INDEX_CREATE = "CREATE INDEX DEF_WORD_FK_I ON " + TABLE_NAME + " (" + WORD + ")";
    
	// dictionary sources
    private static final String DICT_SRC_AHD = "ahd-legacy";
    private static final String DICT_SRC_AHD5 = "ahd5";
	private static final String DICT_SRC_WEBSTER = "gcide";
	private static final String DICT_SRC_CENTURY = "century";
	private static final String DICT_SRC_WIKTIONARY = "wiktionary";
	private static final String DICT_SRC_WORDNET = "wordnet";
    
    private String word; // FK, part of PK
    private int order;   // part of PK
    private String source;
    private String partOfSpeech;
    private String definition;

    
    // constructor
	public Definition() {}
	
    // constructor
	public Definition(String word, int order, String definition, String source, String partOfSpeech) {
		this.word = word;
		this.order = order;
		this.definition = definition;
		this.source = source;
		this.partOfSpeech = partOfSpeech;
	}

    // constructor
    public Definition(Cursor c) {
    	this.setWord(c);
        this.setOrder(c);
        this.setDefinition(c);
        this.setSource(c);
        this.setPartOfSpeech(c);
    }    
	
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(WORD, getWord());
        values.put(DEF_ORDER, getOrder());
        values.put(DEFINITION, getDefinition());
        values.put(SOURCE, getSource());
        values.put(PART_OF_SPEECH, getPartOfSpeech());
        return values;
    }    
	
    public String getFullText(int definitionNo) {
    	StringBuffer buf = new StringBuffer();
    	buf.append(definitionNo).append(". ");
    	if (partOfSpeech != null && partOfSpeech.length() > 0) {
    		buf.append("(").append(getPartOfSpeech()).append(") ");
    	}
    	buf.append(getDefinition());
    	return buf.toString();
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
	
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public void setOrder(Cursor c) {
		int i = c.getColumnIndex(DEF_ORDER);
    	if (i != -1) { setOrder(c.getInt(i)); }
	}
	
	public String getDefinition() {
		return definition;
	}
	public void setDefinition(String definition) {
		this.definition = definition;
	}
	public void setDefinition(Cursor c) {
		int i = c.getColumnIndex(DEFINITION);
		if (i != -1) { setDefinition(c.getString(i)); }
	}
	
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public void setSource(Cursor c) {
		int i = c.getColumnIndex(SOURCE);
		if (i != -1) { setSource(c.getString(i)); }
	}
	public boolean isSourceAhd() { return DICT_SRC_AHD.equals(source) || DICT_SRC_AHD5.equals(source); }
	public boolean isSourceWebster() { return DICT_SRC_WEBSTER.equals(source); }
	public boolean isSourceCentury() { return DICT_SRC_CENTURY.equals(source); }
	public boolean isSourceWiktionary() { return DICT_SRC_WIKTIONARY.equals(source); }
	public boolean isSourceWordnet() { return DICT_SRC_WORDNET.equals(source); }
	
	
	
	public String getPartOfSpeech() {
		return partOfSpeech;
	}
	public void setPartOfSpeech(String partOfSpeech) {
		this.partOfSpeech = partOfSpeech;
	}
	public void setPartOfSpeech(Cursor c) {
		int i = c.getColumnIndex(PART_OF_SPEECH);
		if (i != -1) { setPartOfSpeech(c.getString(i)); }
	}
}

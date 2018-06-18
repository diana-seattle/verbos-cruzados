package org.indiv.dls.games.verboscruzados.feature.mock;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.test.mock.MockContentProvider;

public class MockVocabContentProvider extends MockContentProvider {

	Cursor mResultCursor;
	
	public void setQueryResult(Cursor c) {
		mResultCursor = c;
	}
	
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder){
        return mResultCursor;
    } 
	
}

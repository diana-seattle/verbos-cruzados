package org.indiv.dls.games.vocabrecall.feature.mock;

import android.content.ContentResolver;
import android.content.Context;
import android.test.RenamingDelegatingContext;

public class ContextWithMockContentResolver extends RenamingDelegatingContext {
    private ContentResolver contentResolver;
    
    public ContextWithMockContentResolver(Context targetContext) { 
    	super(targetContext, "test");
    }

    public void setContentResolver(ContentResolver contentResolver){ 
    	this.contentResolver = contentResolver;
	}
    @Override public ContentResolver getContentResolver() { return contentResolver; }

    //    @Override public Context getApplicationContext(){ return this; } //Added in-case my class called getApplicationContext() 

}

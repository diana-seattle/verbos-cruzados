package org.indiv.dls.games.vocabrecall.feature;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/*
 * this class is only used in single pane mode (on smaller screens)
 */
public class AnswerActivity extends MyActionBarActivity {

    //region CLASS VARIABLES -----------------------------------------------------------------------

    private AnswerFragment mAnswerFragment;

    //endregion

    //region OVERRIDDEN METHODS --------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);

        // Set up toolbar
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // set to portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // get answer fragment
        mAnswerFragment = (AnswerFragment) getSupportFragmentManager().findFragmentById(R.id.answer_fragment);

        // enables back button (see http://stackoverflow.com/questions/10108774/android-actionbar-back-button)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.answerfragment_options, menu);
        mOptionsMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        int i = item.getItemId();
        if (i == android.R.id.home) {
            // in response to back button on answer activity, close answer activity
            mAnswerFragment.hideSoftKeyboardForAnswer();
            setResult(Activity.RESULT_CANCELED);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /*
     * overriding to display answer
     */
    @Override
    protected void giveAnswer() {
        super.giveAnswer();
        mAnswerFragment.giveAnswer();
    }

    /*
     * overriding to display hint
     */
    @Override
    protected void give3LetterHint() {
        super.give3LetterHint();
        mAnswerFragment.give3LetterHint();
    }

    //endregion

}

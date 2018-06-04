package org.indiv.dls.games.vocabrecall.feature

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

/**
 * This activity houses the [AnswerFragment] when run in portrait mode (on non-tablet devices). Not used on tablets.
 */
class AnswerActivity : MyActionBarActivity() {

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private var answerFragment: AnswerFragment? = null

    //endregion

    //region OVERRIDDEN FUNCTIONS ------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answer)

        // Set up toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // set to portrait mode
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // get answer fragment
        answerFragment = supportFragmentManager.findFragmentById(R.id.answer_fragment) as AnswerFragment

        // enables back button (see http://stackoverflow.com/questions/10108774/android-actionbar-back-button)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.answerfragment_options, menu)
        optionsMenu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar items
        when (item.itemId) {
            android.R.id.home -> {
                // In response to back button on answer activity, close answer activity
                answerFragment?.hideSoftKeyboardForAnswer()
                setResult(Activity.RESULT_CANCELED)
                finish()
                return true
            }
            // The remaining actions are common to both activities to call super to handle them.
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /*
     * overriding to display answer
     */
    override fun giveAnswer() {
        super.giveAnswer()
        answerFragment?.giveAnswer()
    }

    /*
     * overriding to display hint
     */
    override fun give3LetterHint() {
        super.give3LetterHint()
        answerFragment?.give3LetterHint()
    }

    //endregion

}

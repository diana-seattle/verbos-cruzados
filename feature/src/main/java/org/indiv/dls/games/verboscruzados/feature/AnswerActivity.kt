package org.indiv.dls.games.verboscruzados.feature

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import org.indiv.dls.games.verboscruzados.feature.R

/**
 * This activity houses the [AnswerFragment] when run in portrait mode (on non-tablet devices). Not used on tablets.
 */
class AnswerActivity : MyActionBarActivity() {

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        private val KEY_ANSWER_PRESENTATION = "KEY_ANSWER_PRESENTATION"

        @JvmStatic
        fun getIntent(context: Context, answerPresentation: AnswerPresentation): Intent {
            val intent = Intent(context, AnswerActivity::class.java)
            intent.putExtra(KEY_ANSWER_PRESENTATION, answerPresentation)
            return intent
        }
    }

    //endregion

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

        // get answer fragment
        answerFragment = supportFragmentManager.findFragmentById(R.id.answer_fragment) as AnswerFragment

        val answerPresentation: AnswerPresentation = intent.getParcelableExtra(KEY_ANSWER_PRESENTATION)
        answerPresentation?.let {
            answerFragment?.setGameWord(it)
        }

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

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------
    //endregion

}

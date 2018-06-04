package org.indiv.dls.games.vocabrecall.feature

import org.indiv.dls.games.vocabrecall.feature.db.ContentHelper
import org.indiv.dls.games.vocabrecall.feature.db.GameWord
import org.indiv.dls.games.vocabrecall.feature.dialog.HelpDialogFragment
import org.indiv.dls.games.vocabrecall.feature.dialog.StatsDialogFragment

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView

/**
 * Base class for [VocabRecallActivity] and [AnswerActivity]. The latter is used to house [AnswerFragment] when run
 * in portrait mode, on non-tablet devices. The former houses [PuzzleFragment], and optionally [AnswerFragment], when
 * run in landscape mode, on tablet devices. The shared logic between these activities is implemented in this base class.
 */
abstract class MyActionBarActivity : AppCompatActivity() {

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        var sCurrentGameWord: GameWord? = null
        var sPuzzleRepresentation: List<TextView>? = null
        protected var sDbHelper: ContentHelper? = null
        protected var sGamesCompleted = 0
        protected var sWordsCompleted = 0
        protected var sDbSetupComplete = false
    }

    //endregion

    //region PROTECTED PROPERTIES ------------------------------------------------------------------

    protected var optionsMenu: Menu? = null
    protected var toolbar: Toolbar? = null

    //endregion

    //region OVERRIDDEN FUNCTIONS ------------------------------------------------------------------

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        sCurrentGameWord?.let {
            // modify 3-letter clue menu item
            val menuItemMiniClue = optionsMenu?.findItem(R.id.action_give3letters)
            menuItemMiniClue?.apply {
                title = resources.getString(R.string.action_give3letters) + it.game.miniCluesMenuText
                isEnabled = it.game.isMiniClueRemaining
            }

            // modify full-answer clue menu item
            val menuItemFullAnswer = optionsMenu?.findItem(R.id.action_giveanswer)
            menuItemFullAnswer?.apply {
                title = resources.getString(R.string.action_giveanswer) + it.game.fullCluesMenuText
                isEnabled = it.game.isFullClueRemaining
            }
        }
        return true
    }

    /**
     * Handles presses on the action bar items.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_help -> showHelpDialog()
            R.id.action_showstats -> showStatsDialog()
            R.id.action_give3letters -> give3LetterHint()
            R.id.action_giveanswer -> giveAnswer()
            R.id.action_playagainsoon -> sCurrentGameWord?.let {
                Thread { MyActionBarActivity.sDbHelper?.setWordPlaySoon(it.word, true) }.start()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    //endregion

    //region PROTECTED FUNCTIONS -------------------------------------------------------------------

    /*
     * override to display answer
     */
    protected open fun giveAnswer() {
        sCurrentGameWord?.let {
            it.game.fullClues++
            Thread { MyActionBarActivity.sDbHelper?.saveFullClues(it.game) }.start()
        }
        // subclass handles the rest
    }

    /*
     * override to display 3 letter hint
     */
    protected open fun give3LetterHint() {
        sCurrentGameWord?.let {
            it.game.miniClues++
            Thread { MyActionBarActivity.sDbHelper?.saveMiniClues(it.game) }.start()
        }
        // subclass handles the rest
    }

    protected fun setOptionsMenuText(menuItemId: Int, textId: Int) {
        optionsMenu?.findItem(menuItemId)?.setTitle(textId)
    }

    protected fun showHelpDialog() {
        HelpDialogFragment().show(supportFragmentManager, "fragment_showhelp")
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    private fun showStatsDialog() {
        sDbHelper?.let {
            val stats = it.wordsSolvedStats
            val dlg = StatsDialogFragment()
            dlg.setStats(sGamesCompleted, sWordsCompleted, stats)
            dlg.show(supportFragmentManager, "fragment_showstats")
        }
    }

    //endregion

}

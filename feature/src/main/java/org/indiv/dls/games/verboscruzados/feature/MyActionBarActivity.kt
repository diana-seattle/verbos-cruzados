package org.indiv.dls.games.verboscruzados.feature

import android.os.Bundle
import org.indiv.dls.games.verboscruzados.feature.db.ContentHelper
import org.indiv.dls.games.verboscruzados.feature.db.GameWord
import org.indiv.dls.games.verboscruzados.feature.dialog.HelpDialogFragment
import org.indiv.dls.games.verboscruzados.feature.dialog.StatsDialogFragment

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem

/**
 * Base class for [MainActivity] and [AnswerActivity]. The latter is used to house [AnswerFragment] when run
 * in portrait mode, on non-tablet devices. The former houses [PuzzleFragment], and optionally [AnswerFragment], when
 * run in landscape mode, on tablet devices. The shared logic between these activities is implemented in this base class.
 */
abstract class MyActionBarActivity : AppCompatActivity() {

    //region COMPANION OBJECT ----------------------------------------------------------------------

    // TODO: get rid of static vars below


    companion object {
        var currentGameWord: GameWord? = null
    }

    //endregion

    //region PROTECTED PROPERTIES ------------------------------------------------------------------

    protected var optionsMenu: Menu? = null
    protected var toolbar: Toolbar? = null
    protected lateinit var dbHelper: ContentHelper

    //endregion

    //region OVERRIDDEN FUNCTIONS ------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = ContentHelper(this)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        currentGameWord?.game?.let {
            // modify 3-letter clue menu item
            val menuItemMiniClue = optionsMenu?.findItem(R.id.action_give3letters)
            menuItemMiniClue?.apply {
                title = resources.getString(R.string.action_give3letters) + it.miniCluesMenuText
                isEnabled = it.isMiniClueRemaining
            }

            // modify full-answer clue menu item
            val menuItemFullAnswer = optionsMenu?.findItem(R.id.action_giveanswer)
            menuItemFullAnswer?.apply {
                title = resources.getString(R.string.action_giveanswer) + it.fullCluesMenuText
                isEnabled = it.isFullClueRemaining
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
            R.id.action_playagainsoon -> currentGameWord?.word?.let {
                Thread { dbHelper.setWordPlaySoon(it, true) }.start()
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
        currentGameWord?.game?.let {
            it.fullClues++
            Thread { dbHelper.saveFullClues(it) }.start()
        }
        // subclass handles the rest
    }

    /*
     * override to display 3 letter hint
     */
    protected open fun give3LetterHint() {
        currentGameWord?.game?.let {
            it.miniClues++
            Thread { dbHelper.saveMiniClues(it) }.start()
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
        val stats = dbHelper.wordsSolvedStats
        val dlg = StatsDialogFragment()
        dlg.setStats(dbHelper.gamesCompleted, dbHelper.wordCountOfGamesCompleted, stats)
        dlg.show(supportFragmentManager, "fragment_showstats")
    }

    //endregion

}

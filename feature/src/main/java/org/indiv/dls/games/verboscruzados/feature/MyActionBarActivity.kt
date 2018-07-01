package org.indiv.dls.games.verboscruzados.feature

import android.os.Bundle
import org.indiv.dls.games.verboscruzados.feature.game.PersistenceHelper
import org.indiv.dls.games.verboscruzados.feature.game.GameWord
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
    protected lateinit var mDbHelper: PersistenceHelper

    //endregion

    //region OVERRIDDEN FUNCTIONS ------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDbHelper = PersistenceHelper(this)
    }

    /**
     * Handles presses on the action bar items.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_help -> showHelpDialog()
            R.id.action_showstats -> showStatsDialog()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    //endregion

    //region PROTECTED FUNCTIONS -------------------------------------------------------------------

    protected fun setOptionsMenuText(menuItemId: Int, textId: Int) {
        optionsMenu?.findItem(menuItemId)?.setTitle(textId)
    }

    protected fun showHelpDialog() {
        HelpDialogFragment().show(supportFragmentManager, "fragment_showhelp")
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    private fun showStatsDialog() {
        val dlg = StatsDialogFragment()
        dlg.setStats(0, 0)
        dlg.show(supportFragmentManager, "fragment_showstats")
    }

    //endregion

}

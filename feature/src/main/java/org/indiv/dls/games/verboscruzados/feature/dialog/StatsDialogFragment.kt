package org.indiv.dls.games.verboscruzados.feature.dialog

import org.indiv.dls.games.verboscruzados.feature.R

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.TextView

/**
 * Dialog for showing game stats.
 */
class StatsDialogFragment : DialogFragment() {

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------
    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private var mGamesCompleted: Int = 0
    private var mWordsCompleted: Int = 0

    //endregion

    //region PUBLIC INTERFACES ---------------------------------------------------------------------
    //endregion

    //region OVERRIDDEN FUNCTIONS ------------------------------------------------------------------

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.fragment_stats_dialog, null)

        val dialog = AlertDialog.Builder(activity!!)
                .setPositiveButton(R.string.dialog_ok) { dialog, id -> }
                .setView(view)
                .create()

        // fill in stats
        appendTextToView(view.findViewById(R.id.textview_gamescompleted), " $mGamesCompleted")
        appendTextToView(view.findViewById(R.id.textview_wordscompleted), " $mWordsCompleted")

        return dialog
    }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    fun setStats(gamesCompleted: Int, wordsCompleted: Int) {
        mGamesCompleted = gamesCompleted
        mWordsCompleted = wordsCompleted
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    private fun appendTextToView(textView: TextView, text: String) {
        textView.text = textView.text.toString() + text
    }

    //endregion
}

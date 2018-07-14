package org.indiv.dls.games.verboscruzados.feature.dialog

import org.indiv.dls.games.verboscruzados.feature.R

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.TextView
import org.indiv.dls.games.verboscruzados.feature.game.PersistenceHelper
import org.indiv.dls.games.verboscruzados.feature.model.ConjugationType
import org.indiv.dls.games.verboscruzados.feature.model.InfinitiveEnding
import org.indiv.dls.games.verboscruzados.feature.model.IrregularityCategory

/**
 * Dialog for showing game stats.
 */
class StatsDialogFragment : DialogFragment() {

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        /**
         * Creates stats index for 2-dimensional representation of stats where IrregularityCategory
         * and InfinitiveEnding are on the y-axis, and conjugation type on the x-axis.
         */
        fun createStatsIndex(conjugationType: ConjugationType,
                             infinitiveEnding: InfinitiveEnding,
                             irregularityCategory: IrregularityCategory): Int {
            val rowIndex = (irregularityCategory.indexForStats * 3) + infinitiveEnding.indexForStats
            return rowIndex * ConjugationType.values().size + conjugationType.indexForStats
        }
    }

    //endregion

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------
    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------
    //endregion

    //region PUBLIC INTERFACES ---------------------------------------------------------------------
    //endregion

    //region OVERRIDDEN FUNCTIONS ------------------------------------------------------------------

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = activity!!.layoutInflater
        val v = inflater.inflate(R.layout.fragment_stats_dialog, null)

        val dialog = AlertDialog.Builder(activity!!)
                .setTitle(R.string.dialog_stats_heading)
                .setPositiveButton(R.string.dialog_ok) { dialog, id -> }
                .setView(v)
                .create()

        // fill in stats
        val persistenceHelper = PersistenceHelper(activity!!)
        val statsMap = persistenceHelper.allGameStats

//        val textView: TextView = v.findViewById(R.id.textview_temp)
        val myText = statsMap.toString()
//        textView.text = myText

        return dialog
    }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------
    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------
    //endregion
}

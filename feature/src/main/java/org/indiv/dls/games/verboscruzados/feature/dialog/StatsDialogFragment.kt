package org.indiv.dls.games.verboscruzados.feature.dialog

import org.indiv.dls.games.verboscruzados.feature.R

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import org.indiv.dls.games.verboscruzados.feature.component.StatsGraphicView
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
        val columnCount = ConjugationType.values().size
        val rowCount = IrregularityCategory.values().size * InfinitiveEnding.values().size
        /**
         * Creates stats index for 2-dimensional representation of stats where IrregularityCategory
         * and InfinitiveEnding are on the y-axis, and conjugation type on the x-axis.
         */
        fun createStatsIndex(conjugationType: ConjugationType,
                             infinitiveEnding: InfinitiveEnding,
                             irregularityCategory: IrregularityCategory): Int {
            val rowIndex = (irregularityCategory.indexForStats * 3) + infinitiveEnding.indexForStats
            return rowIndex * columnCount + conjugationType.indexForStats
        }

        /**
         * Returns x,y coordinates for a stats index where 0,0 is top left.
         */
        fun getCoordinates(statsIndex: Int): Pair<Int, Int> {
            val x = statsIndex % columnCount
            val y = rowCount - 1 - statsIndex / columnCount
            return Pair(x, y)
        }
    }

    //endregion

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    var showGameOptionsListener: (() -> Unit)? = null

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
                .setNeutralButton(R.string.action_showgameoptions) { _, _ -> showGameOptionsListener?.invoke() }
                .setPositiveButton(R.string.dialog_ok) { dialog, id -> }
                .setView(v)
                .create()

        // fill in stats
        val persistenceHelper = PersistenceHelper(activity!!)
        val statsMap = persistenceHelper.allGameStats
                .mapKeys { getCoordinates(it.key) }

        val statsGraphicView: StatsGraphicView = v.findViewById(R.id.stats_dialog_graphic)
        statsGraphicView.setStats(rowCount, columnCount, statsMap)

        return dialog
    }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------
    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------
    //endregion
}

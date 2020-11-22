package org.indiv.dls.games.verboscruzados.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import org.indiv.dls.games.verboscruzados.R
import org.indiv.dls.games.verboscruzados.component.StatsGraphicView
import org.indiv.dls.games.verboscruzados.databinding.FragmentGameOptionsDialogBinding
import org.indiv.dls.games.verboscruzados.databinding.FragmentStatsDialogBinding
import org.indiv.dls.games.verboscruzados.game.PersistenceHelper
import org.indiv.dls.games.verboscruzados.model.ConjugationType
import org.indiv.dls.games.verboscruzados.model.InfinitiveEnding
import org.indiv.dls.games.verboscruzados.model.IrregularityCategory

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
            val y = statsIndex / columnCount
            return Pair(x, y)
        }
    }

    //endregion

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    var showGameOptionsListener: (() -> Unit)? = null

    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private lateinit var binding: FragmentStatsDialogBinding

    //endregion

    //region OVERRIDDEN FUNCTIONS ------------------------------------------------------------------

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentStatsDialogBinding.inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(activity!!)
                .setTitle(R.string.dialog_stats_heading)
                .setNeutralButton(R.string.action_showgameoptions) { _, _ -> showGameOptionsListener?.invoke() }
                .setPositiveButton(R.string.dialog_ok) { dialog, id -> }
                .setView(binding.root)
                .create()

        // fill in stats
        val persistenceHelper = PersistenceHelper(activity!!)
        val statsMap = persistenceHelper.allGameStats
                .mapKeys { getCoordinates(it.key) }

        val statsGraphicView: StatsGraphicView = binding.statsDialogGraphic
        statsGraphicView.setStats(rowCount, columnCount, statsMap)

        return dialog
    }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------
    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------
    //endregion
}

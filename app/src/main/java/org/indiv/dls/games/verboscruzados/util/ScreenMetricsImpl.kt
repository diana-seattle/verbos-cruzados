package org.indiv.dls.games.verboscruzados.util

import android.app.Activity
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue
import org.indiv.dls.games.verboscruzados.MainActivityViewModel
import org.indiv.dls.games.verboscruzados.R
import kotlin.math.roundToInt

class ScreenMetricsImpl(activity: Activity) : MainActivityViewModel.ScreenMetrics {

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private val puzzleMarginTopPixels: Float
    private val viewablePuzzleHeight: Float
    private val pixelsPerCell: Float

    //endregion

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    override val keyboardHeight: Float
    override val gridHeight: Int
    override val gridWidth: Int

    //region INITIALIZER ---------------------------------------------------------------------------

    init {
        val resources = activity.resources
        val displayMetrics = resources.displayMetrics
        val configuration = resources.configuration

        keyboardHeight = resources.getDimension(R.dimen.keyboard_height)
        puzzleMarginTopPixels = resources.getDimension(R.dimen.puzzle_margin_top)

        val puzzleMarginSidePixels = resources.getDimension(R.dimen.puzzle_margin_side)
        val totalPuzzleMarginTopPixels = puzzleMarginTopPixels * 2
        val totalPuzzleMarginSidePixels = puzzleMarginSidePixels * 2
        val actionBarHeightPixels = getActionBarHeightInPixels(displayMetrics, activity.theme)
        val screenWidthDp = configuration.smallestScreenWidthDp
        val screenHeightDp = maxOf(configuration.screenHeightDp, configuration.screenWidthDp)
        val heightFactor = when {
            screenWidthDp < 350 -> 2f
            screenWidthDp < 450 -> 1.5f
            else -> 1f
        }
        val screenWidthPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                screenWidthDp.toFloat(), displayMetrics)
        val screenHeightPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                screenHeightDp.toFloat(), displayMetrics)

        viewablePuzzleHeight = screenHeightPixels - actionBarHeightPixels - totalPuzzleMarginTopPixels
        val puzzleHeightPixels = viewablePuzzleHeight * heightFactor
        val puzzleWidthPixels = screenWidthPixels - totalPuzzleMarginSidePixels

        // calculate number of pixels equivalent to 24dp (24dp allows 13 cells on smallest screen supported by Android (320dp width, 426dp height))
        pixelsPerCell = resources.getDimension(R.dimen.cell_width)
        gridHeight = (puzzleHeightPixels / pixelsPerCell).toInt()
        gridWidth = (puzzleWidthPixels / pixelsPerCell).toInt()
    }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    /**
     * Returns new scroll position to maximize position a game word, and especially the selected character of the word.
     *
     * @param startingRow row of first character of the selected game word.
     * @param endingRow row of last character of the selected game word.
     * @param rowOfSelectedCell row of the selected character within the selected game word.
     * @param currentScrollPosition current scroll position of the game.
     */
    override fun newScrollPositionShowingFullWord(startingRow: Int, endingRow: Int, rowOfSelectedCell: Int, currentScrollPosition: Int): Int? {
        val yOfFirstCell = startingRow * pixelsPerCell

        val availableHeight = viewablePuzzleHeight - keyboardHeight
        val wordHeight = (endingRow - startingRow + 1) * pixelsPerCell

        // if there's room to display the whole word
        if (wordHeight < availableHeight) {
            // if first cell is above visible area, scroll up to it, or if last cell is below visible area, scroll down to it
            if (yOfFirstCell < currentScrollPosition) {
                return (yOfFirstCell - puzzleMarginTopPixels).roundToInt()
            } else if (yOfFirstCell + wordHeight > currentScrollPosition + availableHeight) {
                return (yOfFirstCell + wordHeight - availableHeight + puzzleMarginTopPixels).roundToInt()
            }
        } else {
            // There is not room for the whole word vertically, so make sure the selected cell is at least visible.
            // (This scenario should only happen with a vertical word.)
            val yOfSelectedCell = rowOfSelectedCell * pixelsPerCell
            if (yOfSelectedCell < currentScrollPosition) {
                // scroll top of cell to top of viewable area
                return (yOfSelectedCell - puzzleMarginTopPixels).roundToInt()
            } else if (yOfSelectedCell + pixelsPerCell > currentScrollPosition + availableHeight) {
                // scroll bottom of cell to bottom of viewable area
                return (yOfSelectedCell + pixelsPerCell - availableHeight + puzzleMarginTopPixels).roundToInt()
            }
        }

        // No scrolling is necessary
        return null
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    private fun getActionBarHeightInPixels(displayMetrics: DisplayMetrics, theme: Resources.Theme): Int {
        // actionBar.getHeight() returns zero in onCreate (i.e. before it is shown)
        // for the following solution, see: http://stackoverflow.com/questions/12301510/how-to-get-the-actionbar-height/13216807#13216807
        var actionBarHeight = 0  // actionBar.getHeight() returns zero in onCreate
        val tv = TypedValue()
        if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, displayMetrics)
        }
        return actionBarHeight
    }

    //endregion
}
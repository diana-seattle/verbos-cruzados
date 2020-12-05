package org.indiv.dls.games.verboscruzados.util

import android.app.Activity
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue
import org.indiv.dls.games.verboscruzados.MainActivityViewModel
import org.indiv.dls.games.verboscruzados.R

class ScreenMetricsImpl(activity: Activity) : MainActivityViewModel.ScreenMetrics {
    override val keyboardHeight: Float
    override val puzzleMarginTopPixels: Float
    override val viewablePuzzleHeight: Float
    override val pixelsPerCell: Float
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
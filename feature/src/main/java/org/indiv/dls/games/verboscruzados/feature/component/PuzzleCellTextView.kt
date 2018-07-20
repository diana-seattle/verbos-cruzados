package org.indiv.dls.games.verboscruzados.feature.component

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import org.indiv.dls.games.verboscruzados.feature.R

/**
 * Styled [TextView] used in puzzle.
 */
open class PuzzleCellTextView @JvmOverloads constructor(context: Context,
                                                        attrs: AttributeSet? = null,
                                                        defStyleAttr: Int = 0)
    : TextView(context, attrs, defStyleAttr) {

    //region INITIALIZER ---------------------------------------------------------------------------

    init {
        val size = Math.round(resources.getDimension(R.dimen.cell_width))
        val fontHeight = size * FONT_SIZE_FRACTION
        gravity = Gravity.CENTER
        // need to create Drawable object for each TextView
        background = resources.getDrawable(R.drawable.cell_drawable, null)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, fontHeight)
        width = size
        height = size
        background.level = CELL_BKGD_LEVEL_NORMAL // default to normal text cell background (i.e. no error indication)
        //		setSoundEffectsEnabled(false); // true by default, consider disabling since we're providing our own vibration (except not all devices have vibration)
    }

    //endregion

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        private val FONT_SIZE_FRACTION = .74f

        private val CELL_BKGD_LEVEL_NORMAL = 1
        private val CELL_BKGD_LEVEL_ERRORED = 2
        private val CELL_BKGD_LEVEL_SELECTED = 3
        private val CELL_BKGD_LEVEL_ERRORED_SELECTED = 4
        private val CELL_BKGD_LEVEL_SELECTED_INDIV = 5 // Individual cell selected within selected word
        private val CELL_BKGD_LEVEL_ERRORED_SELECTED_INDIV = 6
        private val ERRORED_BACKGROUND_LEVELS = listOf(CELL_BKGD_LEVEL_ERRORED,
                CELL_BKGD_LEVEL_ERRORED_SELECTED,
                CELL_BKGD_LEVEL_ERRORED_SELECTED_INDIV)
        private val INDIV_SELECTION_BACKGROUND_LEVELS = listOf(CELL_BKGD_LEVEL_SELECTED_INDIV,
                CELL_BKGD_LEVEL_ERRORED_SELECTED_INDIV)
    }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    /**
     * Fills [TextView] with the character from the user's answer.
     *
     * @param userChar the character to fill the textview with.
     */
    fun fillTextView(userChar: Char?) {
        text = userChar?.toString()
        setTextColor(Color.BLACK)
    }

    /**
     * Sets background and text colors according to specified state.
     */
    fun setStyle(isSelected: Boolean, indicateError: Boolean) {
        val individuallySelected = background.level in INDIV_SELECTION_BACKGROUND_LEVELS
        background.level = when {
            indicateError && isSelected && individuallySelected -> CELL_BKGD_LEVEL_ERRORED_SELECTED_INDIV
            indicateError && isSelected && !individuallySelected -> CELL_BKGD_LEVEL_ERRORED_SELECTED
            indicateError && !isSelected -> CELL_BKGD_LEVEL_ERRORED
            !indicateError && isSelected && individuallySelected -> CELL_BKGD_LEVEL_SELECTED_INDIV
            !indicateError && isSelected && !individuallySelected -> CELL_BKGD_LEVEL_SELECTED
            else -> CELL_BKGD_LEVEL_NORMAL
        }
        val textColor = if (indicateError) Color.RED else Color.BLACK
        setTextColor(textColor)
    }

    /**
     * Updates cell background to selected or unselected while maintaining existing error state.
     */
    fun setSelection(selected: Boolean) {
        val showingError = background.level in ERRORED_BACKGROUND_LEVELS
        background.level = when {
            selected && showingError -> CELL_BKGD_LEVEL_ERRORED_SELECTED
            selected && !showingError -> CELL_BKGD_LEVEL_SELECTED
            !selected && showingError -> CELL_BKGD_LEVEL_ERRORED
            else -> CELL_BKGD_LEVEL_NORMAL
        }
    }

    /**
     * Updates cell background to be individually selected or generally selected while maintaining existing
     * error state. That is, a selected word can have an individually selected letter that stands out.
     */
    fun setIndividualSelection(individuallySelected: Boolean) {
        val showingError = background.level in ERRORED_BACKGROUND_LEVELS
        background.level = when {
            individuallySelected && showingError -> CELL_BKGD_LEVEL_ERRORED_SELECTED_INDIV
            individuallySelected && !showingError -> CELL_BKGD_LEVEL_SELECTED_INDIV
            !individuallySelected && showingError -> CELL_BKGD_LEVEL_ERRORED_SELECTED
            else -> CELL_BKGD_LEVEL_SELECTED
        }
    }

    //endregion
}

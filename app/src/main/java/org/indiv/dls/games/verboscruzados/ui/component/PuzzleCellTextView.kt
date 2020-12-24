package org.indiv.dls.games.verboscruzados.ui.component

import android.content.Context
import android.graphics.Color
import androidx.core.content.res.ResourcesCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import org.indiv.dls.games.verboscruzados.R
import org.indiv.dls.games.verboscruzados.util.extensions.isInNightMode
import kotlin.math.roundToInt


/**
 * Styled TextView used in puzzle.
 */
open class PuzzleCellTextView @JvmOverloads constructor(context: Context,
                                                        attrs: AttributeSet? = null,
                                                        defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private val fontColorBlack: Int = ResourcesCompat.getColor(resources, R.color.soft_black, null)
    private val fontColorWhite: Int = ResourcesCompat.getColor(resources, R.color.soft_white, null)
    private val fontColorRed: Int = Color.RED
    private val size = resources.getDimension(R.dimen.cell_width).roundToInt()
    private val fontHeight = size * FONT_SIZE_FRACTION
    private val fontHeightForCompoundText = size * FONT_SIZE_FRACTION * .7f

    //endregion

    //region INITIALIZER ---------------------------------------------------------------------------

    init {
        gravity = Gravity.CENTER

        typeface = ResourcesCompat.getFont(context, R.font.latoregular)

        // need to create Drawable object for each TextView
        background = ResourcesCompat.getDrawable(resources, R.drawable.cell_drawable, null)
        width = size
        height = size

        // default to normal text cell background (i.e. no error indication)
        background.level = if (isNightMode()) CELL_BKGD_LEVEL_DARK else CELL_BKGD_LEVEL_LIGHT
    }

    //endregion

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        private const val FONT_SIZE_FRACTION = .82f

        private const val CELL_BKGD_LEVEL_LIGHT = 1
        private const val CELL_BKGD_LEVEL_ERRORED = 2
        private const val CELL_BKGD_LEVEL_SELECTED = 3
        private const val CELL_BKGD_LEVEL_ERRORED_SELECTED = 4
        private const val CELL_BKGD_LEVEL_SELECTED_INDIV = 5 // Individual cell selected within selected word
        private const val CELL_BKGD_LEVEL_ERRORED_SELECTED_INDIV = 6
        private const val CELL_BKGD_LEVEL_DARK = 7

        private val ERRORED_BACKGROUND_LEVELS = listOf(CELL_BKGD_LEVEL_ERRORED,
                CELL_BKGD_LEVEL_ERRORED_SELECTED,
                CELL_BKGD_LEVEL_ERRORED_SELECTED_INDIV)
        private val INDIV_SELECTION_BACKGROUND_LEVELS = listOf(CELL_BKGD_LEVEL_SELECTED_INDIV,
                CELL_BKGD_LEVEL_ERRORED_SELECTED_INDIV)
    }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    /**
     * Fills TextView with the text from the user's answer. This can be longer than one character when there is a
     * conflict between the vertical and horizontal entries.
     *
     * @param userText the text to fill the textview with.
     */
    fun fillTextView(userText: String?) {
        text = userText
        setTextSize(TypedValue.COMPLEX_UNIT_PX,
                if (userText?.length ?: 0 > 1) fontHeightForCompoundText else fontHeight)
        setTextColor(fontColorBlack)
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
            isNightMode() -> CELL_BKGD_LEVEL_DARK
            else -> CELL_BKGD_LEVEL_LIGHT
        }
        setAppropriateTextColor(selected = isSelected, hasError = indicateError)
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
            isNightMode() -> CELL_BKGD_LEVEL_DARK
            else -> CELL_BKGD_LEVEL_LIGHT
        }
        setAppropriateTextColor(selected = selected, hasError = showingError)
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
        setAppropriateTextColor(selected = individuallySelected, hasError = showingError)
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    private fun isNightMode(): Boolean {
        return resources.isInNightMode()
    }

    private fun setAppropriateTextColor(selected: Boolean, hasError: Boolean) {
        val textColor = when {
            hasError -> fontColorRed
            selected -> fontColorBlack
            isNightMode() -> fontColorWhite
            else -> fontColorBlack
        }
        setTextColor(textColor)
    }

    //endregion
}

package org.indiv.dls.games.verboscruzados.component

import android.content.Context
import android.graphics.Color
import androidx.core.content.res.ResourcesCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import org.indiv.dls.games.verboscruzados.R
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
        background.level = CELL_BKGD_LEVEL_NORMAL // default to normal text cell background (i.e. no error indication)
    }

    //endregion

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        private const val FONT_SIZE_FRACTION = .82f

        private const val CELL_BKGD_LEVEL_NORMAL = 1
        private const val CELL_BKGD_LEVEL_ERRORED = 2
        private const val CELL_BKGD_LEVEL_SELECTED = 3
        private const val CELL_BKGD_LEVEL_ERRORED_SELECTED = 4
        private const val CELL_BKGD_LEVEL_SELECTED_INDIV = 5 // Individual cell selected within selected word
        private const val CELL_BKGD_LEVEL_ERRORED_SELECTED_INDIV = 6

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
            else -> CELL_BKGD_LEVEL_NORMAL
        }
        val textColor = if (indicateError) fontColorRed else fontColorBlack
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

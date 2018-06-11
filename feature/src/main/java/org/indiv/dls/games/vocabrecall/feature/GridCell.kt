package org.indiv.dls.games.vocabrecall.feature

import org.indiv.dls.games.vocabrecall.feature.db.GameWord

import android.widget.TextView

/**
 * Represents a cell in the puzzle.
 */
class GridCell(val char: Char) {

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    var gameWordAcross: GameWord? = null
    var gameWordDown: GameWord? = null
    var userCharAcross: Char? = null
    var userCharDown: Char? = null
    var view: PuzzleCellTextView? = null

    /**
     * Returns the dominant character of the two that the cell may contain (down or across).
     */
    val dominantUserChar: Char?
        get() = if (userCharAcross != null && userCharDown != null) {
            if (gameWordAcross!!.isConfident && !gameWordDown!!.isConfident) {
                userCharAcross
            } else {
                userCharDown
            }
        } else {
            userCharAcross ?: userCharDown
        }

    /**
     * @return true if the word of the dominant character has been marked confident.
     */
    val isDominantCharConfident: Boolean
        get() = userCharAcross != null && gameWordAcross!!.isConfident ||
                userCharDown != null && gameWordDown!!.isConfident

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    /**
     * Returns error if cell contains wrong value or empty.
     */
    fun hasUserError(): Boolean {
        return dominantUserChar != char
    }

    //endregion
}

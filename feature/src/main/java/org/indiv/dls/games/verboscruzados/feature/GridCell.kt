package org.indiv.dls.games.verboscruzados.feature

import org.indiv.dls.games.verboscruzados.feature.db.GameWord

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
            userCharDown
        } else {
            userCharAcross ?: userCharDown
        }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    /**
     * Returns error if cell contains wrong value or empty.
     */
    fun hasUserError(): Boolean {
        return dominantUserChar != char
    }

    /**
     * Returns error if cell contains wrong value for a word going across.
     */
    fun hasUserErrorAcross(): Boolean {
        return gameWordAcross != null && userCharAcross != char
    }

    /**
     * Returns error if cell contains wrong value for a word going down.
     */
    fun hasUserErrorDown(): Boolean {
        return gameWordDown != null && userCharDown != char
    }

    //endregion
}

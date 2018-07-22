package org.indiv.dls.games.verboscruzados.feature

import org.indiv.dls.games.verboscruzados.feature.component.PuzzleCellTextView
import org.indiv.dls.games.verboscruzados.feature.game.GameWord

/**
 * Represents a cell in the puzzle.
 */
class GridCell(val char: Char) {

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    var gameWordAcross: GameWord? = null
    var gameWordDown: GameWord? = null
    var userCharAcross: Char = GameWord.BLANK
        private set(value) { field = value }
    var userCharDown: Char = GameWord.BLANK
        private set(value) { field = value }
    var view: PuzzleCellTextView? = null

    /**
     * If both across and down characters are non-blank, they will be the same.
     */
    var userChar: Char
        get() = when {
            userCharDown != GameWord.BLANK -> userCharDown
            userCharAcross != GameWord.BLANK -> userCharAcross
            else -> GameWord.BLANK
        }
        set(value) {
            userCharAcross = value
            userCharDown = value
        }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    fun clearAcross() {
        userCharAcross = GameWord.BLANK
    }

    fun clearDown() {
        userCharDown = GameWord.BLANK
    }

    /**
     * Returns error if cell contains wrong value or empty.
     */
    fun hasUserError(): Boolean {
        return userChar != char
    }

    //endregion
}

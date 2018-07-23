package org.indiv.dls.games.verboscruzados.feature

import org.indiv.dls.games.verboscruzados.feature.component.PuzzleCellTextView
import org.indiv.dls.games.verboscruzados.feature.game.GameWord

/**
 * Represents a cell in the puzzle.
 */
class GridCell(val char: Char) {

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    var view: PuzzleCellTextView? = null
    var acrossIndex: Int = 0
    var downIndex: Int = 0
    var gameWordAcross: GameWord? = null
    var gameWordDown: GameWord? = null
    var userCharAcross: Char = GameWord.BLANK
    var userCharDown: Char = GameWord.BLANK

    /**
     * Default user-entered character to display.
     */
    val userChar: Char
        get() = when {
            userCharDown != GameWord.BLANK -> userCharDown
            else -> userCharAcross
        }

    /**
     * True if the across and down characters are both non-blank and conflict with each other.
     */
    val isConflict: Boolean
        get() = userCharAcross != GameWord.BLANK && userCharDown != GameWord.BLANK &&
                userCharAcross !=  userCharDown

    val isBlank: Boolean
        get() = userCharAcross == GameWord.BLANK && userCharDown == GameWord.BLANK

    val hasUserError: Boolean
        get() = userChar != char || isConflict

    //endregion

}

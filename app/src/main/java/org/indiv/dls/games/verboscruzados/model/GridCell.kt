package org.indiv.dls.games.verboscruzados.model

/**
 * Represents a cell in the puzzle.
 */
class GridCell(val char: Char) {

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    var acrossCharIndex: Int = 0
    var downCharIndex: Int = 0
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
        get() = userChar == GameWord.BLANK

    val hasUserError: Boolean
        get() = userChar != char || isConflict

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    fun wordAcrossStartsInCol(col: Int): Boolean {
       return gameWordAcross?.let {
           it.col == col
       } ?: false
    }

    fun wordDownStartsInRow(row: Int): Boolean {
       return gameWordDown?.let {
           it.row == row
       } ?: false
    }

    //endregion

}
package org.indiv.dls.games.verboscruzados.model

/**
 * Represents a cell in the puzzle.
 */
class GridCell(val answerChar: Char) {

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    var acrossCharIndex: Int = 0
    var downCharIndex: Int = 0
    var gameWordIdAcross: String? = null
    var gameWordIdDown: String? = null
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
        get() = userCharAcross != GameWord.BLANK
                && userCharDown != GameWord.BLANK
                && userCharAcross != userCharDown

    val isBlank: Boolean
        get() = userChar == GameWord.BLANK

    val hasUserError: Boolean
        get() = userChar != answerChar || isConflict

    //endregion
}

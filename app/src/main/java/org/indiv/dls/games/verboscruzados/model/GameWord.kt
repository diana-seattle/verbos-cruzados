package org.indiv.dls.games.verboscruzados.model

/**
 * Represents a word in the current game.
 */
data class GameWord(val id: String,                   // identifier used in cell grid
                    val persistenceKey: String,       // unique key for use in persistence
                    val answer: String,               // the conjugated verb which is the answer in the puzzle
                    val conjugationTypeLabel: String, // conjugation type label (e.g. "Preterite tense of")
                    val subjectPronounLabel: String,  // subject pronoun label (e.g. "Nosotros" or "" for gerund/past participle)
                    val infinitive: String,           // infinitive clue (e.g. "hablar (to speak)")
                    val translation: String,          // English translation
                    val statsIndex: Int,              // Stats index
                    val startingRow: Int,             // row in which the word begins
                    val startingCol: Int,             // column in which the word begins
                    val isAcross: Boolean,            // true if word appears in the across orientation, false if down
                    var userEntry: CharArray = CharArray(answer.length)) {   // text entered by the user

    companion object {
        const val BLANK = '\u0000'
    }

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    val isAnsweredCompletelyAndCorrectly: Boolean
        get() {
            userEntry.forEachIndexed { index, c ->
                if (c != answer[index]) {
                    return false
                }
            }
            return true
        }

    val hasErroredCells: Boolean
        get() {
            userEntry.forEachIndexed { index, c ->
                if (c != answer[index] && c != BLANK) {
                    return true
                }
            }
            return false
        }

    /**
     * When a word is auto-selected, the first letter of the word that needs changing is selected by default.
     */
    val defaultSelectionIndex: Int
        get() {
            for (i in answer.indices) {
                if (userEntry[i] != answer[i]) {
                    return i
                }
            }
            return answer.length - 1
        }

    fun setUserText(userText: String) {
        for (i in userEntry.indices) {
            userEntry[i] = if (i < userText.length) userText[i] else BLANK
        }
    }

    //endregion
}

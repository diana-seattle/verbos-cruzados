package org.indiv.dls.games.verboscruzados.feature.game

/**
 * Represents a word in the current game.
 */
class GameWord(val uniqueKey: String,            // unique key for use in persistence
               val word: String,                 // word conjugated verb used in the puzzle
               val conjugationTypeLabel: String, // conjugation type label (e.g. "Preterit")
               val subjectPronounLabel: String,  // subject pronoun label (e.g. "Nosotros" or "" for gerund/past participle)
               val infinitive: String,           // infinitive clue (e.g. "hablar (to speak)")
               val translation: String,          // English translation
               val statsIndex: Int,              // Stats index
               val row: Int,                     // row in which the word begins
               val col: Int,                     // column in which the word begins
               val isAcross: Boolean,            // true if word appears in the across orientation, false if down
               var userEntry: CharArray = CharArray(word.length)) {   // text entered by the user
    var userText: String
        get() {
            val sb = StringBuilder()
            userEntry.forEach {
                sb.append(it)
            }
            return sb.toString()
        }
        set(value) {
            for (i in 0 until value.length.coerceAtMost(userEntry.size)) {
                userEntry[i] = value[i]
            }
        }
    val isAnsweredCorrectly: Boolean
        get() {
            userEntry.forEachIndexed { index, c ->
                if (c != word.get(index)) {
                    return false
                }
            }
            return true
        }
    val isEntryEmpty: Boolean
        get() {
            return userEntry.none { it.isLetter() }
        }
    val defaultSelectionIndex: Int
        get() {
            for (i in 0 until word.length) {
                if (userEntry[i] != word[i]) {
                    return i
                }
            }
            return word.length - 1
        }
}
package org.indiv.dls.games.verboscruzados.feature.game

/**
 * Represents a word in the current game.
 */
class GameWord(val word: String,                  // word conjugated verb used in the puzzle
               val conjugationTypeLabel: String,  // conjugation type label (e.g. "Preterit")
               val pronounLabel: String,          // pronoun label (e.g. "(Yo)")
               val infinitive: String,            // infinitive clue (e.g. "hablar (to speak)")
               val translation: String,           // English translation
               val row: Int,                      // row in which the word begins
               val col: Int,                      // column in which the word begins
               val isAcross: Boolean,             // true if word appears in the across orientation, false if down
               var userText: String = "")         // text entered by the user

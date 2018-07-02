package org.indiv.dls.games.verboscruzados.feature.game

/**
 * Represents a word in the current game.
 */
class GameWord(val word: String,                  // word conjugated verb used in the puzzle
               val conjugationTypeLabel: String,  // pronoun label (e.g. "(Yo)")
               val pronounLabel: String,          // infinitive clue (e.g. "hablar (to speak)")
               val infinitiveClue: String,        // conjugation type label (e.g. "Preterit")
               val row: Int,                      // row in which the word begins
               val col: Int,                      // column in which the word begins
               val isAcross: Boolean,             // true if word appears in the across orientation, false if down
               var userText: String = "")         // text entered by the user

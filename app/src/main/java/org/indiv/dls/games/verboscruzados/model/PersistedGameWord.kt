package org.indiv.dls.games.verboscruzados.model

/**
 * This model is persisted as JSON, so do NOT rename fields.
 */
data class PersistedGameWord(val uniqueKey: String,            // unique key for use in persistence
                             val word: String,                 // the conjugated verb which is the answer in the puzzle
                             val conjugationTypeLabel: String, // conjugation type label (e.g. "Preterite tense of")
                             val subjectPronounLabel: String,  // subject pronoun label (e.g. "Nosotros" or "" for gerund/past participle)
                             val infinitive: String,           // infinitive clue (e.g. "hablar (to speak)")
                             val translation: String,          // English translation
                             val statsIndex: Int,              // Stats index
                             val row: Int,                     // row in which the word begins
                             val col: Int,                     // column in which the word begins
                             val isAcross: Boolean,            // true if word appears in the across orientation, false if down
                             var userEntry: CharArray = CharArray(word.length))   // text entered by the user

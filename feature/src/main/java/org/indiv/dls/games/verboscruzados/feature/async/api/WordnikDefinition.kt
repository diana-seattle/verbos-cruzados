package org.indiv.dls.games.verboscruzados.feature.async.api

/**
 * API model for definition from Wordnik service.
 */
data class WordnikDefinition(val partOfSpeech: String?,
                             val sourceDictionary: String?,
                             val text: String?,
                             val word: String?)

/*
Example:

{
textProns: [ ],
sourceDictionary: "ahd-legacy",
exampleUses: [ ],
relatedWords: [ ],
labels: [ ],
citations: [ ],
word: "majesty",
sequence: "0",
attributionText: "from The American Heritage® Dictionary of the English Language, 4th Edition",
partOfSpeech: "noun",
text: "The greatness and dignity of a sovereign.",
score: 0
}
 */
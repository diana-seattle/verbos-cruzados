package org.indiv.dls.games.vocabrecall.feature

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class AnswerPresentation(val word: String,
                         val wordHint: String,
                         val userText: String?,
                         val ahdDefinitions: List<String> = emptyList(),
                         val centuryDefinitions: List<String> = emptyList(),
                         val websterDefinitions: List<String> = emptyList(),
                         val wiktionaryDefinitions: List<String> = emptyList(),
                         val opposingPuzzleCellValues: Map<Int, PuzzleCellValue>) : Parcelable

/**
 * Represents a user-entered value in a cell and the confidence with which they entered it.
 */
@Parcelize
class PuzzleCellValue(val char: Char,
                      val confident: Boolean) : Parcelable
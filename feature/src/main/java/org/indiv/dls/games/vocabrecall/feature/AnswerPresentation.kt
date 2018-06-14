package org.indiv.dls.games.vocabrecall.feature

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class AnswerPresentation(val word: String,
                         val wordHint: String,
                         val userText: String?,
                         val ahdDefinitions: List<String>,
                         val centuryDefinitions: List<String>,
                         val websterDefinitions: List<String>,
                         val wiktionaryDefinitions: List<String>,
                         val opposingPuzzleCellValues: List<PuzzleCellValue>) : Parcelable

/**
 * Represents a user-entered value in a cell and the confidence with which they entered it.
 */
@Parcelize
class PuzzleCellValue(val position: Int,
                      val char: Char,
                      val confident: Boolean) : Parcelable
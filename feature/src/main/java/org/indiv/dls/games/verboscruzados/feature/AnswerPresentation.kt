package org.indiv.dls.games.verboscruzados.feature

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class AnswerPresentation(val word: String,
                         val userText: String?,
                         val clue: String,
                         val secondaryClue: String,
                         val opposingPuzzleCellValues: Map<Int, Char>) : Parcelable

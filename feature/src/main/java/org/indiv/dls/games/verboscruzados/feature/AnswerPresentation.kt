package org.indiv.dls.games.verboscruzados.feature

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class AnswerPresentation(val word: String,
                         val userText: String?,
                         val sentenceClueBeginning: String,
                         val sentenceClueEnd: String,
                         val infinitiveClue: String,
                         val conjugationTypeLabel: String,
                         val opposingPuzzleCellValues: Map<Int, Char>) : Parcelable

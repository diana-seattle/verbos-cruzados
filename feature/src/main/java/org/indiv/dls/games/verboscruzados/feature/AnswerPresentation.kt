package org.indiv.dls.games.verboscruzados.feature

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class AnswerPresentation(val word: String,
                         val across: Boolean,
                         val conjugationTypeLabel: String,
                         val subjectPronounLabel: String,
                         val infinitive: String,
                         val translation: String) : Parcelable

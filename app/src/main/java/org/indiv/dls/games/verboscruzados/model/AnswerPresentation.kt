package org.indiv.dls.games.verboscruzados.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class AnswerPresentation(val isAcross: Boolean,
                         val conjugationTypeLabel: String,
                         val subjectPronounLabel: String,
                         val infinitive: String,
                         val translation: String) : Parcelable

package org.indiv.dls.games.vocabrecall.feature

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Represents a user-entered value in a cell and the confidence with which they entered it.
 */
@Parcelize
class PuzzleCellValue(val position: Int,
                      val char: Char,
                      val confident: Boolean) : Parcelable
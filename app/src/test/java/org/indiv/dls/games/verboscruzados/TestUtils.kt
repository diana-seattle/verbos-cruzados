package org.indiv.dls.games.verboscruzados

import org.indiv.dls.games.verboscruzados.model.GameWord
import java.util.UUID

interface TestUtils {

    fun createGameWord(
            uniqueKey: String = UUID.randomUUID().toString(),
            word: String = "hablo",
            conjugationTypeLabel: String = "Present tense of",
            subjectPronounLabel: String = "Yo",
            infinitive: String = "hablar",
            translation: String = "speak",
            statsIndex: Int = 0,
            row: Int = 0,
            col: Int = 0,
            isAcross: Boolean = true): GameWord {
        return GameWord(
                uniqueKey = uniqueKey,
                word = word,
                conjugationTypeLabel = conjugationTypeLabel,
                subjectPronounLabel = subjectPronounLabel,
                infinitive = infinitive,
                translation = translation,
                statsIndex = statsIndex,
                row = row,
                col = col,
                isAcross = isAcross
        )
    }
}

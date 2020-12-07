package org.indiv.dls.games.verboscruzados

import org.indiv.dls.games.verboscruzados.model.GameWord
import java.util.UUID

interface TestUtils {

    fun createGameWord(
            id: String = UUID.randomUUID().toString(),
            persistenceKey: String = UUID.randomUUID().toString(),
            word: String = "hablo",
            conjugationTypeLabel: String = "Present tense of",
            subjectPronounLabel: String = "Yo",
            infinitive: String = "hablar",
            translation: String = "speak",
            statsIndex: Int = 0,
            startingRow: Int = 0,
            startingCol: Int = 0,
            isAcross: Boolean = true): GameWord {
        return GameWord(
                id = id,
                persistenceKey = persistenceKey,
                answer = word,
                conjugationTypeLabel = conjugationTypeLabel,
                subjectPronounLabel = subjectPronounLabel,
                infinitive = infinitive,
                translation = translation,
                statsIndex = statsIndex,
                startingRow = startingRow,
                startingCol = startingCol,
                isAcross = isAcross
        )
    }
}

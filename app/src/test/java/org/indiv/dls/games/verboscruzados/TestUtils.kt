package org.indiv.dls.games.verboscruzados

import org.indiv.dls.games.verboscruzados.model.AnswerPresentation
import org.indiv.dls.games.verboscruzados.model.GameWord
import org.indiv.dls.games.verboscruzados.model.PuzzleWordPresentation
import java.util.UUID

interface TestUtils {

    fun createGameWord(
            id: String = UUID.randomUUID().toString(),
            persistenceKey: String = UUID.randomUUID().toString(),
            answer: String = "hablo",
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
                answer = answer,
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

    fun createPuzzleWordPresentation(
            id: String = UUID.randomUUID().toString(),
            answer: String = "hablo",
            infinitive: String = "hablar",
            startingRow: Int = 0,
            startingCol: Int = 0,
            isAcross: Boolean = true): PuzzleWordPresentation {
        return PuzzleWordPresentation(
                id = id,
                answer = answer,
                infinitive = infinitive,
                startingRow = startingRow,
                startingCol = startingCol,
                isAcross = isAcross
        )
    }

    fun createAnswerPresentation(
            conjugationTypeLabel: String = "Present tense of",
            subjectPronounLabel: String = "Yo",
            infinitive: String = "hablar",
            translation: String = "speak",
            isAcross: Boolean = true): AnswerPresentation {
        return AnswerPresentation(
                conjugationTypeLabel = conjugationTypeLabel,
                subjectPronounLabel = subjectPronounLabel,
                infinitive = infinitive,
                translation = translation,
                isAcross = isAcross
        )
    }
}

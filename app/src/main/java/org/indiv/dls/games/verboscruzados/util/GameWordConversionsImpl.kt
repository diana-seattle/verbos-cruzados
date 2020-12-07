package org.indiv.dls.games.verboscruzados.util

import org.indiv.dls.games.verboscruzados.MainActivityViewModel
import org.indiv.dls.games.verboscruzados.model.AnswerPresentation
import org.indiv.dls.games.verboscruzados.model.GameWord
import org.indiv.dls.games.verboscruzados.model.PuzzleWordPresentation

class GameWordConversionsImpl : MainActivityViewModel.GameWordConversions {

    override fun toAnswerPresentation(gameWord: GameWord): AnswerPresentation {
        return AnswerPresentation(
                word = gameWord.answer,
                across = gameWord.isAcross,
                conjugationTypeLabel = gameWord.conjugationTypeLabel,
                subjectPronounLabel = gameWord.subjectPronounLabel,
                infinitive = gameWord.infinitive,
                translation = gameWord.translation)
    }

    override fun toPuzzleWordPresentation(gameWord: GameWord): PuzzleWordPresentation {
        return PuzzleWordPresentation(
                id = gameWord.id,
                word = gameWord.answer,
                startingRow = gameWord.startingRow,
                startingCol = gameWord.startingCol,
                isAcross = gameWord.isAcross,
                infinitive = gameWord.infinitive)
    }
}
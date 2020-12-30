package org.indiv.dls.games.verboscruzados.util

import org.indiv.dls.games.verboscruzados.model.GameWord
import org.indiv.dls.games.verboscruzados.model.PersistedGameWord

class PersistenceConversions(private val idGenerator: IdGenerator) {

    fun toPersistedGameWord(gameWord: GameWord): PersistedGameWord {
        return PersistedGameWord(
                uniqueKey = gameWord.persistenceKey,
                word = gameWord.answer,
                conjugationTypeLabel = gameWord.conjugationTypeLabel,
                subjectPronounLabel = gameWord.subjectPronounLabel,
                infinitive = gameWord.infinitive,
                translation = gameWord.translation,
                statsIndex = gameWord.statsIndex,
                row = gameWord.startingRow,
                col = gameWord.startingCol,
                isAcross = gameWord.isAcross,
                userEntry = gameWord.userEntry)
    }

    fun fromPersistedGameWord(persistedGameWord: PersistedGameWord): GameWord {
        return GameWord(
                id = idGenerator.generateId(),
                persistenceKey = persistedGameWord.uniqueKey,
                answer = persistedGameWord.word,
                conjugationTypeLabel = persistedGameWord.conjugationTypeLabel,
                subjectPronounLabel = persistedGameWord.subjectPronounLabel,
                infinitive = persistedGameWord.infinitive,
                translation = persistedGameWord.translation,
                statsIndex = persistedGameWord.statsIndex,
                startingRow = persistedGameWord.row,
                startingCol = persistedGameWord.col,
                isAcross = persistedGameWord.isAcross,
                userEntry = persistedGameWord.userEntry)
    }
}
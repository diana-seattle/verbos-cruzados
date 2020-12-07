package org.indiv.dls.games.verboscruzados.util

import java.util.UUID

object IdGenerator {
    fun generateId() = UUID.randomUUID().toString()
}
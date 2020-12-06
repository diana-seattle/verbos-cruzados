package org.indiv.dls.games.verboscruzados.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.indiv.dls.games.verboscruzados.TestUtils
import org.indiv.dls.games.verboscruzados.model.GridCell
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameSetupImplTest : TestUtils {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private lateinit var gameSetupImpl: GameSetupImpl

    private val gridWidth = 25
    private val gridHeight = 75

    @Before
    fun setUp() {
        gameSetupImpl = GameSetupImpl(context.resources)
    }

    @Test
    fun testNewGame() {
        val cellGrid: Array<Array<GridCell?>> = Array(gridHeight) { arrayOfNulls(gridWidth) }
        val gameOptions = emptyMap<String, Boolean>()

        // WHEN call made
        val result = gameSetupImpl.newGame(cellGrid, gameOptions)

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun testDoWordsFitInGrid() {

        val gameWords = listOf(createGameWord(), createGameWord())

        // WHEN call made
        val result = gameSetupImpl.doWordsFitInGrid(gameWords, 1, 1)

        assertFalse(result)
    }
}

package org.indiv.dls.games.verboscruzados.util

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.indiv.dls.games.verboscruzados.TestUtils
import org.indiv.dls.games.verboscruzados.model.GridCell
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P]) // to support Robolectric which maxes out at P
class GameSetupImplTest : TestUtils {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private lateinit var gameSetupImpl: GameSetupImpl

    private val gridWidth = 25
    private val gridHeight = 75

    @Before
    fun setUp() {
        gameSetupImpl = GameSetupImpl(context.resources, IdGenerator)
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
        assertFalse(gameSetupImpl.doWordsFitInGrid(gameWords, 1, 1))
        assertTrue(gameSetupImpl.doWordsFitInGrid(gameWords, 30, 30))
    }
}

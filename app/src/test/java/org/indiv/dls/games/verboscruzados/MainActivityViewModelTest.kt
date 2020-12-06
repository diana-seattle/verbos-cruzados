package org.indiv.dls.games.verboscruzados

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import org.indiv.dls.games.verboscruzados.model.GameWord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when` as whenever
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class MainActivityViewModelTest : TestUtils {
    // This causes LiveData objects to execute synchronously.
    @Rule @JvmField val instantExecutorRule = InstantTaskExecutorRule()

    @Mock private lateinit var screenMetrics: MainActivityViewModel.ScreenMetrics
    @Mock private lateinit var gameSetup: MainActivityViewModel.GameSetup
    @Mock private lateinit var gamePersistence: MainActivityViewModel.GamePersistence

    private lateinit var viewModel: MainActivityViewModel

    private val gridWidth = 5
    private val gridHeight = 7

    @Before fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(screenMetrics.gridWidth).thenReturn(gridWidth)
        whenever(screenMetrics.gridHeight).thenReturn(gridHeight)

        viewModel = MainActivityViewModelFactory(screenMetrics, gameSetup, gamePersistence)
                .create(MainActivityViewModel::class.java)
    }

    //region TESTS ---------------------------------------------------------------------------------

    @Test fun testInit() {
        assertEquals(gridHeight, viewModel.cellGrid.size)
        assertEquals(gridWidth, viewModel.cellGrid[0].size)
    }

    @Test fun testCurrentGameWordLiveData() {
        // Observe changes
        var gameWordReceived: GameWord? = null
        viewModel.currentGameWord.observeForever {
            gameWordReceived = it
        }

        // Trigger a LiveData event and verify received.
        listOf(null, createGameWord(), null).forEach {
            val charIndex = it?.defaultSelectionIndex ?: 0
            viewModel.selectNewGameWord(it, charIndex)
            assertEquals(it, gameWordReceived)
            assertEquals(charIndex, viewModel.charIndexOfSelectedCell)
        }
    }

    @Test fun testGameStartOrLoadEventLiveData() {
        // Observe changes
        var eventReceived: MainActivityViewModel.GameEvent? = null
        viewModel.gameStartOrLoadEvent.observeForever {
            eventReceived = it
        }

        // For each event type, trigger a LiveData event and verify received.
        listOf(MainActivityViewModel.GameEvent.CREATED, MainActivityViewModel.GameEvent.RELOADED).forEach {
            viewModel._gameStartOrLoadEvent.value = it
            assertEquals(it, eventReceived)
        }
    }

    @Test fun testGridHeight() {
        (1..2).forEach {
            whenever(screenMetrics.gridHeight).thenReturn(it)

            // WHEN call made
            val result = viewModel.gridHeight

            assertEquals(it, result)
        }
    }

    @Test fun testGridWidth() {
        (1..2).forEach {
            whenever(screenMetrics.gridWidth).thenReturn(it)

            // WHEN call made
            val result = viewModel.gridWidth

            assertEquals(it, result)
        }
    }

    @Test fun testGetKeyboardHeight() {
        (1..2).forEach {
            whenever(screenMetrics.keyboardHeight).thenReturn(it.toFloat())

            // WHEN call made
            val result = viewModel.keyboardHeight

            assertEquals(it.toFloat(), result)
        }
    }

    @Test fun testGetCurrentImageIndex() {
        (1..2).forEach {
            whenever(gamePersistence.currentImageIndex).thenReturn(it)

            // WHEN call made
            val result = viewModel.currentImageIndex

            assertEquals(it, result)
        }
    }

    @Test fun testSetCurrentImageIndex() {
        (1..2).forEach {
            // WHEN call made
            viewModel.currentImageIndex = it

            verify(gamePersistence).currentImageIndex = it
        }
    }

    @Test fun testGetCurrentGameCompleted() {
        listOf(false, true).forEach {
            whenever(gamePersistence.currentGameCompleted).thenReturn(it)

            // WHEN call made
            val result = viewModel.currentGameCompleted

            assertEquals(it, result)
        }
    }

    @Test fun testSetCurrentGameCompleted() {
        listOf(false, true).forEach {
            // WHEN call made
            viewModel.currentGameCompleted = it

            verify(gamePersistence).currentGameCompleted = it
        }
    }

    @Test fun testElapsedSecondsSnapshot() {
        assertEquals(0L, viewModel.elapsedSecondsSnapshot)

        (1L..3L).forEach {
            viewModel.addToElapsedSeconds(2)

            // WHEN call made
            val result = viewModel.elapsedSecondsSnapshot

            assertEquals(2 * it, result)
        }
    }

    @Test fun testPersistedElapsedSeconds() {
        (1L..2L).forEach {
            whenever(gamePersistence.elapsedSeconds).thenReturn(it)

            // WHEN call made
            val result = viewModel.persistedElapsedSeconds

            assertEquals(it, result)
            assertEquals(it, viewModel.elapsedSecondsSnapshot)
        }
    }

    @ExperimentalCoroutinesApi
    @Test fun testLaunchNewGame() {
        val gameWords = listOf(createGameWord(), createGameWord())
        val gameOptions = emptyMap<String, Boolean>()
        whenever(gamePersistence.currentGameOptions).thenReturn(gameOptions)
        whenever(gameSetup.newGame(viewModel.cellGrid, gameOptions))
                .thenReturn(gameWords)

        runBlockingTest {
            // WHEN call made
            viewModel.launchNewGame()

            delay(500) // TODO why is this needed?
        }

        assertEquals(gameWords, viewModel.currentGameWords)
    }

    @ExperimentalCoroutinesApi
    @Test fun testLoadGame() {
        val gameWords = listOf(createGameWord(), createGameWord())
        whenever(gamePersistence.currentGameWords).thenReturn(gameWords)
        whenever(gameSetup.doWordsFitInGrid(gameWords, gridWidth, gridHeight)).thenReturn(true)

        runBlockingTest {
            // WHEN call made
            viewModel.loadGame()

            delay(100) // TODO why is this needed?
        }

        assertEquals(gameWords, viewModel.currentGameWords)
    }

    @Test fun testClearGame() {
        // WHEN call made
        viewModel.clearGame()

        // Verify everything cleared
        assertTrue(viewModel.currentGameWords.isEmpty())
        assertNull(viewModel.currentGameWord.value)
        for (row in 0 until gridHeight) {
            for (col in 0 until gridWidth) {
                assertNull(viewModel.cellGrid[row][col])
            }
        }
    }

    @Test fun testPersistUserEntry() {
        val gameWord = createGameWord()

        // WHEN call made
        viewModel.persistUserEntry(gameWord)

        verify(gamePersistence).persistUserEntry(gameWord)
    }

    @Test fun testPersistGameStatistics() {
        // WHEN call made
        viewModel.persistGameStatistics()

        verify(gamePersistence).persistGameStats(viewModel.currentGameWords)
    }

    @Test fun testSelectNextGameWordAndWrapAround() {
        //todo
    }

    @Test fun testSelectNextGameWord() {
        //todo
    }

    @Test fun testNewScrollPositionShowingFullWord_horizontalWord() {
        // Select a game word
        val gameWord = createGameWord(row = 4, col = 2, isAcross = true)
        viewModel.selectNewGameWord(gameWord)

        // Expect a call to screenMetrics
        val currentScrollPosition = 50
        val expectedScrollPosition = 30
        whenever(screenMetrics.newScrollPositionShowingFullWord(gameWord.row, gameWord.row, gameWord.row, currentScrollPosition))
                .thenReturn(expectedScrollPosition)

        // WHEN call made
        val result = viewModel.newScrollPositionShowingFullWord(currentScrollPosition)

        // Verify expected scroll position returned.
        assertEquals(expectedScrollPosition, result)
    }

    @Test fun testNewScrollPositionShowingFullWord_verticalWord() {
        // Select a game word
        val gameWord = createGameWord(row = 4, col = 2, isAcross = false)
        val indexOfSelectedChar = 1
        viewModel.selectNewGameWord(gameWord, indexOfSelectedChar)

        // Expect a call to screenMetrics
        val currentScrollPosition = 50
        val expectedScrollPosition = 30
        whenever(screenMetrics.newScrollPositionShowingFullWord(
                startingRow = gameWord.row,
                endingRow = gameWord.row + gameWord.word.length - 1,
                rowOfSelectedCell = gameWord.row + indexOfSelectedChar,
                currentScrollPosition = currentScrollPosition))
                .thenReturn(expectedScrollPosition)

        // WHEN call made
        val result = viewModel.newScrollPositionShowingFullWord(currentScrollPosition)

        // Verify expected scroll position returned.
        assertEquals(expectedScrollPosition, result)
    }

    //endregion
}
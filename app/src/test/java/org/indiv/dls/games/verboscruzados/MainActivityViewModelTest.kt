package org.indiv.dls.games.verboscruzados

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import org.indiv.dls.games.verboscruzados.model.AnswerPresentation
import org.indiv.dls.games.verboscruzados.model.GridCell
import org.indiv.dls.games.verboscruzados.model.PuzzleWordPresentation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when` as whenever
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MainActivityViewModelTest : TestUtils {
    // This causes LiveData objects to execute synchronously.
    @Rule @JvmField val instantExecutorRule = InstantTaskExecutorRule()

    @Mock private lateinit var screenMetrics: MainActivityViewModel.ScreenMetrics
    @Mock private lateinit var gameSetup: MainActivityViewModel.GameSetup
    @Mock private lateinit var gamePersistence: MainActivityViewModel.GamePersistence
    @Mock private lateinit var gameWordConversions: MainActivityViewModel.GameWordConversions

    private lateinit var viewModel: MainActivityViewModel

    private val gridWidth = 5
    private val gridHeight = 7

    @Before fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(screenMetrics.gridWidth).thenReturn(gridWidth)
        whenever(screenMetrics.gridHeight).thenReturn(gridHeight)

        viewModel = MainActivityViewModelFactory(screenMetrics, gameSetup, gamePersistence, gameWordConversions)
                .create(MainActivityViewModel::class.java)
    }

    //region TESTS ---------------------------------------------------------------------------------

    @Test fun testInit() {
        assertEquals(gridHeight, viewModel.cellGrid.size)
        assertEquals(gridWidth, viewModel.cellGrid[0].size)
    }

    @Test fun testSelectedPuzzleWordLiveData() {
        // Observe changes
        var wordReceived: PuzzleWordPresentation? = null
        viewModel.selectedPuzzleWord.observeForever {
            wordReceived = it
        }

        // Create game of one word
        val gameWord = createGameWord()
        viewModel.gameWordMap = mapOf(gameWord.id to gameWord)
        whenever(gameWordConversions.toPuzzleWordPresentation(gameWord))
                .thenReturn(createPuzzleWordPresentation(id = gameWord.id))

        // Trigger LiveData events and verify received.
        listOf(null, gameWord, null).forEach {
            viewModel.selectNewGameWord(it?.id)
            assertEquals(it?.id, wordReceived?.id)
            assertEquals(it?.defaultSelectionIndex ?: 0, viewModel.charIndexOfSelectedCell)
        }
    }

    @Test fun testAnswerPresentationLiveData() {
        // Observe changes
        var answerPresentationReceived: AnswerPresentation? = null
        viewModel.answerPresentation.observeForever {
            answerPresentationReceived = it
        }

        // Create game of one word
        val gameWord = createGameWord()
        viewModel.gameWordMap = mapOf(gameWord.id to gameWord)
        whenever(gameWordConversions.toAnswerPresentation(gameWord))
                .thenReturn(createAnswerPresentation(infinitive = gameWord.infinitive))

        // Trigger LiveData events and verify received.
        listOf(null, gameWord, null).forEach {
            viewModel.selectNewGameWord(it?.id)
            assertEquals(it?.infinitive, answerPresentationReceived?.infinitive)
            assertEquals(it?.defaultSelectionIndex ?: 0, viewModel.charIndexOfSelectedCell)
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
    @Ignore("need to figure out how to test co-routines")
    @Test fun testLaunchNewGame() {
        val gameWords = listOf(createGameWord(), createGameWord())
        val gameOptions = emptyMap<String, Boolean>()
        whenever(gamePersistence.currentGameOptions).thenReturn(gameOptions)
        whenever(gameSetup.newGame(viewModel.cellGrid, gameOptions))
                .thenReturn(gameWords)

        runBlockingTest {
            // WHEN call made
            viewModel.launchNewGame()

            delay(200) // TODO why is this needed?
        }

        assertEquals(gameWords.size, viewModel.gameWordMap.size)
        gameWords.forEach {
            assertTrue(viewModel.gameWordMap.containsValue(it))
        }
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

        assertEquals(gameWords.size, viewModel.gameWordMap.size)
        gameWords.forEach {
            assertTrue(viewModel.gameWordMap.containsValue(it))
        }
    }

    @Test fun testClearGame() {
        // Setup a minimal 1-word game
        val gameWord = createGameWord()
        viewModel.gameWordMap = mapOf(gameWord.id to gameWord)
        viewModel.selectNewGameWord(gameWord.id)
        viewModel.charIndexOfSelectedCell = 0
        viewModel.cellGrid[0][0] = GridCell(answerChar = 'h')

        // WHEN call made
        viewModel.clearGame()

        // Verify everything cleared
        assertTrue(viewModel.gameWordMap.isEmpty())
        assertNull(viewModel.selectedPuzzleWord.value)
        for (row in 0 until gridHeight) {
            for (col in 0 until gridWidth) {
                assertNull(viewModel.cellGrid[row][col])
            }
        }
    }

    @Test fun testPersistUserEntry() {
        // Setup a minimal 1-word game
        val gameWord = createGameWord(startingRow = 0, startingCol = 0, isAcross = true)
        viewModel.gameWordMap = mapOf(gameWord.id to gameWord)
        viewModel.selectNewGameWord(gameWord.id)
        viewModel.charIndexOfSelectedCell = 0
        viewModel.cellGrid[0][0] = GridCell(answerChar = 'h').apply {
            gameWordIdAcross = gameWord.id
        }

        // WHEN call made
        val newChar = 'z'
        viewModel.updateCharOfSelectedCell(newChar)

        // verify updates made in game
        assertEquals(newChar, viewModel.cellGrid[0][0]?.userCharAcross)
        verify(gamePersistence).persistUserEntry(gameWord)
    }

    @Test fun testPersistGameStatistics() {
        // WHEN call made
        viewModel.persistGameStatistics()

        verify(gamePersistence).persistGameStats(viewModel.gameWordMap.values.toList())
    }

    @Test fun testSelectNextGameWordAndWrapAround() {
        //todo
    }

    @Test fun testSelectNextGameWord() {
        //todo
    }

    @Test fun testNewScrollPositionShowingFullWord_horizontalWord() {
        // Select a game word
        val gameWord = createGameWord(startingRow = 4, startingCol = 2, isAcross = true)
        viewModel.gameWordMap = mapOf(gameWord.id to gameWord)
        viewModel.selectNewGameWord(gameWord.id)

        // Expect a call to screenMetrics
        val currentScrollPosition = 50
        val expectedScrollPosition = 30
        whenever(screenMetrics.newScrollPositionShowingFullWord(gameWord.startingRow, gameWord.startingRow, gameWord.startingRow, currentScrollPosition))
                .thenReturn(expectedScrollPosition)

        // WHEN call made
        val result = viewModel.newScrollPositionShowingFullWord(currentScrollPosition)

        // Verify expected scroll position returned.
        assertEquals(expectedScrollPosition, result)
    }

    @Test fun testNewScrollPositionShowingFullWord_verticalWord() {
        // Select a game word
        val gameWord = createGameWord(startingRow = 4, startingCol = 2, isAcross = false)
        viewModel.gameWordMap = mapOf(gameWord.id to gameWord)
        val indexOfSelectedChar = 1
        viewModel.selectNewGameWord(gameWord.id, indexOfSelectedChar)

        // Expect a call to screenMetrics
        val currentScrollPosition = 50
        val expectedScrollPosition = 30
        whenever(screenMetrics.newScrollPositionShowingFullWord(
                startingRow = gameWord.startingRow,
                endingRow = gameWord.startingRow + gameWord.answer.length - 1,
                rowOfSelectedCell = gameWord.startingRow + indexOfSelectedChar,
                currentScrollPosition = currentScrollPosition))
                .thenReturn(expectedScrollPosition)

        // WHEN call made
        val result = viewModel.newScrollPositionShowingFullWord(currentScrollPosition)

        // Verify expected scroll position returned.
        assertEquals(expectedScrollPosition, result)
    }

    //endregion
}
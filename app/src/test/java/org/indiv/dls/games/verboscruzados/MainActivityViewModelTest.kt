package org.indiv.dls.games.verboscruzados

import android.os.Build
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.indiv.dls.games.verboscruzados.model.GameWord
import org.indiv.dls.games.verboscruzados.util.GameSetupImpl
import org.indiv.dls.games.verboscruzados.util.GamePersistenceImpl
import org.indiv.dls.games.verboscruzados.view.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
//@RunWith(MockitoJUnitRunner::class)
class MainActivityViewModelTest {

    @Mock private lateinit var gameSetup: GameSetupImpl

    @get:Rule var activityScenarioRule = activityScenarioRule<MainActivity>()

    private lateinit var mainActivity: MainActivity

    private lateinit var viewModel: MainActivityViewModel

    @Before
    public fun setUp() {
        MockitoAnnotations.initMocks(this)
        activityScenarioRule.scenario.onActivity { activity ->
            mainActivity = activity
            val persistenceHelper = GamePersistenceImpl(activity)
            viewModel = MainActivityViewModel(activity.resources, activity.theme, persistenceHelper, gameSetup)
        }
    }

    @Test fun testInit() {
        assertTrue(viewModel.keyboardHeight > 0)
        assertTrue(viewModel.gridHeight > 0)
        assertTrue(viewModel.gridWidth > 0)
        assertTrue(viewModel.cellGrid.isNotEmpty())
        assertTrue(viewModel.cellGrid[0].isNotEmpty())
    }

    @Test fun testGetReloadedGameWords() {
        // Observe new game creation, and launch new game
        var resultGameWords: List<GameWord>? = null
        viewModel.newlyCreatedGameWords.observe(mainActivity) { gameWords ->
            assertTrue(gameWords.isNotEmpty())
            resultGameWords = gameWords
        }
        viewModel.currentGameWord.observe(mainActivity) { gameWord ->
            // Verify selected word is the first game word.
            assertNotNull(gameWord)
            assertTrue(resultGameWords?.isNotEmpty() ?: false)
            assertEquals(resultGameWords!![0], gameWord)
            assertNull(gameWord?.userEntry!![0])

            // Now that the game is launched, observe a reload
            viewModel.reloadedGameWords.observe(mainActivity) { gameWords ->
                assertTrue(gameWords.isNotEmpty())
                val firstWord = gameWords[0]
                assertEquals(firstWord.word[0], firstWord.userEntry[0])
            }
            // Edit a game word and persist it, then reload game
            gameWord.setUserText(gameWord.word)
            viewModel.persistUserEntry(gameWord)
            viewModel.loadGame()
        }
        viewModel.launchNewGame()



    }

    fun testGetNewlyCreatedGameWords() {}

    fun testGetCharIndexOfSelectedCell() {}

    fun testSetCharIndexOfSelectedCell() {}

    fun testGetCurrentGameWords() {}

    fun testSetCurrentGameWords() {}

    fun testGetCellGrid() {}

    fun testGetKeyboardHeight() {}

    fun testGetViewablePuzzleHeight() {}

    fun testGetPuzzleMarginTopPixels() {}

    fun testGetPixelsPerCell() {}

    fun testGetGridHeight() {}

    fun testGetGridWidth() {}

    @Test fun testCurrentImageIndex() {
        val index = 5
        viewModel.currentImageIndex = index
        assertEquals(index, viewModel.currentImageIndex)
    }

    @Test fun testCurrentGameCompleted() {
        viewModel.currentGameCompleted = true
        assertTrue(viewModel.currentGameCompleted)

        viewModel.currentGameCompleted = false
        assertFalse(viewModel.currentGameCompleted)
    }

    @Test fun testElapsedSecondsSnapshot() {
        assertEquals(0L, viewModel.elapsedSecondsSnapshot)

        viewModel.addToElapsedSeconds(5)
        assertEquals(5, viewModel.elapsedSecondsSnapshot)
        assertEquals(5, viewModel.persistedElapsedSeconds)

        viewModel.addToElapsedSeconds(2)
        assertEquals(7, viewModel.elapsedSecondsSnapshot)
        assertEquals(7, viewModel.persistedElapsedSeconds)
    }

    fun testSelectNewGameWord() {}

    fun testLaunchNewGame() {}

    fun testLoadExistingGame() {}

    fun testClearGame() {}

    fun testPersistUserEntry() {}

    fun testPersistGameStatistics() {}

    fun testSelectNextGameWordAndWrapAround() {}

    fun testSelectNextGameWord() {}

    fun testNewScrollPositionShowingFullWord() {}
}
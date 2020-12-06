package org.indiv.dls.games.verboscruzados.ui

import android.os.Build
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P]) // to support Robolectric which maxes out at P
class PuzzleFragmentTest {

    @Test
    fun testWordSelectionNotification() {
        val scenario = launchFragmentInContainer<PuzzleFragment>()
        scenario.onFragment { fragment ->
            // Observe when the game is set up
            fragment.viewModel.gameStartOrLoadEvent.observe(fragment.viewLifecycleOwner) {
                // Verify game is set up and first word of puzzle is selected
                Assert.assertTrue(fragment.viewModel.currentGameWords.isNotEmpty())
                Assert.assertNotNull(fragment.viewModel.currentGameWord.value)

                // Set up puzzle in the fragment
                fragment.createGridViewsAndSelectWord()

                // Verify first word is still selected
                Assert.assertNotNull(fragment.viewModel.currentGameWord.value)

                if (fragment.viewModel.currentGameWords.size > 1) {
                    fragment.viewModel.currentGameWord.value?.let { gameWord ->

                        // Select the next game word
                        fragment.viewModel.selectNextGameWord(gameWord.row, gameWord.col, false)

                        // Verify newly selected game word
                        Assert.assertNotNull(fragment.viewModel.currentGameWord.value)
                        val newlySelected = fragment.viewModel.currentGameWord.value!!
                        assertNotEquals(newlySelected, gameWord)

                        // Verify fragment notified of selection change.
                        assertEquals(newlySelected, fragment.gameWordLastSelected)
                    }
                }
            }
        }

    }
}

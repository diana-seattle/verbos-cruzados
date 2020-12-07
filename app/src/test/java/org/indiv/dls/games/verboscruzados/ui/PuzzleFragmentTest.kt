package org.indiv.dls.games.verboscruzados.ui

import android.os.Build
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
                assertTrue(fragment.viewModel.wordCount > 0)
                assertNotNull(fragment.viewModel.selectedPuzzleWord.value)
                val initialWordSelection = fragment.viewModel.selectedPuzzleWord.value

                // Set up puzzle in the fragment
                fragment.createGridViewsAndSelectWord()

                // Verify first word is still selected
                assertEquals(initialWordSelection, fragment.viewModel.selectedPuzzleWord.value)

                fragment.viewModel.selectedPuzzleWord.value?.let { initialWord ->

                    // Select the next game word
                    fragment.viewModel.selectNextGameWord(initialWord.startingRow, initialWord.startingCol, false)

                    // Verify newly selected game word
                    assertNotNull(fragment.viewModel.selectedPuzzleWord.value)
                    val newlySelected = fragment.viewModel.selectedPuzzleWord.value!!
                    assertNotEquals(newlySelected, initialWord)

                    // Verify fragment notified of selection change.
                    assertEquals(newlySelected, fragment.puzzleWordLastSelected)
                }
            }
        }
    }
}

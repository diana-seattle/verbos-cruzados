package org.indiv.dls.games.verboscruzados.ui

import android.os.Build
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.indiv.dls.games.verboscruzados.R
import org.indiv.dls.games.verboscruzados.model.GameWord

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P]) // to support Robolectric which maxes out at P
class MainActivityTest {

    @get:Rule var activityScenarioRule = activityScenarioRule<MainActivity>()

    @Test fun testOnCreate() {
        activityScenarioRule.scenario.onActivity { activity ->
            assertTrue(activity.viewModel.currentGameWords.isNotEmpty())
            assertNotNull(activity.viewModel.currentGameWord.value)
            assertNotNull(activity.viewModel.currentGameWord.hasActiveObservers())
            assertNotNull(activity.viewModel.gameStartOrLoadEvent.hasActiveObservers())
            assertFalse(activity.viewModel.showingErrors)
            assertTrue(activity.viewModel.showOnboardingMessage)
            assertTrue(activity.viewModel.gridWidth > 0)
            assertTrue(activity.viewModel.gridHeight > 0)
            assertTrue(activity.viewModel.keyboardHeight > 0)
            assertEquals(0, activity.viewModel.elapsedSecondsSnapshot)
            assertFalse(activity.viewModel.currentGameCompleted)
        }
    }

    @Test fun testKeyboardInfinitiveButton() {
        activityScenarioRule.scenario.onActivity { activity ->
            assertNotNull(activity.viewModel.currentGameWord.value)
            activity.viewModel.currentGameWord.value?.let { gameWord ->
                // Verify current game word has no user text
                gameWord.userEntry.forEachIndexed { index, letter ->
                    assertEquals(GameWord.BLANK, letter)
                }

                // Click on the keyboard's infinitive button
                onView(withId(R.id.keyboard_button_infinitive)).perform(click())

                // Verify current game word has the infinitive text
                gameWord.userEntry.forEachIndexed { index, letter ->
                    val expectedLetter = if (index < gameWord.infinitive.length)
                        gameWord.infinitive.get(index) else GameWord.BLANK
                    assertEquals(expectedLetter, letter)
                }
            }
        }
    }

    @Test fun testKeyboarNextButton() {
        activityScenarioRule.scenario.onActivity { activity ->
            assertNotNull(activity.viewModel.currentGameWord.value)
            activity.viewModel.currentGameWord.value?.let { gameWord ->
                // Verify initial selection starts in the top left corner and goes across
                assertEquals(0, gameWord.row)
                assertEquals(0, gameWord.col)
                assertTrue(gameWord.isAcross)

                if (activity.viewModel.currentGameWords.size > 1) {
                    // Click on the keyboard's "next" button
                    onView(withId(R.id.button_next_word)).perform(click())

                    // Verify new game word has been selected and is vertical.
                    assertNotNull(activity.viewModel.currentGameWord.value)
                    activity.viewModel.currentGameWord.value?.let { newGameWord ->
                        assertEquals(0, newGameWord.row)
                        assertFalse(newGameWord.isAcross)
                    }
                }
            }
        }
    }

    @Test fun testKeyboardLetterButtons() {
        activityScenarioRule.scenario.onActivity { activity ->
            assertNotNull(activity.viewModel.currentGameWord.value)
            activity.viewModel.currentGameWord.value?.let { gameWord ->
                // Verify current game word has no user text
                gameWord.userEntry.forEachIndexed { index, letter ->
                    assertEquals(GameWord.BLANK, letter)
                }

                // Verify selection is on the first letter
                assertEquals(0, activity.viewModel.charIndexOfSelectedCell)

                // All game words will be longer than one character, but check to be safe
                if (gameWord.word.length > 1) {

                    val letterButtonMap = mapOf(R.id.button_a to 'a', R.id.button_b to 'b', R.id.button_c to 'c',
                            R.id.button_d to 'd', R.id.button_e to 'e', R.id.button_f to 'f', R.id.button_g to 'g',
                            R.id.button_h to 'h', R.id.button_i to 'i', R.id.button_j to 'j', R.id.button_k to 'k',
                            R.id.button_l to 'l', R.id.button_m to 'm', R.id.button_n to 'n', R.id.button_o to 'o',
                            R.id.button_p to 'p', R.id.button_q to 'q', R.id.button_r to 'r', R.id.button_s to 's',
                            R.id.button_t to 't', R.id.button_u to 'u', R.id.button_v to 'v', R.id.button_w to 'w',
                            R.id.button_x to 'x', R.id.button_y to 'y', R.id.button_z to 'z',
                            R.id.button_a_accent to 'á', R.id.button_e_accent to 'é', R.id.button_i_accent to 'í',
                            R.id.button_o_accent to 'ó', R.id.button_u_accent to 'ú', R.id.button_u_umlaut to 'ü',
                            R.id.button_n_tilde to 'ñ')

                    letterButtonMap.forEach {
                        // Click on the keyboard's letter button
                        onView(withId(it.key)).perform(click())

                        // Verify current game word has the new text
                        assertEquals(it.value, gameWord.userEntry[0])

                        // Verify selection has moved to the next letter
                        assertEquals(1, activity.viewModel.charIndexOfSelectedCell)

                        // Click on the keyboard's left/up button to return to the first character and confirm
                        onView(withId(R.id.button_left_arrow)).perform(click())
                        assertEquals(0, activity.viewModel.charIndexOfSelectedCell)
                    }
                }
            }
        }
    }
}

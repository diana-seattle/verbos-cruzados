package org.indiv.dls.games.verboscruzados.util

import android.os.Build
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.indiv.dls.games.verboscruzados.ui.MainActivity

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1]) // to support Robolectric which maxes out at P
class ScreenMetricsImplTest {

    @get:Rule var activityScenarioRule = activityScenarioRule<MainActivity>()

    @Test fun testNewScrollPositionShowingFullWord() {
        val startingRow = 0
        val endingRow = 0
        val rowOfSelectedCell = 0
        val currentScrollPosition = 200

        activityScenarioRule.scenario.onActivity { activity ->
            val screenMetrics = ScreenMetricsImpl(activity)

            // WHEN call made
            val result = screenMetrics.newScrollPositionShowingFullWord(
                    startingRow,
                    endingRow,
                    rowOfSelectedCell,
                    currentScrollPosition
            )

            // Verify scrolling is necessary
            assertNotNull(result)
        }
    }
}

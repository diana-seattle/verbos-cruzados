package org.indiv.dls.games.verboscruzados

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.indiv.dls.games.verboscruzados.game.PersistenceHelper

/**
 * Factory class for creating this view model.
 */
class MainActivityViewModelFactory(private val screenMetrics: MainActivityViewModel.ScreenMetrics,
                                   private val gameSetup: MainActivityViewModel.GameSetup,
                                   private val gamePersistence: MainActivityViewModel.GamePersistence) : ViewModelProvider.Factory {

    constructor(activity: Activity) : this(
            ScreenMetricsImpl(activity),
            GameSetupImpl(activity.resources),
            PersistenceHelper(activity)
    )

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(
                    screenMetrics,
                    gameSetup,
                    gamePersistence
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
package org.indiv.dls.games.verboscruzados.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.indiv.dls.games.verboscruzados.util.GameSetupImpl
import org.indiv.dls.games.verboscruzados.util.GamePersistenceImpl
import org.indiv.dls.games.verboscruzados.util.GameWordConversionsImpl
import org.indiv.dls.games.verboscruzados.util.IdGenerator
import org.indiv.dls.games.verboscruzados.util.PersistenceConversions
import org.indiv.dls.games.verboscruzados.util.ScreenMetricsImpl

/**
 * Factory class for creating the main activity view model.
 */
class MainActivityViewModelFactory(private val screenMetrics: MainActivityViewModel.ScreenMetrics,
                                   private val gameSetup: MainActivityViewModel.GameSetup,
                                   private val gamePersistence: MainActivityViewModel.GamePersistence,
                                   private val gameWordConversions: MainActivityViewModel.GameWordConversions,
) : ViewModelProvider.Factory {

    // View controllers will use this secondary constructor. Unit tests can use the primary constructor above.
    constructor(activity: Activity) : this(
            ScreenMetricsImpl(activity),
            GameSetupImpl(activity.resources, IdGenerator),
            GamePersistenceImpl(activity, PersistenceConversions(IdGenerator)),
            GameWordConversionsImpl()
    )

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(
                    screenMetrics,
                    gameSetup,
                    gamePersistence,
                    gameWordConversions
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
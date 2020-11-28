package org.indiv.dls.games.verboscruzados

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.indiv.dls.games.verboscruzados.game.GameWord

class MainActivityViewModel : ViewModel() {
    val currentGameWord = MutableLiveData<GameWord?>()

}

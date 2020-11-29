package org.indiv.dls.games.verboscruzados

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.indiv.dls.games.verboscruzados.game.GameWord

class MainActivityViewModel : ViewModel() {

    // The character index within the selected word of the selected cell.
    var charIndexOfSelectedCell = 0

    // Currently selected word in a game. Allow callers to read the current value, but set new value via a function.
    private val _currentGameWord = MutableLiveData<GameWord?>()
    val currentGameWord = _currentGameWord as LiveData<GameWord?>

    /**
     * Allows callers on the main thread to update the current game word together with the char index of the selected cell.
     */
    fun selectNewGameWord(gameWord: GameWord?, charIndexOfSelectedCell: Int) {
        // Set the index first so that it's available to observers of the game word.
        this.charIndexOfSelectedCell = charIndexOfSelectedCell
        _currentGameWord.value = gameWord
    }
}

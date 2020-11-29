package org.indiv.dls.games.verboscruzados

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import org.indiv.dls.games.verboscruzados.game.GameWord
import org.indiv.dls.games.verboscruzados.game.PersistenceHelper


class MainActivityViewModel(val context: Context) : ViewModel() {

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    // Make the mutable live data private and only update it internally via a setter function.
    private val _currentGameWord = MutableLiveData<GameWord?>()

    private val persistenceHelper = PersistenceHelper(context)

    //endregion

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    // Currently selected word in a game. Allows callers to read the current value from the immutable reference.
    // It must be set thru the setter function.
    val currentGameWord = _currentGameWord as LiveData<GameWord?>

    // The character index within the selected word of the selected cell.
    var charIndexOfSelectedCell = 0

    // Game words loaded from StoredPreferences
    val reloadedGameWords: LiveData<List<GameWord>> = liveData(context = viewModelScope.coroutineContext + Dispatchers.Default) {
        val gameWords = persistenceHelper.currentGameWords
        emit(gameWords)
    }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    /**
     * Allows callers on the main thread to update the current game word together with the char index of the selected cell.
     */
    fun selectNewGameWord(gameWord: GameWord?, charIndexOfSelectedCell: Int) {
        // Set the index first so that it's available to observers of the game word.
        this.charIndexOfSelectedCell = charIndexOfSelectedCell
        _currentGameWord.value = gameWord
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

//    private fun /*suspend*/ loadNewOrExistingGame(): List<GameWord> {
//        // get current game if any
//        val gameWords = persistenceHelper.currentGameWords
//
//        setPuzzleBackgroundImage(persistenceHelper.currentImageIndex)
//
//        // if on very first game, or if no saved game (due to an error), create a new one, otherwise open existing game
//        if (gameWords.isEmpty() || !puzzleFragment.doWordsFitInGrid(gameWords)) {
//            gameSetup.newGame(resources, puzzleFragment.cellGrid, persistenceHelper.currentGameOptions)
//            showOnboarding = true
//        } else {
//            restoreExistingGame()
//        }
//
//    }

    //endregion

    //region INNER CLASSES -------------------------------------------------------------------------

    /**
     * Factory class for creating this view model with an application context.
     */
    class Factory(val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
                return MainActivityViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")        }
    }

    //endregion
}

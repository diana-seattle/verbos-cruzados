package org.indiv.dls.games.verboscruzados

import android.content.Context
import android.content.res.loader.ResourcesProvider
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import org.indiv.dls.games.verboscruzados.game.GameWord


class MainActivityViewModel(val context: Context) : ViewModel() {

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    // Make the mutable live data private and only update it internally via a setter function.
    private val _currentGameWord = MutableLiveData<GameWord?>()

    //endregion

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    // Currently selected word in a game. Allows callers to read the current value from the immutable reference.
    // It must be set thru the setter function.
    val currentGameWord = _currentGameWord as LiveData<GameWord?>

    // The character index within the selected word of the selected cell.
    var charIndexOfSelectedCell = 0

    //    val cellGrid: LiveData<Array<Array<GridCell?>>> = liveData(context = viewModelScope.coroutineContext + Dispatchers.Default) {
//        val data = database.loadUser() // loadUser is a suspend function.
//        emit(data)
//    }

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

package org.indiv.dls.games.verboscruzados

import android.app.Activity
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.indiv.dls.games.verboscruzados.async.GameSetup
import org.indiv.dls.games.verboscruzados.game.GameWord
import org.indiv.dls.games.verboscruzados.game.PersistenceHelper


class MainActivityViewModel(val activity: Activity) : ViewModel() {

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    // Currently selected word in a game. Allows callers to read the current value from the immutable reference.
    private val _currentGameWord = MutableLiveData<GameWord?>()

    // Game words loaded from StoredPreferences
    private val _reloadedGameWords: MutableLiveData<List<GameWord>> by lazy {
        MutableLiveData<List<GameWord>>()
    }

    // Game words newly generated
    private val _newlyCreatedGameWords: MutableLiveData<List<GameWord>> by lazy {
        MutableLiveData<List<GameWord>>()
    }

    private val persistenceHelper = PersistenceHelper(activity)
    private val gameSetup = GameSetup()

    //endregion

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    // Public immutable accessors
    val currentGameWord = _currentGameWord as LiveData<GameWord?>
    val reloadedGameWords = _reloadedGameWords as LiveData<List<GameWord>>
    val newlyCreatedGameWords = _newlyCreatedGameWords as LiveData<List<GameWord>>

    // The character index within the selected word of the selected cell.
    var charIndexOfSelectedCell = 0

    var currentGameWords: List<GameWord> = emptyList()

    // Grid of cells making up the puzzle, plus various dimensions
    val cellGrid: Array<Array<GridCell?>>
    val keyboardHeight: Float
    val viewablePuzzleHeight: Float
    val puzzleMarginTopPixels: Float
    val pixelsPerCell: Float
    val gridHeight: Int
    val gridWidth: Int


    var currentImageIndex: Int
        get() = persistenceHelper.currentImageIndex
        set(value) {
            persistenceHelper.currentImageIndex = value
        }

    var currentGameCompleted: Boolean
        get() = persistenceHelper.currentGameCompleted
        set(value) {
            persistenceHelper.currentGameCompleted = value
        }

    var elapsedSecondsSnapshot = 0L // Last set or retrieved elapsed seconds value
    val persistedElapsedSeconds: Long
        get() {
            elapsedSecondsSnapshot = persistenceHelper.elapsedSeconds
            return elapsedSecondsSnapshot
        }

    //endregion

    //region INITIALIZER ---------------------------------------------------------------------------

    init {
        val resources = activity.resources
        keyboardHeight = resources.getDimension(R.dimen.keyboard_height)
        val displayMetrics = resources.displayMetrics
        val configuration = resources.configuration
        puzzleMarginTopPixels = resources.getDimension(R.dimen.puzzle_margin_top)
        val puzzleMarginSidePixels = resources.getDimension(R.dimen.puzzle_margin_side)
        val totalPuzzleMarginTopPixels = puzzleMarginTopPixels * 2
        val totalPuzzleMarginSidePixels = puzzleMarginSidePixels * 2
        val actionBarHeightPixels = getActionBarHeightInPixels(displayMetrics, activity.theme)
        val screenWidthDp = configuration.smallestScreenWidthDp
        val screenHeightDp = maxOf(configuration.screenHeightDp, configuration.screenWidthDp)
        val heightFactor = when {
            screenWidthDp < 350 -> 2f
            screenWidthDp < 450 -> 1.5f
            else -> 1f
        }
        val screenWidthPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                screenWidthDp.toFloat(), displayMetrics)
        val screenHeightPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                screenHeightDp.toFloat(), displayMetrics)
        viewablePuzzleHeight = screenHeightPixels - actionBarHeightPixels - totalPuzzleMarginTopPixels
        val puzzleHeightPixels = viewablePuzzleHeight * heightFactor
        val puzzleWidthPixels = screenWidthPixels - totalPuzzleMarginSidePixels

        // calculate number of pixels equivalent to 24dp (24dp allows 13 cells on smallest screen supported by Android (320dp width, 426dp height))
        pixelsPerCell = resources.getDimension(R.dimen.cell_width)
        gridHeight = (puzzleHeightPixels / pixelsPerCell).toInt()
        gridWidth = (puzzleWidthPixels / pixelsPerCell).toInt()

        cellGrid = Array(gridHeight) { arrayOfNulls(gridWidth) }
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

    /**
     * Launches new game, setting up on a worker thread.
     */
    fun launchNewGame() {
        viewModelScope.launch(context = Dispatchers.Default) {
            val gameWords = gameSetup.newGame(activity.resources, cellGrid, persistenceHelper.currentGameOptions)
            persistenceHelper.currentGameWords = gameWords
            persistenceHelper.currentGameCompleted = false
            persistenceHelper.elapsedSeconds = 0L
            elapsedSecondsSnapshot = 0L
            currentGameWords = gameWords
            _newlyCreatedGameWords.postValue(gameWords)
        }
    }

    /**
     * Loads existing game on a worker thread.
     */
    fun loadExistingGame() {
        viewModelScope.launch(context = Dispatchers.Default) {
            val gameWords = persistenceHelper.currentGameWords
            if (gameSetup.doWordsFitInGrid(gameWords, gridWidth, gridHeight)) {
                currentGameWords = gameWords
                gameSetup.addToGrid(gameWords, cellGrid)
                _reloadedGameWords.postValue(gameWords)
            } else {
                _reloadedGameWords.postValue(emptyList())
            }
        }
    }

    fun clearGame() {
        selectNewGameWord(null, 0)
        for (row in 0 until gridHeight) {
            cellGrid[row].fill(null)
        }
    }

    fun addToElapsedSeconds(seconds: Long) {
        elapsedSecondsSnapshot += seconds
        persistenceHelper.elapsedSeconds = elapsedSecondsSnapshot
    }

    fun persistUserEntry(gameWord: GameWord) {
        viewModelScope.launch(context = Dispatchers.Default) {
            persistenceHelper.persistUserEntry(gameWord)
        }
    }

    fun persistGameStatistics() {
        viewModelScope.launch(context = Dispatchers.Default) {
            persistenceHelper.persistGameStats(currentGameWords)
        }
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    private fun getActionBarHeightInPixels(displayMetrics: DisplayMetrics, theme: Resources.Theme): Int {
        // actionBar.getHeight() returns zero in onCreate (i.e. before it is shown)
        // for the following solution, see: http://stackoverflow.com/questions/12301510/how-to-get-the-actionbar-height/13216807#13216807
        var actionBarHeight = 0  // actionBar.getHeight() returns zero in onCreate
        val tv = TypedValue()
        if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, displayMetrics)
        }
        return actionBarHeight
    }

    //endregion

    //region INNER CLASSES -------------------------------------------------------------------------

    /**
     * Factory class for creating this view model with an application context.
     */
    class Factory(private val activity: Activity) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
                return MainActivityViewModel(activity) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")        }
    }

    //endregion
}

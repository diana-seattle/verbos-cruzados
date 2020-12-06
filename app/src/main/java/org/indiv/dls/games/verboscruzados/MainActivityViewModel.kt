package org.indiv.dls.games.verboscruzados

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.indiv.dls.games.verboscruzados.model.GameWord
import org.indiv.dls.games.verboscruzados.model.GridCell

/**
 * ViewModel class for the Main activity based on Google's architectural components: https://developer.android.com/jetpack/guide
 */
class MainActivityViewModel(
        private val screenMetrics: ScreenMetrics,
        private val gameSetup: GameSetup,
        private val gamePersistence: GamePersistence
) : ViewModel() {

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    // Private mutable LiveData representing currently selected word in a game.
    private val _currentGameWord = MutableLiveData<GameWord?>()

    // Private mutable LiveData representing event when a game is loaded or started.
    @VisibleForTesting val _gameStartOrLoadEvent: MutableLiveData<GameEvent> by lazy {
        MutableLiveData<GameEvent>()
    }

    //endregion

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    // Public immutable LiveData representing currently selected word in a game.
    val currentGameWord = _currentGameWord as LiveData<GameWord?>

    // Public immutable LiveData representing event when a game is loaded or started.
    val gameStartOrLoadEvent = _gameStartOrLoadEvent as LiveData<GameEvent>

    // List of word data for the current game
    var currentGameWords: List<GameWord> = emptyList()

    // The character index within the selected word of the selected cell.
    var charIndexOfSelectedCell = 0

    // Set to true when saved data indicates initial installation of the game, whereby the user needs initial instruction.
    var showOnboardingMessage = false

    // This mode displays all empty and errored cells in red. It also auto-advances to the next incomplete word once a
    // word is completed correctly.
    var showingErrors = false

    // Grid of cells making up the puzzle and transparent puzzle background.
    val cellGrid: Array<Array<GridCell?>>

    // Game dimensions
    val gridHeight: Int
        get() = screenMetrics.gridHeight
    val gridWidth: Int
        get() = screenMetrics.gridWidth
    val keyboardHeight: Float
        get() = screenMetrics.keyboardHeight

    // Index of currently selected background image for the game
    var currentImageIndex: Int
        get() = gamePersistence.currentImageIndex
        set(value) {
            gamePersistence.currentImageIndex = value
        }

    // A game can be completed and the user can elect to not start a new game. In this state, we need to do some special
    // things such as not run the timer. Or if they un-finish and re-finish, we don't want to give them credit again in
    // the game stats.
    var currentGameCompleted: Boolean
        get() = gamePersistence.currentGameCompleted
        set(value) {
            gamePersistence.currentGameCompleted = value
        }

    // The elapsed time of the game is retrieved whenever a game is resumed. The main activity tracks a delta of elapsed
    // time after resuming, and then when pausing, calls [addToElapsedSeconds] with the delta to update the persisted
    // value. The snapshot variable allows the main activity to request the latest retrieved/saved value without
    // performing i/o.
    var elapsedSecondsSnapshot = 0L // Last set or retrieved elapsed seconds value
    val persistedElapsedSeconds: Long
        get() {
            elapsedSecondsSnapshot = gamePersistence.elapsedSeconds
            return elapsedSecondsSnapshot
        }

    //endregion

    //region INITIALIZER ---------------------------------------------------------------------------

    init {
        // Initialize the grid of cells based on screen dimensions. This is the board on which a game will be set up.
        cellGrid = Array(screenMetrics.gridHeight) { arrayOfNulls(screenMetrics.gridWidth) }
    }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    /**
     * Allows callers on the main thread to update the current game word together with the char index of the selected cell.
     */
    fun selectNewGameWord(gameWord: GameWord?, charIndexOfSelectedCell: Int? = null) {
        // Set the index first so that it's available to observers of the game word change.
        this.charIndexOfSelectedCell = charIndexOfSelectedCell ?: gameWord?.defaultSelectionIndex ?: 0

        // Update the LiveData value to publish to observers.
        _currentGameWord.value = gameWord
    }

    /**
     * Launches new game, with setup up on a worker thread.
     */
    fun launchNewGame() {
        // Do with coroutine on worker thread
        viewModelScope.launch(context = Dispatchers.Default) {
            setupNewGame()
        }
    }

    /**
     * Loads an existing game on a worker thread, starting new game if none found.
     */
    fun loadGame() {
        // On app startup, the word list will be empty, but on a config change it likely won't be so no need to re-load.
        if (currentGameWords.isEmpty()) {
            // Do with coroutine on worker thread
            viewModelScope.launch(context = Dispatchers.Default) {
                val gameWords = gamePersistence.currentGameWords
                if (gameWords.isNotEmpty() && gameSetup.doWordsFitInGrid(gameWords, gridWidth, gridHeight)) {
                    gameSetup.addToGrid(gameWords, cellGrid)
                    currentGameWords = gameWords
                    // Must use postValue() from non-main thread.
                    _gameStartOrLoadEvent.postValue(GameEvent.RELOADED)
                } else {
                    // This will happen if on very first game, or if no saved game (due to an error).
                    showOnboardingMessage = true
                    setupNewGame()
                }
            }
        }
    }

    /**
     * Clears current game in preparation for a new one.
     */
    fun clearGame() {
        selectNewGameWord(null)
        currentGameWords = emptyList()
        for (row in 0 until gridHeight) {
            cellGrid[row].fill(null)
        }
    }

    /**
     * Adds a delta of seconds to the last saved/retrieved elapsed time of the game and persists it.
     */
    fun addToElapsedSeconds(seconds: Long) {
        elapsedSecondsSnapshot += seconds
        gamePersistence.elapsedSeconds = elapsedSecondsSnapshot
    }

    /**
     * Persists user's changes to their entries within a game word.
     */
    fun persistUserEntry(gameWord: GameWord) {
        gamePersistence.persistUserEntry(gameWord)
    }

    /**
     * Adds the current set of game words to the user's game statistics for the stats heat map. This is called when
     * a game is completed.
     */
    fun persistGameStatistics() {
        gamePersistence.persistGameStats(currentGameWords)
    }

    /**
     * Selects next incomplete or errored game word depending on parameter. Starts just after currently selected word,
     * searches to the end, then wraps around to the beginning.
     *
     * @param selectWordWithBlanks true if next incomplete game word should be selected, false if any errored game word should be selected.
     * @return true if word found and selected.
     */
    fun selectNextGameWordWithWrapAround(selectWordWithBlanks: Boolean): Boolean {
        val wordFoundAfterPosition = currentGameWord.value?.let {
            selectNextGameWord(startingRow = it.row, startingCol = it.col, havingEmptyCells = selectWordWithBlanks)
        } ?: false
        // If word not found after position, wrap around to beginning and look for word from start.
        return wordFoundAfterPosition || selectNextGameWord(startingRow = 0, startingCol = 0, havingEmptyCells = selectWordWithBlanks)
    }

    /**
     * Searches for and selects next game word with empty or errored cells depending on parameter, starting at specified cell.
     *
     * @param startingRow starting row to start searching for next word to select.
     * @param startingCol starting col to start searching for next word to select.
     * @param havingEmptyCells true if next game word having empty cells should be selected, false if any word with
     *   empty or errored cells should be selected.
     * @return true if new word found and selected, false if no selection made.
     */
    fun selectNextGameWord(startingRow: Int, startingCol: Int, havingEmptyCells: Boolean): Boolean {
        // If current word is vertical and starts on starting cell, do NOT select the horizontal word starting on that cell
        // or we'll end up circularly going back and forth between the vertical and horizontal on that cell.
        val currentWordIsVerticalAndStartsOnStartingPosition = currentGameWord.value?.let {
            !it.isAcross && it.row == startingRow && it.col == startingCol
        } ?: false

        // This variable will be set back to zero after we're done searching the starting row
        var initialCol = if (currentWordIsVerticalAndStartsOnStartingPosition) startingCol + 1 else startingCol

        // Iterate from starting cell, left to right, and down to bottom
        for (row in startingRow until gridHeight) {

            // For each column from initial of the starting row to the end, then from 0 to end for subsequent columns
            for (col in initialCol until gridWidth) {

                // If the current grid position contains a game cell
                cellGrid[row][col]?.let { cell ->

                    var nextGameWord: GameWord? = null

                    val (wordAcrossIsSelected, wordDownIsSelected) = currentGameWord.value?.let {
                        Pair(cell.gameWordAcross == it, cell.gameWordDown == it)
                    } ?: Pair(false, false)

                    // If cell is the beginning of an across word that is NOT already selected, choose it.
                    if (cell.wordAcrossStartsInCol(col) && !wordAcrossIsSelected && isIncompleteOrErroredGameWord(cell.gameWordAcross, havingEmptyCells)) {
                        nextGameWord = cell.gameWordAcross
                    } else if (cell.wordDownStartsInRow(row) && !wordDownIsSelected && isIncompleteOrErroredGameWord(cell.gameWordDown, havingEmptyCells)) {
                        // Vertical word starts in the row of this cell, select it
                        nextGameWord = cell.gameWordDown
                    }

                    // If a word was found, select it and return
                    nextGameWord?.let {
                        selectNewGameWord(it)
                        return true
                    }
                }
            }
            // for subsequent rows, start at first column
            initialCol = 0
        }
        return false
    }

    /**
     * Calculates new scroll position necessary to display the currently selected word, or null if no scrolling needed.
     */
    fun newScrollPositionShowingFullWord(currentScrollPosition: Int): Int? {
        return currentGameWord.value?.let {
            val startingRow = it.row
            val endingRow = if (it.isAcross) startingRow else startingRow + it.word.length - 1
            val rowOfSelectedCell = if (it.isAcross) startingRow else startingRow + charIndexOfSelectedCell
            screenMetrics.newScrollPositionShowingFullWord(
                    startingRow = startingRow,
                    endingRow = endingRow,
                    rowOfSelectedCell = rowOfSelectedCell,
                    currentScrollPosition = currentScrollPosition
            )
        }
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    /**
     * Must be called within a coroutine.
     */
    private suspend fun setupNewGame() {
        val gameWords = gameSetup.newGame(cellGrid, gamePersistence.currentGameOptions)
        gamePersistence.currentGameWords = gameWords
        gamePersistence.currentGameCompleted = false
        gamePersistence.elapsedSeconds = 0L
        elapsedSecondsSnapshot = 0L
        currentGameWords = gameWords
        _gameStartOrLoadEvent.postValue(GameEvent.CREATED)
    }

    private fun isIncompleteOrErroredGameWord(gameWord: GameWord?, havingEmptyCells: Boolean): Boolean {
        return gameWord?.let {
            return hasVisibleBlanks(it) || !havingEmptyCells && it.hasErroredCells
        } ?: false
    }

    /**
     * Returns true if word has cells that are visibly blank. That is, we ignore cells that are intersecting and
     * filled in in only one direction.
     */
    private fun hasVisibleBlanks(gameWord: GameWord): Boolean {
        var row = gameWord.row
        var col = gameWord.col
        for (i in gameWord.word.indices) {
            cellGrid[row][col]?.let {
                if (it.isBlank) {
                    return true
                }
            }
            if (gameWord.isAcross) col++ else row++
        }
        return false
    }

    //endregion

    //region INNER CLASSES -------------------------------------------------------------------------

    /*
     Based on Robert C. Martin's "Clean Architecture" book, have all dependencies be "pluggable" via interfaces.
     */

    interface ScreenMetrics {
        val keyboardHeight: Float
        val gridHeight: Int
        val gridWidth: Int

        /**
         * Returns new scroll position to maximize position a game word, and especially the selected character of the word.
         *
         * @param startingRow row of first character of the selected game word.
         * @param endingRow row of last character of the selected game word.
         * @param rowOfSelectedCell row of the selected character within the selected game word.
         * @param currentScrollPosition current scroll position of the game.
         */
        fun newScrollPositionShowingFullWord(startingRow: Int, endingRow: Int, rowOfSelectedCell: Int, currentScrollPosition: Int): Int?
    }

    interface GameSetup {
        fun newGame(cellGrid: Array<Array<GridCell?>>, gameOptions: Map<String, Boolean>): List<GameWord>
        fun addToGrid(gameWords: List<GameWord>, cellGrid: Array<Array<GridCell?>>)
        fun doWordsFitInGrid(gameWords: List<GameWord>, gridWidth: Int, gridHeight: Int): Boolean
    }

    interface GamePersistence {
        var currentGameWords: List<GameWord>
        var currentGameCompleted: Boolean
        var currentImageIndex: Int
        var elapsedSeconds: Long
        var currentGameOptions: Map<String, Boolean>
        val allGameStats: Map<Int, Int>

        fun persistUserEntry(gameWord: GameWord)
        fun persistGameStats(gameWords: List<GameWord>)
    }

    enum class GameEvent {
        CREATED,
        RELOADED
    }

    //endregion
}

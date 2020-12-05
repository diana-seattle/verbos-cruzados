package org.indiv.dls.games.verboscruzados

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.indiv.dls.games.verboscruzados.game.GameWord
import kotlin.math.roundToInt


class MainActivityViewModel(
        private val screenMetrics: ScreenMetrics,
        private val gameSetup: GameSetup,
        private val gamePersistence: GamePersistence
) : ViewModel() {

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    // Currently selected word in a game. Allows callers to read the current value from the immutable reference.
    private val _currentGameWord = MutableLiveData<GameWord?>()

    // Event representing when a game is loaded or started.
    private val _gameStartOrLoadEvent: MutableLiveData<GameEvent> by lazy {
        MutableLiveData<GameEvent>()
    }

    //endregion

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    // Public immutable LiveData accessors
    val currentGameWord = _currentGameWord as LiveData<GameWord?>
    val gameStartOrLoadEvent = _gameStartOrLoadEvent as LiveData<GameEvent>

    // List of word data for the current game
    var currentGameWords: List<GameWord> = emptyList()

    // The character index within the selected word of the selected cell.
    var charIndexOfSelectedCell = 0

    // Set to true when saved data indicates initial installation
    var showOnboardingMessage = false

    // This mode shows all empty and errored cells in red. It also auto-advances to the next incomplete word once a word
    // is completed correctly.
    var showingErrors = false

    // Grid of cells making up the puzzle, plus some dimensions
    val cellGrid: Array<Array<GridCell?>>
    val keyboardHeight: Float = screenMetrics.keyboardHeight
    val gridHeight: Int = screenMetrics.gridHeight
    val gridWidth: Int = screenMetrics.gridWidth

    var currentImageIndex: Int
        get() = gamePersistence.currentImageIndex
        set(value) {
            gamePersistence.currentImageIndex = value
        }

    var currentGameCompleted: Boolean
        get() = gamePersistence.currentGameCompleted
        set(value) {
            gamePersistence.currentGameCompleted = value
        }

    var elapsedSecondsSnapshot = 0L // Last set or retrieved elapsed seconds value
    val persistedElapsedSeconds: Long
        get() {
            elapsedSecondsSnapshot = gamePersistence.elapsedSeconds
            return elapsedSecondsSnapshot
        }

    //endregion

    //region INITIALIZER ---------------------------------------------------------------------------

    init {
        cellGrid = Array(screenMetrics.gridHeight) { arrayOfNulls(screenMetrics.gridWidth) }
    }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    /**
     * Allows callers on the main thread to update the current game word together with the char index of the selected cell.
     */
    fun selectNewGameWord(gameWord: GameWord?, charIndexOfSelectedCell: Int) {
        // Set the index first so that it's available to observers of the game word change.
        this.charIndexOfSelectedCell = charIndexOfSelectedCell
        _currentGameWord.value = gameWord
    }

    /**
     * Launches new game, setting up on a worker thread.
     */
    fun launchNewGame() {
        viewModelScope.launch(context = Dispatchers.Default) {
            setupNewGame()
        }
    }

    /**
     * Loads existing game on an IO thread, starting new game if none found.
     */
    fun loadGame() {
        // On app startup, the word list will be empty, but on a config change it likely won't be so no need to re-load.
        if (currentGameWords.isEmpty()) {
            viewModelScope.launch(context = Dispatchers.IO) {
                val gameWords = gamePersistence.currentGameWords
                if (gameWords.isNotEmpty() && gameSetup.doWordsFitInGrid(gameWords, gridWidth, gridHeight)) {
                    gameSetup.addToGrid(gameWords, cellGrid)
                    currentGameWords = gameWords
                    _gameStartOrLoadEvent.postValue(GameEvent.RELOADED)
                } else {
                    // This will happen if on very first game, or if no saved game (due to an error).
                    showOnboardingMessage = true
                    setupNewGame()
                }
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
        gamePersistence.elapsedSeconds = elapsedSecondsSnapshot
    }

    fun persistUserEntry(gameWord: GameWord) {
        viewModelScope.launch(context = Dispatchers.IO) {
            gamePersistence.persistUserEntry(gameWord)
        }
    }

    fun persistGameStatistics() {
        viewModelScope.launch(context = Dispatchers.IO) {
            gamePersistence.persistGameStats(currentGameWords)
        }
    }

    /**
     * Selects next empty or errored game word depending on parameter. Starts with current game word,
     * searches to the end, then wraps around to the beginning.
     *
     * @param shouldSelectEmptyOnly true if next empty game word should be selected, false if any errored game word should be selected.
     * @return true if word found and selected.
     */
    fun selectNextGameWordWithWrapAround(shouldSelectEmptyOnly: Boolean): Boolean {
        val wordFoundAfterPosition = currentGameWord.value?.let {
            selectNextGameWord(startingRow = it.row, startingCol = it.col, havingEmptyCells = shouldSelectEmptyOnly)
        } ?: false
        // If word not found after position, wrap around to beginning and look for word from start.
        return wordFoundAfterPosition || selectNextGameWord(startingRow = 0, startingCol = 0, havingEmptyCells = shouldSelectEmptyOnly)
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
                    if (cell.wordAcrossStartsInCol(col) && !wordAcrossIsSelected && isEmptyOrErroredGameWord(cell.gameWordAcross, havingEmptyCells)) {
                        nextGameWord = cell.gameWordAcross
                    } else if (cell.wordDownStartsInRow(row) && !wordDownIsSelected && isEmptyOrErroredGameWord(cell.gameWordDown, havingEmptyCells)) {
                        // Vertical word starts in the row of this cell, select it
                        nextGameWord = cell.gameWordDown
                    }

                    // If a word was found, select it and return
                    nextGameWord?.let {
                        selectNewGameWord(it, it.defaultSelectionIndex)
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
        currentGameWord.value?.let {
            val firstRowPosition = it.row
            val lastRowPosition = when {
                it.isAcross -> it.row
                else -> it.row + it.word.length - 1
            }
            val yOfFirstCell = firstRowPosition * screenMetrics.pixelsPerCell

            val availableHeight = screenMetrics.viewablePuzzleHeight - keyboardHeight
            val wordHeight = (lastRowPosition - firstRowPosition + 1) * screenMetrics.pixelsPerCell

            // if there's room to display the whole word
            if (wordHeight < availableHeight) {
                // if first cell is above visible area, scroll up to it, or if last cell is below visible area, scroll down to it
                if (yOfFirstCell < currentScrollPosition) {
                    return (yOfFirstCell - screenMetrics.puzzleMarginTopPixels).roundToInt()
                } else if (yOfFirstCell + wordHeight > currentScrollPosition + availableHeight) {
                    return (yOfFirstCell + wordHeight - availableHeight + screenMetrics.puzzleMarginTopPixels).roundToInt()
                }
            } else {
                // There is not room for the whole word vertically, so make sure the selected cell is at least visible.
                // (This scenario should only happen with a vertical word.)
                val rowOfSelectedCell = it.row + charIndexOfSelectedCell
                val yOfSelectedCell = rowOfSelectedCell * screenMetrics.pixelsPerCell
                if (yOfSelectedCell < currentScrollPosition) {
                    // scroll top of cell to top of viewable area
                    return (yOfSelectedCell - screenMetrics.puzzleMarginTopPixels).roundToInt()
                } else if (yOfSelectedCell + screenMetrics.pixelsPerCell > currentScrollPosition + availableHeight) {
                    // scroll bottom of cell to bottom of viewable area
                    return (yOfSelectedCell + screenMetrics.pixelsPerCell - availableHeight + screenMetrics.puzzleMarginTopPixels).roundToInt()
                }
            }
        }
        return null
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

    private fun isEmptyOrErroredGameWord(gameWord: GameWord?, havingEmptyCells: Boolean): Boolean {
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

    interface ScreenMetrics {
        val keyboardHeight: Float
        val puzzleMarginTopPixels: Float
        val viewablePuzzleHeight: Float
        val pixelsPerCell: Float
        val gridHeight: Int
        val gridWidth: Int
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

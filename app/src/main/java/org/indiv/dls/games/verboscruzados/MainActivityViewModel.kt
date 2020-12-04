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
import kotlin.math.roundToInt


class MainActivityViewModel(
        private val resources: Resources,
        theme: Resources.Theme,
        private val persistenceHelper: PersistenceHelper,
        private val gameSetup: GameSetup
) : ViewModel() {

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    // Currently selected word in a game. Allows callers to read the current value from the immutable reference.
    private val _currentGameWord = MutableLiveData<GameWord?>()

    // Event representing when a game is loaded or started.
    private val _gameStartOrLoadEvent: MutableLiveData<GameEvent> by lazy {
        MutableLiveData<GameEvent>()
    }

    private val viewablePuzzleHeight: Float
    private val puzzleMarginTopPixels: Float
    private val pixelsPerCell: Float

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

    // Grid of cells making up the puzzle, plus some dimensions
    val cellGrid: Array<Array<GridCell?>>
    val keyboardHeight: Float
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
        keyboardHeight = resources.getDimension(R.dimen.keyboard_height)
        val displayMetrics = resources.displayMetrics
        val configuration = resources.configuration
        puzzleMarginTopPixels = resources.getDimension(R.dimen.puzzle_margin_top)
        val puzzleMarginSidePixels = resources.getDimension(R.dimen.puzzle_margin_side)
        val totalPuzzleMarginTopPixels = puzzleMarginTopPixels * 2
        val totalPuzzleMarginSidePixels = puzzleMarginSidePixels * 2
        val actionBarHeightPixels = getActionBarHeightInPixels(displayMetrics, theme)
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
                val gameWords = persistenceHelper.currentGameWords
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
        persistenceHelper.elapsedSeconds = elapsedSecondsSnapshot
    }

    fun persistUserEntry(gameWord: GameWord) {
        viewModelScope.launch(context = Dispatchers.IO) {
            persistenceHelper.persistUserEntry(gameWord)
        }
    }

    fun persistGameStatistics() {
        viewModelScope.launch(context = Dispatchers.IO) {
            persistenceHelper.persistGameStats(currentGameWords)
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
            val yOfFirstCell = firstRowPosition * pixelsPerCell

            val availableHeight = viewablePuzzleHeight - keyboardHeight
            val wordHeight = (lastRowPosition - firstRowPosition + 1) * pixelsPerCell

            // if there's room to display the whole word
            if (wordHeight < availableHeight) {
                // if first cell is above visible area, scroll up to it, or if last cell is below visible area, scroll down to it
                if (yOfFirstCell < currentScrollPosition) {
                    return (yOfFirstCell - puzzleMarginTopPixels).roundToInt()
                } else if (yOfFirstCell + wordHeight > currentScrollPosition + availableHeight) {
                    return (yOfFirstCell + wordHeight - availableHeight + puzzleMarginTopPixels).roundToInt()
                }
            } else {
                // There is not room for the whole word vertically, so make sure the selected cell is at least visible.
                // (This scenario should only happen with a vertical word.)
                val rowOfSelectedCell = it.row + charIndexOfSelectedCell
                val yOfSelectedCell = rowOfSelectedCell * pixelsPerCell
                if (yOfSelectedCell < currentScrollPosition) {
                    // scroll top of cell to top of viewable area
                    return (yOfSelectedCell - puzzleMarginTopPixels).roundToInt()
                } else if (yOfSelectedCell + pixelsPerCell > currentScrollPosition + availableHeight) {
                    // scroll bottom of cell to bottom of viewable area
                    return (yOfSelectedCell + pixelsPerCell - availableHeight + puzzleMarginTopPixels).roundToInt()
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
        val gameWords = gameSetup.newGame(resources, cellGrid, persistenceHelper.currentGameOptions)
        persistenceHelper.currentGameWords = gameWords
        persistenceHelper.currentGameCompleted = false
        persistenceHelper.elapsedSeconds = 0L
        elapsedSecondsSnapshot = 0L
        currentGameWords = gameWords
        _gameStartOrLoadEvent.postValue(GameEvent.CREATED)
    }

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

    enum class GameEvent {
        CREATED,
        RELOADED
    }

    /**
     * Factory class for creating this view model.
     */
    class Factory(private val activity: Activity) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
                return MainActivityViewModel(
                        activity.resources,
                        activity.theme,
                        PersistenceHelper(activity),
                        GameSetup()
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    //endregion
}

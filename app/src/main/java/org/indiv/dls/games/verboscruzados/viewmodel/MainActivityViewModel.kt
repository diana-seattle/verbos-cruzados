package org.indiv.dls.games.verboscruzados.viewmodel

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.indiv.dls.games.verboscruzados.model.AnswerPresentation
import org.indiv.dls.games.verboscruzados.model.GameWord
import org.indiv.dls.games.verboscruzados.model.GridCell
import org.indiv.dls.games.verboscruzados.model.PuzzleWordPresentation

/**
 * ViewModel class for the Main activity based on Google's architectural components: https://developer.android.com/jetpack/guide
 */
class MainActivityViewModel(
        private val screenMetrics: ScreenMetrics,
        private val gameSetup: GameSetup,
        private val gamePersistence: GamePersistence,
        private val gameWordConversions: GameWordConversions
) : ViewModel() {

    //region INTERFACES ----------------------------------------------------------------------------

    /*
     Based on Robert C. Martin's "Clean Architecture" book, dependencies are "pluggable" via interfaces.
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

    interface GameWordConversions {
        fun toAnswerPresentation(gameWord: GameWord): AnswerPresentation
        fun toPuzzleWordPresentation(gameWord: GameWord): PuzzleWordPresentation
    }

    //endregion

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        private const val SECONDS_PER_MINUTE = 60
    }

    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    // Private mutable LiveData representing currently selected word in a game.
    private val currentGameWord = MutableLiveData<GameWord?>()

    // Private mutable LiveData representing event when a game is loaded or started.
    @VisibleForTesting val _gameStartOrLoadEvent: MutableLiveData<GameEvent> by lazy {
        MutableLiveData<GameEvent>()
    }

    // Game words for the current game, mapping of id to game word.
    @VisibleForTesting var gameWordMap: Map<String, GameWord> = emptyMap()

    //endregion

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    // Public immutable LiveData representing currently selected word in a game.
    val answerPresentation: LiveData<AnswerPresentation?> = Transformations.map(currentGameWord) { gameWord ->
        gameWord?.let { gameWordConversions.toAnswerPresentation(it) }
    }
    val selectedPuzzleWord: LiveData<PuzzleWordPresentation?> = Transformations.map(currentGameWord) { gameWord ->
        gameWord?.let { gameWordConversions.toPuzzleWordPresentation(it) }
    }

    // Public immutable LiveData representing event when a game is loaded or started.
    val gameStartOrLoadEvent = _gameStartOrLoadEvent as LiveData<GameEvent>

    // The character index within the selected word of the selected cell.
    var charIndexOfSelectedCell = 0

    // Set to true when saved data indicates initial installation of the game, whereby the user needs initial instruction.
    var showOnboardingMessage = false

    // This mode displays all empty and errored cells in red. It also auto-advances to the next incomplete word once a
    // word is completed correctly.
    var showingErrors = false

    // Grid of cells making up the puzzle and transparent puzzle background.
    val cellGrid: Array<Array<GridCell?>>

    val wordCount: Int
        get() = gameWordMap.size

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
    fun selectNewGameWord(gameWordId: String?, charIndexOfSelectedCell: Int? = null) {
        val gameWord = gameWordId?.let { gameWordMap[it] }

        // Set the index first so that it's available to observers of the game word change.
        this.charIndexOfSelectedCell = charIndexOfSelectedCell ?: gameWord?.defaultSelectionIndex ?: 0

        // Update the LiveData value to publish to observers.
        currentGameWord.value = gameWord
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
        if (gameWordMap.isEmpty()) {
            // Do with coroutine on worker thread
            viewModelScope.launch(context = Dispatchers.Default) {
                val gameWords = gamePersistence.currentGameWords
                if (gameWords.isNotEmpty() && gameSetup.doWordsFitInGrid(gameWords, gridWidth, gridHeight)) {
                    gameSetup.addToGrid(gameWords, cellGrid)
                    createGameWordMap(gameWords)
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
        gameWordMap = emptyMap()
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
     * Persists user's change to the full text of their entry for a game word.
     */
    fun updateTextOfSelectedWord(userText: String) {
        currentGameWord.value?.let { gameWord ->
            gameWord.setUserText(userText.take(gameWord.answer.length))
            charIndexOfSelectedCell = gameWord.defaultSelectionIndex
            gamePersistence.persistUserEntry(gameWord)

            // Update cell grid
            var row = gameWord.startingRow
            var col = gameWord.startingCol
            for (charIndex in 0 until gameWord.answer.length) {
                cellGrid[row][col]?.let { cell ->
                    val userChar = gameWord.userEntry[charIndex]
                    if (gameWord.isAcross) {
                        cell.userCharAcross = userChar
                        col++
                    } else {
                        cell.userCharDown = userChar
                        row++
                    }
                }
            }
        }
    }

    /**
     * Updates selected cell with user's change and persists.
     */
    fun updateCharOfSelectedCell(userChar: Char?) {
        val charValue = userChar ?: GameWord.BLANK
        currentGameWord.value?.let { gameWord ->
            // Update letter in currently selected game word
            gameWord.userEntry[charIndexOfSelectedCell] = charValue
            gamePersistence.persistUserEntry(gameWord)

            // Update values in cell grid, and also update intersecting word if present
            if (gameWord.isAcross) {
                val cellRow = gameWord.startingRow
                val cellCol = gameWord.startingCol + charIndexOfSelectedCell
                cellGrid[cellRow][cellCol]?.let { cell ->
                    cell.userCharAcross = charValue

                    // Look for intersecting vertical word on the same cell and update it as well.
                    val inConflict = cell.userCharDown != GameWord.BLANK && cell.userCharDown != charValue
                    if (inConflict) {
                        cell.userCharDown = charValue
                        cell.gameWordIdDown?.let { id ->
                            gameWordMap[id]?.let { intersectingWord ->
                                intersectingWord.userEntry[cell.downCharIndex] = charValue
                                gamePersistence.persistUserEntry(intersectingWord)
                            }
                        }
                    }
                }
            } else {
                val cellRow = gameWord.startingRow + charIndexOfSelectedCell
                val cellCol = gameWord.startingCol
                cellGrid[cellRow][cellCol]?.let { cell ->
                    cell.userCharDown = charValue

                    // Look for intersecting horizontal word on the same cell and update it as well.
                    val inConflict = cell.userCharAcross != GameWord.BLANK && cell.userCharAcross != charValue
                    if (inConflict) {
                        cell.userCharAcross = charValue
                        cell.gameWordIdAcross?.let { id ->
                            gameWordMap[id]?.let { intersectingWord ->
                                intersectingWord.userEntry[cell.acrossCharIndex] = charValue
                                gamePersistence.persistUserEntry(intersectingWord)
                            }
                        }
                    }
                }
            }
        }
    }

    fun advanceSelectedCellWithinWord(inBackwardDirection: Boolean) {
        currentGameWord.value?.let {
            charIndexOfSelectedCell = if (inBackwardDirection) {
                (charIndexOfSelectedCell - 1).coerceAtLeast(0)
            } else {
                (charIndexOfSelectedCell + 1).coerceAtMost(it.answer.length - 1)
            }
        }
    }

    /**
     * Determines if puzzle is completely filled in.
     *
     * @param correctly true if puzzle considered complete when everything filled in correctly, false if puzzle
     * considered complete when everything filled in regardless of correctness.
     * @return true if puzzle is completely filled in.
     */
    fun isPuzzleComplete(correctly: Boolean): Boolean {
        for (row in 0 until gridHeight) {
            for (col in 0 until gridWidth) {
                val gridCell = cellGrid[row][col]
                if (gridCell != null) {
                    // if cell is empty, then not complete
                    if (gridCell.userChar == GameWord.BLANK || correctly && gridCell.hasUserError) {
                        return false
                    }
                }
            }
        }
        return true
    }

    fun isCurrentGameWordAnsweredCompletelyAndCorrectly(): Boolean {
        return currentGameWord.value?.isAnsweredCompletelyAndCorrectly ?: false
    }

    /**
     * Calculates completion rate in words per minute.
     */
    fun calculateCompletionRate(): Float {
        return (wordCount * SECONDS_PER_MINUTE).toFloat() /
                elapsedSecondsSnapshot.toFloat().coerceAtLeast(.1f)
    }

    /**
     * Formats elapsed time for display.
     */
    fun getElapsedTimeText(elapsedMs: Long): String {
        val minutes = elapsedMs / 60L
        val seconds = elapsedMs % 60L
        return "%d:%02d".format(minutes, seconds)
    }

    /**
     * Adds the current set of game words to the user's game statistics for the stats heat map. This is called when
     * a game is completed.
     */
    fun persistGameStatistics() {
        gamePersistence.persistGameStats(gameWordMap.values.toList())
    }

    /**
     * Selects next game word with a preference for one that has incomplete cells.
     */
    fun selectNextGameWordFavoringIncomplete(): Boolean {
        return selectNextGameWordWithWrapAround(selectWordWithBlanks = true)
                || selectNextGameWordWithWrapAround(selectWordWithBlanks = false)
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
            selectNextGameWord(startingRow = it.startingRow, startingCol = it.startingCol, havingEmptyCells = selectWordWithBlanks)
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
        // or we'll end up circularly going back and forth between the vertical and horizontal on that cell. Unless that's
        // what we want because there are only 2 words in the game.
        val currentWordIsVerticalAndStartsOnStartingPosition = wordCount > 2 && currentGameWord.value?.let {
            !it.isAcross && it.startingRow == startingRow && it.startingCol == startingCol
        } ?: false

        // This variable will be set back to zero after we're done searching the starting row
        var initialCol = if (currentWordIsVerticalAndStartsOnStartingPosition) startingCol + 1 else startingCol

        // Iterate from starting cell, left to right, and down to bottom
        for (row in startingRow until gridHeight) {

            // For each column from initial of the starting row to the end, then from 0 to end for subsequent columns
            for (col in initialCol until gridWidth) {

                // If the current grid position contains a game cell
                cellGrid[row][col]?.let { cell ->

                    var nextGameWordId: String? = null

                    val (wordAcrossIsSelected, wordDownIsSelected) = currentGameWord.value?.let {
                        Pair(cell.gameWordIdAcross == it.id, cell.gameWordIdDown == it.id)
                    } ?: Pair(false, false)

                    // If cell is the beginning of an across word that is NOT already selected, choose it.
                    if (acrossWordOfCellStartsInCol(cell, col) && !wordAcrossIsSelected && isIncompleteOrErroredGameWord(cell.gameWordIdAcross, havingEmptyCells)) {
                        nextGameWordId = cell.gameWordIdAcross
                    } else if (downWordOfCellStartsInRow(cell, row) && !wordDownIsSelected && isIncompleteOrErroredGameWord(cell.gameWordIdDown, havingEmptyCells)) {
                        // Vertical word starts in the row of this cell, select it
                        nextGameWordId = cell.gameWordIdDown
                    }

                    // If a word was found, select it and return
                    nextGameWordId?.let {
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
            val startingRow = it.startingRow
            val endingRow = if (it.isAcross) startingRow else startingRow + it.answer.length - 1
            val rowOfSelectedCell = if (it.isAcross) startingRow else startingRow + charIndexOfSelectedCell
            screenMetrics.newScrollPositionShowingFullWord(
                    startingRow = startingRow,
                    endingRow = endingRow,
                    rowOfSelectedCell = rowOfSelectedCell,
                    currentScrollPosition = currentScrollPosition
            )?.coerceAtLeast(0)
        }
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    private fun acrossWordOfCellStartsInCol(cell: GridCell, col: Int): Boolean {
        return cell.gameWordIdAcross?.let { id ->
            gameWordMap[id]?.startingCol == col
        } ?: false
    }

    private fun downWordOfCellStartsInRow(cell: GridCell, row: Int): Boolean {
        return cell.gameWordIdDown?.let { id ->
            gameWordMap[id]?.startingRow == row
        } ?: false
    }

    /**
     * Must be called within a coroutine.
     */
    private suspend fun setupNewGame() {
        val gameWords = gameSetup.newGame(cellGrid, gamePersistence.currentGameOptions)
        gamePersistence.currentGameWords = gameWords
        gamePersistence.currentGameCompleted = false
        gamePersistence.elapsedSeconds = 0L
        elapsedSecondsSnapshot = 0L
        createGameWordMap(gameWords)
        _gameStartOrLoadEvent.postValue(GameEvent.CREATED)
    }

    private fun isIncompleteOrErroredGameWord(gameWordId: String?, havingEmptyCells: Boolean): Boolean {
        return gameWordMap[gameWordId]?.let {
            return hasVisibleBlanks(it) || !havingEmptyCells && it.hasErroredCells
        } ?: false
    }

    /**
     * Returns true if word has cells that are visibly blank. That is, we ignore cells that are intersecting and
     * filled in in only one direction.
     */
    private fun hasVisibleBlanks(gameWord: GameWord): Boolean {
        var row = gameWord.startingRow
        var col = gameWord.startingCol
        for (i in gameWord.answer.indices) {
            cellGrid[row][col]?.let {
                if (it.isBlank) {
                    return true
                }
            }
            if (gameWord.isAcross) col++ else row++
        }
        return false
    }

    private fun createGameWordMap(gameWords: List<GameWord>) {
        gameWordMap = gameWords.associateBy { it.id }
    }

    //endregion

    //region INNER CLASSES -------------------------------------------------------------------------

    enum class GameEvent {
        CREATED,
        RELOADED
    }

    //endregion
}

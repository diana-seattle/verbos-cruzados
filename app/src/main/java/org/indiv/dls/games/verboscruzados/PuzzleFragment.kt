package org.indiv.dls.games.verboscruzados


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TableRow
import androidx.lifecycle.ViewModelProvider
import org.indiv.dls.games.verboscruzados.component.PuzzleCellTextView
import org.indiv.dls.games.verboscruzados.databinding.FragmentPuzzleBinding
import org.indiv.dls.games.verboscruzados.game.GameWord


/**
 * Fragment containing the crossword puzzle.
 */
class PuzzleFragment : Fragment() {

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    // binding is only valid between onCreateView and onDestroyView
    private var _binding: FragmentPuzzleBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MainActivityViewModel

    private lateinit var vibration: Vibration

    private var gameWordLastSelected: GameWord? = null

    // Used to find a view based on a position value
    private var viewByPositionMap: MutableMap<Position, PuzzleCellTextView> = mutableMapOf()

    // Used to find the position of a view
    private var positionOfViewMap: MutableMap<PuzzleCellTextView, Position> = mutableMapOf()

    private val onPuzzleClickListener = OnClickListener { v ->
        getCellForView(v)?.let { gridCell ->
            vibration.vibrate()

            // If clicked-on cell is part of the already selected word, let it remain the selected word, otherwise
            // choose the vertical word if exists, otherwise the horizontal word.
            val newGameWord = viewModel.currentGameWord.value?.takeIf {
                it == gridCell.gameWordDown || it == gridCell.gameWordAcross
            } ?: gridCell.gameWordDown ?: gridCell.gameWordAcross

            newGameWord?.let {
                val charIndexOfSelectedCell = if (it.isAcross) gridCell.acrossCharIndex else gridCell.downCharIndex

                // This will cause us to be notified, which will take care of showing word and cell as selected
                viewModel.selectNewGameWord(newGameWord, charIndexOfSelectedCell)
            }
        }
    }

    //endregion

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    var scrollPosition: Int
        get() = binding.root.scrollY
        set(value) {
            binding.root.smoothScrollTo(0, value)
        }

    //endregion

    //region OVERRIDDEN FUNCTIONS ------------------------------------------------------------------

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentPuzzleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity(), MainActivityViewModel.Factory(requireActivity()))
                .get(MainActivityViewModel::class.java)

        viewModel.currentGameWord.observe(viewLifecycleOwner) { gameWord ->
            // deselect word that currently has selection, if any
            gameWordLastSelected?.let { showAsSelected(it, false) }

            // select new word if any
            gameWord?.let { showAsSelected(it, true) }

            gameWordLastSelected = gameWord
        }

        // Get instance of Vibrator from current Context
        context?.let {
            vibration = Vibration(it)
        }
    }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    fun initialize() {
        // create table rows
        for (row in 0 until viewModel.gridHeight) {
            val tableRow = TableRow(activity)
            tableRow.gravity = Gravity.CENTER
            binding.cellTableLayout.addView(tableRow)
        }
    }

    fun doWordsFitInGrid(gameWords: List<GameWord>): Boolean {
        gameWords.forEach {
            if ((it.row >= viewModel.gridHeight || it.col >= viewModel.gridWidth) ||
                    (it.isAcross && it.col + it.word.length > viewModel.gridWidth) ||
                    (!it.isAcross && it.row + it.word.length > viewModel.gridHeight)) {
                return false
            }
        }
        return true
    }

    fun clearExistingGame() {
        // clear out any existing data
        for (row in 0 until viewModel.gridHeight) {
            val tableRow = binding.cellTableLayout.getChildAt(row) as TableRow
            tableRow.removeAllViews()
        }
    }

    /**
     * Creates grid of textviews making up the puzzle.
     */
    fun createGridViewsAndSelectWord() {

        // add views into table rows and columns
        val activityContext = requireContext()
        var firstGameWord: GameWord? = null
        for (row in 0 until viewModel.gridHeight) {
            val tableRow = binding.cellTableLayout.getChildAt(row) as TableRow
            tableRow.removeAllViews()
            for (col in 0 until viewModel.gridWidth) {
                viewModel.cellGrid[row][col]?.let {
                    // create text view for this row and column
                    val textView = PuzzleCellTextView(activityContext)
                    textView.setOnClickListener(onPuzzleClickListener)
                    tableRow.addView(textView, col)

                    val position = Position(row, col)
                    viewByPositionMap[position] = textView
                    positionOfViewMap[textView] = position

                    fillTextView(it, textView)

                    // set current game word to the first across word found
                    if (firstGameWord == null) {
                        firstGameWord = it.gameWordAcross ?: it.gameWordDown
                    }
                } ?: run {
                    tableRow.addView(View(activityContext), col)
                }
            }
        }

        // Make the initial word selection, in this priority:
        // 1. Select first empty word if any
        // 2. Select first errored word if any
        // 3. Select first word (this can happen if user returns to a finished game)
        if (!selectNextGameWord(0, 0, true)
                && !selectNextGameWord(0, 0, false)) {
            viewModel.selectNewGameWord(firstGameWord, firstGameWord?.defaultSelectionIndex ?: 0)
        }
    }

    /**
     * Selects next empty or errored game word depending on parameter. Starts with current game word,
     * searches to the end, then wraps around to the beginning.
     *
     * @param shouldSelectEmptyOnly true if next empty game word should be selected, false if any errored game word should be selected.
     * @return true if word found and selected.
     */
    fun selectNextGameWordAfterCurrent(shouldSelectEmptyOnly: Boolean): Boolean {
        val wordFoundAfterPosition = viewModel.currentGameWord.value?.let {
            selectNextGameWord(startingRow = it.row, startingCol = it.col, emptyOnly = shouldSelectEmptyOnly)
        } ?: false
        // If word not found after position, wrap around to beginning and look for word from start.
        return wordFoundAfterPosition || selectNextGameWord(startingRow = 0, startingCol = 0, emptyOnly = shouldSelectEmptyOnly)
    }

    /**
     * Indicates errored cells in the puzzle with a reddish background.
     *
     * @param showErrors true if errors should be indicated, false if not.
     */
    fun showErrors(showErrors: Boolean) {
        // update background of cells based on whether text is correct or not, and whether selected or not.
        for (row in 0 until viewModel.gridHeight) {
            for (col in 0 until viewModel.gridWidth) {
                val position = Position(row, col)

                // if cell is part of currently selected game word, adjust the level to set the background color
                viewModel.cellGrid[row][col]?.let { cell ->
                    val isSelected = viewModel.currentGameWord.value?.let {
                        it == cell.gameWordAcross || it == cell.gameWordDown
                    } ?: false

                    viewByPositionMap[position]?.setStyle(isSelected, showErrors && cell.hasUserError)
                }
            }
        }
    }

    /**
     * @param correctly true if puzzle considered complete when everything filled in correctly, false if puzzle
     * considered complete when everything filled in regardless of correctness.
     * @return true if puzzle is completely filled in.
     */
    fun isPuzzleComplete(correctly: Boolean): Boolean {
        for (row in 0 until viewModel.gridHeight) {
            for (col in 0 until viewModel.gridWidth) {
                val gridCell = viewModel.cellGrid[row][col]
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

    /**
     * Fills in the entire word with the specified text.
     */
    fun updateTextInPuzzleWord(userText: String) {
        viewModel.currentGameWord.value?.let {
            it.setUserText(userText.take(it.word.length))
            updateUserEntryInPuzzle(it)
            viewModel.charIndexOfSelectedCell = it.defaultSelectionIndex
            showAsSelected(it, true)
        }
    }

    fun deleteLetterInPuzzle(): GameWord? {
        return updateLetterInPuzzle(GameWord.BLANK)
    }

    fun updateLetterInPuzzle(userChar: Char): GameWord? {
        viewModel.currentGameWord.value?.let {
            it.userEntry[viewModel.charIndexOfSelectedCell] = userChar
            val row = if (it.isAcross) it.row else it.row + viewModel.charIndexOfSelectedCell
            val col = if (it.isAcross) it.col + viewModel.charIndexOfSelectedCell else it.col
            return updateUserLetterInPuzzle(userChar, it.isAcross, row, col)
        }
        return null
    }

    fun advanceSelectedCellInPuzzle(backwardDirection: Boolean) {
        viewModel.currentGameWord.value?.let {
            viewModel.charIndexOfSelectedCell = if (backwardDirection) {
                (viewModel.charIndexOfSelectedCell - 1).coerceAtLeast(0)
            } else {
                (viewModel.charIndexOfSelectedCell + 1).coerceAtMost(it.word.length - 1)
            }
            showAsSelected(it, true)
        }
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    /**
     * Fills in the puzzle with the user's answer for the specified game word.
     */
    private fun updateUserEntryInPuzzle(gameWord: GameWord) {
        // show answer in puzzle
        val userEntry = gameWord.userEntry
        val wordLength = gameWord.word.length
        val isAcross = gameWord.isAcross
        var row = gameWord.row
        var col = gameWord.col
        for (charIndex in 0 until wordLength) {
            val position = Position(row, col)
            viewModel.cellGrid[row][col]?.let {
                val userChar = userEntry[charIndex]
                if (isAcross) {
                    it.userCharAcross = userChar
                    col++
                } else {
                    it.userCharDown = userChar
                    row++
                }
                fillTextView(it, viewByPositionMap[position])
            }
        }
    }

    /**
     * Fills in the puzzle with the user's letter for the specified position.
     *
     * @return word in opposing direction that had conflict and was updated if any, otherwise null
     */
    private fun updateUserLetterInPuzzle(userChar: Char, isAcross: Boolean, row: Int, col: Int): GameWord? {
        var conflictingGameWord: GameWord? = null
        val position = Position(row, col)
        viewModel.cellGrid[row][col]?.let { cell ->
            if (isAcross) {
                val inConflict = cell.userCharDown != GameWord.BLANK && userChar != cell.userCharDown
                cell.userCharAcross = userChar
                if (inConflict) {
                    cell.userCharDown = userChar
                    val index = cell.downCharIndex
                    conflictingGameWord = cell.gameWordDown
                    conflictingGameWord?.let {
                        it.userEntry[index] = userChar
                    }
                }
            } else {
                val inConflict = cell.userCharAcross != GameWord.BLANK && userChar != cell.userCharAcross
                cell.userCharDown = userChar
                if (inConflict) {
                    cell.userCharAcross = userChar
                    val index = cell.acrossCharIndex
                    conflictingGameWord = cell.gameWordAcross
                    conflictingGameWord?.let {
                        it.userEntry[index] = userChar
                    }
                }
            }
            fillTextView(cell, viewByPositionMap[position])
        }
        return conflictingGameWord
    }

    /**
     * Searches for and selects next empty or errored game word depending on parameter, starting at specified cell.
     *
     * @param startingRow starting row to start searching for next word to select.
     * @param startingCol starting col to start searching for next word to select.
     * @param emptyOnly true if next empty game word should be selected, false if any errored game word should be selected.
     * @return true if new word found and selected, false if no selection made.
     */
    private fun selectNextGameWord(startingRow: Int, startingCol: Int, emptyOnly: Boolean): Boolean {
        // If current word is vertical and starts on starting cell, do NOT select the horizontal word starting on that cell
        // or we'll end up circularly going back and forth between the vertical and horizontal on that cell.
        val currentWordIsVerticalAndStartsOnStartingPosition = viewModel.currentGameWord.value?.let {
            !it.isAcross && it.row == startingRow && it.col == startingCol
        } ?: false

        // This variable will be set back to zero after we're done searching the starting row
        var initialCol = if (currentWordIsVerticalAndStartsOnStartingPosition) startingCol + 1 else startingCol

        // Iterate from starting cell, left to right, and down to bottom
        for (row in startingRow until viewModel.gridHeight) {

            // For each column from initial of the starting row to the end, then from 0 to end for subsequent columns
            for (col in initialCol until viewModel.gridWidth) {

                // If the current grid position contains a game cell
                viewModel.cellGrid[row][col]?.let { cell ->

                    var nextGameWord: GameWord? = null

                    val (wordAcrossIsSelected, wordDownIsSelected) = viewModel.currentGameWord.value?.let {
                        Pair(cell.gameWordAcross == it, cell.gameWordDown == it)
                    } ?: Pair(false, false)

                    // If cell is the beginning of an across word that is NOT already selected, choose it.
                    if (cell.wordAcrossStartsInCol(col) && !wordAcrossIsSelected && isEmptyOrErroredGameWord(cell.gameWordAcross, emptyOnly)) {
                        nextGameWord = cell.gameWordAcross
                    } else if (cell.wordDownStartsInRow(row) && !wordDownIsSelected && isEmptyOrErroredGameWord(cell.gameWordDown, emptyOnly)) {
                        // Vertical word starts in the row of this cell, select it
                        nextGameWord = cell.gameWordDown
                    }

                    // If a word was found, select it and return
                    nextGameWord?.let {
                        viewModel.selectNewGameWord(it, it.defaultSelectionIndex)
                        return true
                    }
                }
            }
            // for subsequent rows, start at first column
            initialCol = 0
        }
        return false
    }

    private fun isEmptyOrErroredGameWord(gameWord: GameWord?, emptyOnly: Boolean): Boolean {
        return gameWord?.let {
            return hasVisibleBlanks(it) || !emptyOnly && it.hasErroredCells
        } ?: false
    }

    private fun hasVisibleBlanks(gameWord: GameWord): Boolean {
        var row = gameWord.row
        var col = gameWord.col
        for (i in gameWord.word.indices) {
            viewModel.cellGrid[row][col]?.let {
                if (it.isBlank) {
                    return true
                }
            }
            if (gameWord.isAcross) {
                col++
            } else {
                row++
            }
        }
        return false
    }

    /**
     * Shows the specified word as selected (yellow highlight), or not selected.
     *
     * @param gameWord word to show as selected or unselected.
     * @param asSelected true if word should be shown as selected, false if unselected.
     */
    private fun showAsSelected(gameWord: GameWord?, asSelected: Boolean) {
        gameWord?.let {
            var row = it.row
            var col = it.col
            for (i in it.word.indices) {
                val textView = viewByPositionMap[Position(row, col)]
                if (asSelected && i == viewModel.charIndexOfSelectedCell) {
                    textView?.setIndividualSelection(true)
                } else {
                    textView?.setSelection(asSelected)
                }
                if (it.isAcross) {
                    col++
                } else {
                    row++
                }
            }
        }
    }

    /**
     * Fills puzzle textview with the character from the user's answer.
     *
     * @param gridCell the grid cell from which to get the textview and the user's answer.
     * @param textView the textview to fill.
     */
    private fun fillTextView(gridCell: GridCell, textView: PuzzleCellTextView?) {
        val text = when {
            gridCell.isConflict -> "${gridCell.userCharAcross}/${gridCell.userCharDown}"
            gridCell.isBlank -> null
            else -> gridCell.userChar.toString()
        }
        textView?.fillTextView(text)
    }

    /**
     * Gets [GridCell] cell corresponding to the specified view.
     *
     * @param v view to get [GridCell] for.
     */
    private fun getCellForView(v: View): GridCell? {
        return positionOfViewMap[v]?.let {
            viewModel.cellGrid[it.row][it.col]
        }
    }

    //endregion

    //region INNER CLASSES -------------------------------------------------------------------------

    data class Position(val row: Int, val col: Int)

    //endregion
}

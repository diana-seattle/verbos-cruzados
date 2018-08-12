package org.indiv.dls.games.verboscruzados.feature


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TableRow
import kotlinx.android.synthetic.main.fragment_puzzle.*
import org.indiv.dls.games.verboscruzados.feature.component.PuzzleCellTextView
import org.indiv.dls.games.verboscruzados.feature.game.GameWord


/**
 * Fragment containing the crossword puzzle.
 */
class PuzzleFragment : Fragment() {

    //region PUBLIC INTERFACES ---------------------------------------------------------------------

    // interface for activity to implement to receive touch event
    interface PuzzleListener {
        fun onPuzzleClick(gameWord: GameWord)
    }

    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private var gridWidth: Int = 0
    private var gridHeight: Int = 0
    private lateinit var vibration: Vibration

    //endregion

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    var initialized = false
    lateinit var cellGrid: Array<Array<GridCell?>>

    var selectedCellIndex = 0

    var currentGameWord: GameWord? = null
        private set(gameWord) {
            field?.let { showAsSelected(it, false) }   // unselect previous word
            gameWord?.let { showAsSelected(it, true) } // select new word
            field = gameWord
        }

    var scrollPosition: Int
        get() = (view as? ScrollView)?.scrollY ?: 0
        set(value) {
            (view as? ScrollView)?.smoothScrollTo(0, value)
        }

    //endregion

    //region OVERRIDDEN FUNCTIONS ------------------------------------------------------------------

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_puzzle, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get instance of Vibrator from current Context
        context?.let {
            vibration = Vibration(it)
        }
    }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    fun initialize(gridWidth: Int, gridHeight: Int) {
        this.gridWidth = gridWidth
        this.gridHeight = gridHeight

        // set up grid
        cellGrid = Array(gridHeight) { arrayOfNulls<GridCell>(gridWidth) }

        // create table rows
        for (row in 0 until gridHeight) {
            val tableRow = TableRow(activity)
            tableRow.gravity = Gravity.CENTER
            cell_table_layout.addView(tableRow)
        }

        initialized = true
    }

    fun doWordsFitInGrid(gameWords: List<GameWord>): Boolean {
        gameWords.forEach {
            if ((it.row >= gridHeight || it.col >= gridWidth) ||
                    (it.isAcross && it.col + it.word.length > gridWidth) ||
                    (!it.isAcross && it.row + it.word.length > gridHeight)) {
                return false
            }
        }
        return true
    }

    fun clearExistingGame() {
        // clear out any existing data
        currentGameWord = null
        for (row in 0 until gridHeight) {
            val tableRow = cell_table_layout.getChildAt(row) as TableRow
            tableRow.removeAllViews()
            cellGrid[row].fill(null)
        }
    }

    /**
     * Creates grid of textviews making up the puzzle.
     *
     * @param puzzleListener implementation of the [PuzzleListener] interface that listens for clicks on the puzzle.
     */
    fun createGrid(puzzleListener: PuzzleListener) {
        val onPuzzleClickListener = OnClickListener { v ->
            getCellForView(v)?.let {gridCell ->
                vibration.vibrate()

                val sameWordSelected = currentGameWord == gridCell.gameWordDown || currentGameWord == gridCell.gameWordAcross
                val newGameWord = if (sameWordSelected) {
                    currentGameWord
                } else {
                    gridCell.gameWordDown ?: gridCell.gameWordAcross
                }

                newGameWord?.let {
                    selectedCellIndex = if (it.isAcross) gridCell.acrossIndex else gridCell.downIndex
                    currentGameWord = newGameWord  // this assignment will call setter which will take care of showing word and cell as selected

                    // Notify the listener regardless of whether new word selected or not (e.g. so keyboard can be shown).
                    puzzleListener.onPuzzleClick(it)
                }
            }
        }

        // add views into table rows and columns
        var firstGameWord: GameWord? = null
        for (row in 0 until gridHeight) {
            val tableRow = cell_table_layout.getChildAt(row) as TableRow
            tableRow.removeAllViews()
            for (col in 0 until gridWidth) {
                cellGrid[row][col]?.let {
                    // create text view for this row and column
                    val textView = PuzzleCellTextView(context!!)
                    textView.setOnClickListener(onPuzzleClickListener)
                    tableRow.addView(textView, col)
                    it.view = textView

                    fillTextView(it)

                    // set current game word to the first across word found
                    if (firstGameWord == null) {
                        firstGameWord = it.gameWordAcross ?: it.gameWordDown
                    }
                }
                        ?: run {
                            tableRow.addView(View(activity), col)
                        }
            }
        }

        // make the initial word selection
        if (!selectNextGameWord(0, -1, true)
                && !selectNextGameWord(0, -1, false)) {
            selectedCellIndex = firstGameWord?.defaultSelectionIndex ?: 0
            currentGameWord = firstGameWord
        }
    }

    /**
     * Selects next empty or errored game word depending on parameter.
     *
     * @param emptyOnly true if next empty game word should be selected, false if any errored game word shold be selected.
     */
    fun selectNextGameWord(emptyOnly: Boolean): Boolean {
        return selectNextGameWord(currentGameWord?.row ?: 0, currentGameWord?.col
                ?: 0, emptyOnly) ||
                selectNextGameWord(0, -1, emptyOnly)
    }

    /**
     * Indicates errored cells in the puzzle with a reddish background.
     *
     * @param showErrors true if errors should be indicated, false if not.
     */
    fun showErrors(showErrors: Boolean) {
        // update background of cells based on whether text is correct or not
        for (row in 0 until gridHeight) {
            for (col in 0 until gridWidth) {
                // if cell is part of currently selected game word, adjust the level to set the background color
                cellGrid[row][col]?.let {
                    val isSelected = currentGameWord == it.gameWordAcross || currentGameWord == it.gameWordDown
                    it.view?.setStyle(isSelected, showErrors && it.hasUserError)
                }
            }
        }
    }

    /**
     * @param correctly true if puzzle considered complete when everything filled in correctly, false if puzzle
     * considered complete when everthing filled in regardless of correctness.
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

    /**
     * Fills in the puzzle with the user's answer for the specified game word.
     */
    fun updateUserTextInPuzzle(userText: String) {
        currentGameWord?.let {
            it.setUserText(userText.take(it.word.length))
            updateUserEntryInPuzzle(it)
            selectedCellIndex = it.defaultSelectionIndex
            showAsSelected(it, true)
        }
    }

    fun deleteLetterInPuzzle(): GameWord? {
        return updateLetterInPuzzle(GameWord.BLANK)
    }

    fun updateLetterInPuzzle(userChar: Char): GameWord? {
        currentGameWord?.let {
            it.userEntry[selectedCellIndex] = userChar
            val row = if (it.isAcross) it.row else it.row + selectedCellIndex
            val col = if (it.isAcross) it.col + selectedCellIndex else it.col
            return updateUserLetterInPuzzle(userChar, it.isAcross, row, col)
        }
        return null
    }

    fun advanceSelectedCellInPuzzle(backwardDirection: Boolean) {
        currentGameWord?.let {
            if (backwardDirection) {
                selectedCellIndex = (selectedCellIndex - 1).coerceAtLeast(0)
            } else {
                selectedCellIndex = (selectedCellIndex + 1).coerceAtMost(it.word.length - 1)
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
            cellGrid[row][col]?.let {
                val userChar = userEntry[charIndex]
                if (isAcross) {
                    it.userCharAcross = userChar
                    col++
                } else {
                    it.userCharDown = userChar
                    row++
                }
                fillTextView(it)
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
        cellGrid[row][col]?.let {
            if (isAcross) {
                val inConflict = it.userCharDown != GameWord.BLANK && userChar != it.userCharDown
                it.userCharAcross = userChar
                if (inConflict) {
                    it.userCharDown = userChar
                    val index = it.downIndex
                    conflictingGameWord = it.gameWordDown
                    conflictingGameWord?.let {
                        it.userEntry[index] = userChar
                    }
                }
            } else {
                val inConflict = it.userCharAcross != GameWord.BLANK && userChar != it.userCharAcross
                it.userCharDown = userChar
                if (inConflict) {
                    it.userCharAcross = userChar
                    val index = it.acrossIndex
                    conflictingGameWord = it.gameWordAcross
                    conflictingGameWord?.let {
                        it.userEntry[index] = userChar
                    }
                }
            }
            fillTextView(it)
        }
        return conflictingGameWord
    }

    /**
     * Selects next empty or errored game word depending on parameter.
     *
     * @param emptyOnly true if next empty game word should be selected, false if any errored game word should be selected.
     */
    private fun selectNextGameWord(startingRow: Int, startingCol: Int, emptyOnly: Boolean): Boolean {
        var initialCol = startingCol
        for (row in 0 until gridHeight) {
            if (row >= startingRow) {
                for (col in 0 until gridWidth) {
                    if (col >= initialCol) {
                        cellGrid[row][col]?.let {
                            var nextGameWord: GameWord? = null
                            if (row > startingRow || col > initialCol) {
                                // if the cell's word begins in the cell, then select it
                                if (it.gameWordAcross?.col == col && it.gameWordAcross != currentGameWord && isEmptyOrErroredGameWord(it.gameWordAcross, emptyOnly)) {
                                    nextGameWord = it.gameWordAcross
                                } else if (it.gameWordDown?.row == row && it.gameWordDown != currentGameWord && isEmptyOrErroredGameWord(it.gameWordDown, emptyOnly)) {
                                    nextGameWord = it.gameWordDown
                                }
                            } else if (it.gameWordDown?.row == row && it.gameWordDown != currentGameWord && isEmptyOrErroredGameWord(it.gameWordDown, emptyOnly)) {
                                nextGameWord = it.gameWordDown
                            }
                            nextGameWord?.let {
                                selectedCellIndex = it.defaultSelectionIndex
                                currentGameWord = it
                                return true
                            }
                        }
                    }
                }
                // for subsequent rows, start at first column
                initialCol = 0
            }
        }
        return false
    }

    private fun isEmptyOrErroredGameWord(gameWord: GameWord?, emptyOnly: Boolean): Boolean {
        gameWord?.let {
            return hasVisibleBlanks(it) || !emptyOnly && it.hasErroredCells
        }
        return false
    }

    private fun hasVisibleBlanks(gameWord: GameWord): Boolean {
        var row = gameWord.row
        var col = gameWord.col
        for (i in 0 until gameWord.word.length) {
            cellGrid[row][col]?.let {
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
            for (i in 0 until it.word.length) {
                if (asSelected && i == selectedCellIndex) {
                    cellGrid[row][col]?.view?.setIndividualSelection(true)
                } else {
                    cellGrid[row][col]?.view?.setSelection(asSelected)
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
     */
    private fun fillTextView(gridCell: GridCell) {
        gridCell.view?.let {
            val text = when {
                gridCell.isConflict -> "${gridCell.userCharAcross}/${gridCell.userCharDown}"
                gridCell.isBlank -> null
                else -> gridCell.userChar.toString()
            }
            it.fillTextView(text)
        }
    }

    /**
     * Gets [GridCell] cell corresponding to the specified view.
     *
     * @param v view to get [GridCell] for.
     */
    private fun getCellForView(v: View): GridCell? {
        cellGrid.forEach {
            it.forEach {
                if (v === it?.view) {
                    return it
                }
            }
        }
        return null
    }

    //endregion

}

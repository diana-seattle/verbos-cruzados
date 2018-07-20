package org.indiv.dls.games.verboscruzados.feature

import org.indiv.dls.games.verboscruzados.feature.game.GameWord


import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.View.OnClickListener
import android.widget.ScrollView
import android.widget.TableRow
import kotlinx.android.synthetic.main.fragment_puzzle.*
import org.indiv.dls.games.verboscruzados.feature.MainActivity.Companion.currentGameWord
import org.indiv.dls.games.verboscruzados.feature.component.PuzzleCellTextView


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
    private var vibrator: Vibrator? = null

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
        vibrator = activity!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
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
            getCellForView(v)?.let {
                vibrator?.vibrate(25)
                val gridCell = it

                val sameWordSelected = currentGameWord == gridCell.gameWordDown || currentGameWord == gridCell.gameWordAcross
                val newGameWord = if (sameWordSelected) {
                    currentGameWord
                } else {
                    gridCell.gameWordDown ?: gridCell.gameWordAcross
                }

                newGameWord?.let {
                    selectedCellIndex = getIndexOfCellInWord(it, gridCell).coerceAtMost(it.userText.length)
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
        currentGameWord = firstGameWord
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
                    it.view?.setStyle(isSelected, showErrors && it.hasUserError())
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
                    if (gridCell.dominantUserChar == null || correctly && gridCell.hasUserError()) {
                        return false
                    }
                }
            }
        }
        return true
    }

    /*
     * Fills in the puzzle with the user's answer for the specified game word.
     */
    fun updateUserTextInPuzzle(gameWord: GameWord) {
        // show answer in puzzle
        val userText = gameWord.userText
        val userTextLength = userText.length
        val wordLength = gameWord.word.length
        val isAcross = gameWord.isAcross
        var row = gameWord.row
        var col = gameWord.col
        for (charIndex in 0 until wordLength) {
            cellGrid[row][col]?.let {
                val userChar = if (charIndex < userTextLength) userText[charIndex] else null
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

    private fun getIndexOfCellInWord(gameWord: GameWord, gridCell: GridCell): Int {
        val isAcross = gameWord.isAcross
        var row = gameWord.row
        var col = gameWord.col
        for (charIndex in 0 until gameWord.word.length) {
            cellGrid[row][col]?.let {
                if (it == gridCell) {
                    return if (isAcross) col - gameWord.col else row - gameWord.row
                }
                if (isAcross) {
                    col++
                } else {
                    row++
                }
            }
        }
        return 0
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    /**
     * Selects next empty or errored game word depending on parameter.
     *
     * @param emptyOnly true if next empty game word should be selected, false if any errored game word shold be selected.
     */
    private fun selectNextGameWord(startingRow: Int, startingCol: Int, emptyOnly: Boolean): Boolean {
        var initialCol = startingCol
        for (row in 0 until gridHeight) {
            if (row >= startingRow) {
                for (col in 0 until gridWidth) {
                    if (col >= initialCol) {
                        cellGrid[row][col]?.let {
                            if (row > startingRow || col > initialCol) {
                                // if the cell's word begins in the cell, then select it
                                if (it.gameWordAcross?.col == col && it.gameWordAcross != currentGameWord && isEmptyOrErroredGameWord(it.gameWordAcross, emptyOnly)) {
                                    currentGameWord = it.gameWordAcross
                                    return true
                                } else if (it.gameWordDown?.row == row && it.gameWordDown != currentGameWord && isEmptyOrErroredGameWord(it.gameWordDown, emptyOnly)) {
                                    currentGameWord = it.gameWordDown
                                    return true
                                }
                            } else if (it.gameWordDown?.row == row && it.gameWordDown != currentGameWord) {
                                currentGameWord = it.gameWordDown
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
        return gameWord?.let {
            emptyOnly && it.userText.isEmpty() || (!emptyOnly && !it.isAnsweredCorrectly)
        } ?: false
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
        gridCell.view?.fillTextView(gridCell.dominantUserChar?.toUpperCase())
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

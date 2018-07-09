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
import android.widget.TableRow
import kotlinx.android.synthetic.main.fragment_puzzle.*
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

    var currentGameWord: GameWord? = null
        private set(gameWord) {
            field?.let { showAsSelected(it, false) }
            gameWord?.let { showAsSelected(it, true) }
            field = gameWord
        }

    /**
     * List of cell values from opposing words of the the currently selected word.
     */
    val opposingPuzzleCellValues: Map<Int, Char>
        get() {
            val puzzleCellValues = HashMap<Int, Char>()
            currentGameWord?.let {
                val isAcross = it.isAcross
                val wordLength = it.word.length
                var row = it.row
                var col = it.col
                for (charIndex in 0 until wordLength) {
                    cellGrid[row][col]?.let {
                        when {
                            isAcross && it.userCharDown != null -> it.userCharDown
                            !isAcross && it.userCharAcross != null -> it.userCharAcross
                            else -> null
                        }
                    }?.let {
                        puzzleCellValues.put(charIndex, it)
                    }

                    if (isAcross) {
                        col++
                    } else {
                        row++
                    }
                }
            }
            return puzzleCellValues
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
        // create click listener for puzzle clicks
        val onPuzzleClickListener = OnClickListener { v ->
            getCellForView(v)?.let {
                vibrator?.vibrate(25)
                currentGameWord = it.gameWordDown ?: it.gameWordAcross
                currentGameWord?.let {
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
     * Selects next game word containing an error.
     */
    fun selectNextErroredGameWord(): Boolean {
        for (row in 0 until gridHeight) {
            for (col in 0 until gridWidth) {
                cellGrid[row][col]?.let {
                    when {
                        it.hasUserErrorAcross() -> {
                            currentGameWord = it.gameWordAcross
                            return true
                        }
                        it.hasUserErrorDown() -> {
                            currentGameWord = it.gameWordDown
                            return true
                        }
                        else -> {
                        }
                    }
                }
            }
        }
        return false
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

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    /**
     * Shows the specified word as selected (yellow highlight).
     *
     * @param gameWord word to show as selected or unselected.
     * @param asSelected true if word should be shown as selected, false if unselected.
     */
    private fun showAsSelected(gameWord: GameWord?, asSelected: Boolean) {
        if (gameWord != null) {
            var row = gameWord.row
            var col = gameWord.col
            for (i in 0 until gameWord.word.length) {
                cellGrid[row][col]?.view?.setSelection(asSelected)
                if (gameWord.isAcross) {
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

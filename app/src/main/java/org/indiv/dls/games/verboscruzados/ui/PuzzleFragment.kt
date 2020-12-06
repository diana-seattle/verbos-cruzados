package org.indiv.dls.games.verboscruzados.ui


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TableRow
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModelProvider
import org.indiv.dls.games.verboscruzados.MainActivityViewModel
import org.indiv.dls.games.verboscruzados.MainActivityViewModelFactory
import org.indiv.dls.games.verboscruzados.ui.component.PuzzleCellTextView
import org.indiv.dls.games.verboscruzados.databinding.FragmentPuzzleBinding
import org.indiv.dls.games.verboscruzados.model.GameWord
import org.indiv.dls.games.verboscruzados.model.GridCell


/**
 * Fragment containing the crossword puzzle.
 */
class PuzzleFragment : Fragment() {

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    // binding is only valid between onCreateView and onDestroyView
    private var _binding: FragmentPuzzleBinding? = null
    private val binding get() = _binding!!

    @VisibleForTesting lateinit var viewModel: MainActivityViewModel

    private lateinit var vibration: Vibration

    @VisibleForTesting var gameWordLastSelected: GameWord? = null

    private var gameInitialized = false;

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

        viewModel = ViewModelProvider(requireActivity(), MainActivityViewModelFactory(requireActivity()))
                .get(MainActivityViewModel::class.java)

        viewModel.currentGameWord.observe(viewLifecycleOwner) { gameWord ->
            if (gameInitialized) {
                // deselect word that currently has selection, if any
                gameWordLastSelected?.let { showAsSelected(it, false) }

                // select new word if any
                gameWord?.let { showAsSelected(it, true) }

                // Remember last selected so we can deselect it later.
                gameWordLastSelected = gameWord
            }
        }

        // create table rows
        for (row in 0 until viewModel.gridHeight) {
            val tableRow = TableRow(activity)
            tableRow.gravity = Gravity.CENTER
            binding.cellTableLayout.addView(tableRow)
        }

        // Get instance of Vibrator from current Context
        context?.let {
            vibration = Vibration(it)
        }
    }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    fun clearExistingGame() {
        // clear out any existing data
        for (row in 0 until viewModel.gridHeight) {
            val tableRow = binding.cellTableLayout.getChildAt(row) as TableRow
            tableRow.removeAllViews()
        }
        viewByPositionMap.clear()
        positionOfViewMap.clear()
        gameWordLastSelected = null
        gameInitialized = false
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
        gameInitialized = true

        // If a game word is selected already, this represents a config change. Reselect the same word so we can observe
        // it and update the views accordingly.
        viewModel.currentGameWord.value?.let {
            viewModel.selectNewGameWord(it)
        } ?: run {
            // Otherwise, no selection has been made yet. Choose a word in this priority:
            // 1. Select first word with empty cells if any
            // 2. Select first word with errored cells if any
            // 3. Select first word (this can happen if user returns to a finished game)
            if (!viewModel.selectNextGameWord(0, 0, true)
                    && !viewModel.selectNextGameWord(0, 0, false)) {
                viewModel.selectNewGameWord(firstGameWord)
            }
        }
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
                // if cell is part of currently selected game word, adjust the level to set the background color
                viewModel.cellGrid[row][col]?.let { cell ->
                    val isSelected = viewModel.currentGameWord.value?.let {
                        it == cell.gameWordAcross || it == cell.gameWordDown
                    } ?: false

                    viewByPositionMap[Position(row, col)]?.setStyle(isSelected, indicateError = showErrors && cell.hasUserError)
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
            viewModel.cellGrid[row][col]?.let {
                val userChar = userEntry[charIndex]
                val position = Position(row, col)
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
            fillTextView(cell, viewByPositionMap[Position(row, col)])
        }
        return conflictingGameWord
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

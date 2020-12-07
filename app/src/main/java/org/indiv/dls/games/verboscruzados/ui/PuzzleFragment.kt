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
import org.indiv.dls.games.verboscruzados.viewmodel.MainActivityViewModel
import org.indiv.dls.games.verboscruzados.viewmodel.MainActivityViewModelFactory
import org.indiv.dls.games.verboscruzados.ui.component.PuzzleCellTextView
import org.indiv.dls.games.verboscruzados.databinding.FragmentPuzzleBinding
import org.indiv.dls.games.verboscruzados.model.GridCell
import org.indiv.dls.games.verboscruzados.model.PuzzleWordPresentation


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

    @VisibleForTesting var puzzleWordLastSelected: PuzzleWordPresentation? = null

    private var gameInitialized = false;

    // Used to find a view based on a position value
    private var viewByPositionMap: MutableMap<Position, PuzzleCellTextView> = mutableMapOf()

    // Used to find the position of a view
    private var positionOfViewMap: MutableMap<PuzzleCellTextView, Position> = mutableMapOf()

    private val onPuzzleClickListener = OnClickListener { v ->
        getCellForView(v)?.let { gridCell ->
            vibration.vibrate()

            // If clicked-on cell is part of the already selected word, let it remain the selected word.
            val (newPuzzleWordId, isAcross) = viewModel.selectedPuzzleWord.value?.takeIf { current ->
                current.id == gridCell.gameWordIdDown || current.id == gridCell.gameWordIdAcross
            }?.let { current ->
                (current.id to current.isAcross)
            } ?: run {
                // A different word was clicked on.

                // Choose the vertical word if exists, otherwise the horizontal word.
                val newPuzzleWordId = gridCell.gameWordIdDown ?: gridCell.gameWordIdAcross
                val newWordIsAcross = newPuzzleWordId == gridCell.gameWordIdAcross
                (newPuzzleWordId to newWordIsAcross)
            }

            // This will trigger notifications, which will take care of showing word and individual cell as selected.
            // (The main activity needs this too even if same word, e.g. to reshow the keyboard if it has been dismissed.)
            viewModel.selectNewGameWord(
                    gameWordId = newPuzzleWordId,
                    charIndexOfSelectedCell = if (isAcross) gridCell.acrossCharIndex else gridCell.downCharIndex
            )
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

        viewModel.selectedPuzzleWord.observe(viewLifecycleOwner) { puzzleWord ->
            if (gameInitialized) {
                // deselect word that currently has selection, if any
                puzzleWordLastSelected?.let { showAsSelected(it, false) }

                // select new word if any
                puzzleWord?.let { showAsSelected(it, true) }

                // Remember last selected so we can deselect it later.
                puzzleWordLastSelected = puzzleWord
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
        puzzleWordLastSelected = null
        gameInitialized = false
    }

    /**
     * Creates grid of textviews making up the puzzle.
     */
    fun createGridViewsAndSelectWord() {

        // add views into table rows and columns
        val activityContext = requireContext()
        var firstPuzzleWordId: String? = null
        for (row in 0 until viewModel.gridHeight) {
            val tableRow = binding.cellTableLayout.getChildAt(row) as TableRow
            tableRow.removeAllViews()
            for (col in 0 until viewModel.gridWidth) {
                viewModel.cellGrid[row][col]?.let { cell ->
                    // create text view for this row and column
                    val textView = PuzzleCellTextView(activityContext)
                    textView.setOnClickListener(onPuzzleClickListener)
                    tableRow.addView(textView, col)

                    val position = Position(row, col)
                    viewByPositionMap[position] = textView
                    positionOfViewMap[textView] = position

                    fillTextView(cell, textView)

                    // set current game word to the first across word found
                    if (firstPuzzleWordId == null) {
                        firstPuzzleWordId = cell.gameWordIdAcross ?: cell.gameWordIdDown
                    }
                } ?: run {
                    tableRow.addView(View(activityContext), col)
                }
            }
        }
        gameInitialized = true

        // If a game word is selected already, this represents a config change. Reselect the same word so we can observe
        // it and update the views accordingly.
        viewModel.selectedPuzzleWord.value?.let {
            viewModel.selectNewGameWord(it.id)
        } ?: run {
            // Otherwise, no selection has been made yet. Choose a word in this priority:
            // 1. Select first word with empty or errored cells if any
            // 2. Select first word (this can happen if user returns to a finished game)
            if (!viewModel.selectNextGameWordFavoringIncomplete()) {
                viewModel.selectNewGameWord(firstPuzzleWordId)
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
                    val isSelected = viewModel.selectedPuzzleWord.value?.let {
                        it.id == cell.gameWordIdAcross || it.id == cell.gameWordIdDown
                    } ?: false

                    viewByPositionMap[Position(row, col)]?.setStyle(isSelected, indicateError = showErrors && cell.hasUserError)
                }
            }
        }
    }

    /**
     * Fills in the entire selected word with the specified text.
     */
    fun refreshTextOfSelectedWord() {
        viewModel.selectedPuzzleWord.value?.let { puzzleWord ->
            // show answer in puzzle
            var row = puzzleWord.startingRow
            var col = puzzleWord.startingCol
            for (charIndex in 0 until puzzleWord.answer.length) {
                viewModel.cellGrid[row][col]?.let {
                    fillTextView(it, viewByPositionMap[Position(row, col)])
                    if (puzzleWord.isAcross) col++ else row++
                }
            }
            showAsSelected(puzzleWord, true)
        }
    }

    /**
     * Updates letter in the selected cell of the puzzle.
     */
    fun refreshCharOfSelectedCell() {
        viewModel.selectedPuzzleWord.value?.let { puzzleWord ->
            val row = if (puzzleWord.isAcross)
                puzzleWord.startingRow else puzzleWord.startingRow + viewModel.charIndexOfSelectedCell
            val col = if (puzzleWord.isAcross)
                puzzleWord.startingCol + viewModel.charIndexOfSelectedCell else puzzleWord.startingCol
            viewModel.cellGrid[row][col]?.let { cell ->
                fillTextView(cell, viewByPositionMap[Position(row, col)])
            }
        }
    }

    /**
     * Re-displays currently selected word with appropriate cell selection and style.
     */
    fun refreshStyleOfSelectedWord() {
        viewModel.selectedPuzzleWord.value?.let {
            showAsSelected(it, true)
        }
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    /**
     * Shows the specified word as selected (yellow highlight), or not selected.
     *
     * @param puzzleWord word to show as selected or unselected.
     * @param asSelected true if word should be shown as selected, false if unselected.
     */
    private fun showAsSelected(puzzleWord: PuzzleWordPresentation?, asSelected: Boolean) {
        puzzleWord?.let {
            var row = it.startingRow
            var col = it.startingCol
            for (i in it.answer.indices) {
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

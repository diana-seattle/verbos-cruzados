package org.indiv.dls.games.verboscruzados.feature

/*
 * Play Store: https://play.google.com/store/apps/details?id=org.indiv.dls.games.verboscruzados
 */

import org.indiv.dls.games.verboscruzados.feature.async.GameSetup
import org.indiv.dls.games.verboscruzados.feature.game.GameWord
import org.indiv.dls.games.verboscruzados.feature.dialog.ConfirmStartNewGameDialogFragment

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.indiv.dls.games.verboscruzados.feature.MyActionBarActivity.Companion.currentGameWord
import kotlin.math.round

/**
 * This is the main activity. It houses [PuzzleFragment], and optionally [AnswerFragment] when in landscape mode (on tablets).
 */
class MainActivity : MyActionBarActivity(), ConfirmStartNewGameDialogFragment.StartNewGameDialogListener,
        AnswerFragment.DualPaneAnswerListener, PuzzleFragment.PuzzleListener {

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private val RESULTCODE_ANSWER = 100
        const val ACTIVITYRESULT_ANSWER = "answer"
    }

    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private val compositeDisposable = CompositeDisposable()
    private var currentGameWords: List<GameWord> = emptyList()
    private val gameSetup = GameSetup()
    private lateinit var puzzleFragment: PuzzleFragment
    private var answerFragment: AnswerFragment? = null // for use in panel
    private var answerActivityLaunched = false // use this to load activity only once when puzzle double clicked on

    private var showingErrors = false

    //endregion

    //region OVERRIDDEN METHODS --------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (BuildConfig.BUILD_TYPE === "debug") {
            // This is a special debug-build-only hack that allows the developer/tester to complete a game immediately.
            toolbar?.setOnLongClickListener { v ->
                do {
                    currentGameWord = puzzleFragment.currentGameWord
                    currentGameWord?.let {
                        it.word?.let {
                            onFinishAnswerDialog(it)
                        }
                    }
                } while (puzzleFragment.selectNextErroredGameWord() == true)
                true
            }
        }


        // get puzzle fragment
        puzzleFragment = supportFragmentManager.findFragmentById(R.id.puzzle_fragment) as PuzzleFragment

        // get answer fragment if present (this will be found only in dual pane mode)
        answerFragment = supportFragmentManager.findFragmentById(R.id.answer_fragment) as AnswerFragment?

        val displayMetrics = resources.displayMetrics

        // if answer fragment present (dual pane mode), use landscape orientation
        answerFragment?.let {
            it.view?.visibility = View.GONE // set invisible until puzzle shows up
        }

        // calculate available space for the puzzle
        val screenHeightPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                resources.configuration.screenHeightDp.toFloat(), displayMetrics)
        val puzzleHeightPixels = screenHeightPixels - Math.round(resources.getDimension(R.dimen.fragment_answer_height))
        val puzzleWidthPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                resources.configuration.smallestScreenWidthDp.toFloat(), displayMetrics)

        // calculate number of pixels equivalent to 24dp (24dp allows 13 cells on smallest screen supported by Android (320dp width, 426dp height))
        val pixelsPerCell = Math.round(resources.getDimension(R.dimen.cell_width))
        val gridHeight = (puzzleHeightPixels / pixelsPerCell - 2).toInt() // subtract 2 because subtracting action bar height doesn't seem to be enough
        val gridWidth = (puzzleWidthPixels / pixelsPerCell - 1).toInt()   // subtract 1 to give margin for consistency with height

        if (puzzleWidthPixels > 0) {
            puzzleFragment.initialize(gridWidth, gridHeight)
            loadNewOrExistingGame()
        }

//        puzzleFragment.view?.viewTreeObserver?.addOnGlobalLayoutListener {
//            if (answerFragment?.view?.height ?: 0 > 0 && !puzzleFragment.initialized) {
//                val puzzleWidthPixels = puzzleFragment.view?.width ?: 0
//                val puzzleHeightPixels = puzzleFragment.view?.height ?: 0
//                val smallestScreenWidthPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
//                        resources.configuration.smallestScreenWidthDp.toFloat(), displayMetrics)
//                val screenHeightPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
//                        resources.configuration.screenHeightDp.toFloat(), displayMetrics)
//
//
//                val gridHeight = puzzleHeightPixels / pixelsPerCell - 2 // subtract 2 because subtracting action bar height doesn't seem to be enough
//                val gridWidth = puzzleWidthPixels / pixelsPerCell - 1   // subtract 1 to give margin for consistency with height
////            val gridHeight = round(puzzleHeightPixels / pixelsPerCell - 2).toInt() // subtract 2 because subtracting action bar height doesn't seem to be enough
////            val gridWidth = round(puzzleWidthPixels / pixelsPerCell - 1).toInt()  // subtract 1 to give margin for consistency with height
//
//                if (puzzleWidthPixels > 0) {
//                    puzzleFragment.initialize(gridWidth, gridHeight)
//                    loadNewOrExistingGame()
//                }
//            }
//        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_options, menu)
        optionsMenu = menu
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()

        currentGameWord = null
        showingErrors = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) {
        super.onActivityResult(requestCode, resultCode, result)

        // if response from answer activity
        if (requestCode == RESULTCODE_ANSWER) {
            if (resultCode == Activity.RESULT_OK && result != null) {
                val userText = result.getStringExtra(MainActivity.ACTIVITYRESULT_ANSWER)
                onFinishAnswerDialog(userText)
            }
            answerActivityLaunched = false
        }
    }

    /**
     * Handles presses on the action bar items.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_showerrors -> showErrors(!showingErrors)
            R.id.action_startnewgame -> promptForNewGame(null)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    //endregion

    //region INTERFACE METHODS (PuzzleFragment.PuzzleListener) -------------------------------------

    /*
     * implements PuzzleListener interface for callback from PuzzleFragment
     */
    override fun onPuzzleClick(gameWord: GameWord) {
        currentGameWord = gameWord
        val answerPresentation = createAnswerPresentation(gameWord)

        // update answer fragment with current game word
        answerFragment?.let {
            // dual pane mode
            it.setGameWord(answerPresentation)
        } ?: run {
            // single pane mode
            if (!answerActivityLaunched) {
                val intent = AnswerActivity.getIntent(this, answerPresentation)
                startActivityForResult(intent, RESULTCODE_ANSWER)
                answerActivityLaunched = true
            }
        }
    }

    //endregion

    //region INTERFACE FUNCTIONS (AnswerFragment.DualPaneAnswerListener) ---------------------------

    /*
     * implements interface for receiving callback from AnswerFragment
     */
    override fun onFinishAnswerDialog(userText: String) {

        // in dual pane mode, this method may be called by answer dialog during setup (on text change)
        if (puzzleFragment.currentGameWord == null) {
            return
        }

        puzzleFragment.currentGameWord?.let {
            it.userText = userText.toUpperCase()
            puzzleFragment.updateUserTextInPuzzle(it)

            // update database with answer
            Thread { mDbHelper.persistUserEntry(it) }.start()
        }

        // update error indications
        if (showingErrors) {
            showErrors(showingErrors)
        }

        // if puzzle is complete and correct
        if (puzzleFragment.isPuzzleComplete(true)) {

            // prompt user with congrats and new game
            var extraMessage = resources.getString(R.string.dialog_startnewgame_congrats)
            promptForNewGame(extraMessage)

            // else if puzzle is complete but not correct, show errors
        } else if (!showingErrors && puzzleFragment.isPuzzleComplete(false)) {
            showErrors(true)
        }

    }

    //endregion

    //region INTERFACE METHODS (ConfirmStartNewGameDialogFragment.StartNewGameDialogListener) ------

    /**
     * create new game (called first time app run, or when user starts new game)
     * implements interface for receiving callback from ConfirmStartNewGameDialogFragment
     */
    override fun setupNewGame() {
        // if dual pane, clear game word and hide answer fragment for now
        answerFragment?.let {
            it.clearGameWord()
            it.view?.visibility = View.GONE // set invisible until puzzle shows up
        }

        // clear puzzle fragment of existing game if any
        puzzleFragment.clearExistingGame()
        showErrors(false)

        // setup new game
        compositeDisposable.add(gameSetup.newGame(puzzleFragment.cellGrid)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { gameWords ->
                            currentGameWords = gameWords
                            mDbHelper.persistGame(gameWords)
                            createGrid()
                        },
                        { error ->
                            Toast.makeText(this, R.string.error_game_setup_failure, Toast.LENGTH_SHORT).show()
                            Log.e(TAG, "Error setting up game: " + error.message)
                        }))
    }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------
    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    private fun createAnswerPresentation(gameWord: GameWord): AnswerPresentation {
        return AnswerPresentation(gameWord.word, gameWord.userText,
                gameWord.sentenceClue, gameWord.infinitiveClue, puzzleFragment.opposingPuzzleCellValues)
    }

    /*
     * show/hide errors in puzzle
     */
    private fun showErrors(showErrors: Boolean) {
        showingErrors = showErrors
        setOptionsMenuText(R.id.action_showerrors, if (showingErrors) R.string.action_hideerrors else R.string.action_showerrors)
        puzzleFragment.showErrors(showErrors)
    }

    private fun promptForNewGame(extraMessage: String?) {
        val dlg = ConfirmStartNewGameDialogFragment()
        dlg.setExtraMessage(extraMessage)
        dlg.showDlg(this as ConfirmStartNewGameDialogFragment.StartNewGameDialogListener,
                supportFragmentManager, "fragment_startnewgame")
    }

    /**
     * this is called when db setup completes
     */
    private fun loadNewOrExistingGame() {

        // get current game if any
        currentGameWords = mDbHelper.currentGameWords

        // if on very first game, or if no saved game (due to an error), create a new one, otherwise open existing game
        if (currentGameWords.isEmpty() || !puzzleFragment.doWordsFitInGrid(currentGameWords)) {
            setupNewGame()
        } else {
            restoreExistingGame()
        }
    }

    /**
     * open existing game
     */
    private fun restoreExistingGame() {
        // copy game words to cell grid
        for (gameWord in currentGameWords) {
            gameSetup.addToGrid(gameWord, puzzleFragment.cellGrid)
        }
        createGrid()
    }

    private fun createGrid() {
        puzzleFragment.createGrid(this)

        currentGameWord = puzzleFragment.currentGameWord

        // if dual panel, update answer fragment with current game word
        answerFragment?.let {
            if (currentGameWord != null) { // this extra check is necessary for case where setting up initial game and no words available in db
                it.setGameWord(createAnswerPresentation(currentGameWord!!))
                it.view?.visibility = View.VISIBLE // set answer dialog fragment visible now that puzzle drawn
            }
        }
    }

    //endregion

}



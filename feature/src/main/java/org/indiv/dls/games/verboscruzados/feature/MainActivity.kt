package org.indiv.dls.games.verboscruzados.feature

/*
 * Play Store: https://play.google.com/store/apps/details?id=org.indiv.dls.games.verboscruzados
 */

// TODO: change fragments into components
// TODO: answer styling refinements, puzzle size on small phones
// TODO: more verbs - or message the user about selecting more options
// TODO: test/fix conjugations
// TODO: stats screen
// TODO: help screen
// TODO: app icons
// TODO: instant app
// TODO: fix imports
// Complete other TODO items
// TODO: feedback on keyboard touch


// TODO: animate keyboard


// https://pixnio.com/nature-landscapes/deserts/desert-landscape-herb-canyon-dry-geology-mountain
// https://pixabay.com/en/canyon-desert-sky-huge-mountains-311233/
// https://www.pexels.com/photo/america-arid-bushes-california-221148/

import org.indiv.dls.games.verboscruzados.feature.async.GameSetup
import org.indiv.dls.games.verboscruzados.feature.game.GameWord

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.indiv.dls.games.verboscruzados.feature.dialog.GameOptionsDialogFragment
import org.indiv.dls.games.verboscruzados.feature.dialog.StatsDialogFragment
import org.indiv.dls.games.verboscruzados.feature.game.PersistenceHelper
import kotlinx.android.synthetic.main.activity_main.*


/**
 * This is the main activity. It houses [PuzzleFragment], and [AnswerFragment].
 */
class MainActivity : AppCompatActivity(), AnswerFragment.AnswerListener, PuzzleFragment.PuzzleListener {

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        private val TAG = MainActivity::class.java.simpleName

        var currentGameWord: GameWord? = null
    }

    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private val compositeDisposable = CompositeDisposable()
    private var currentGameWords: List<GameWord> = emptyList()
    private val gameSetup = GameSetup()
    private lateinit var puzzleFragment: PuzzleFragment
    private lateinit var answerFragment: AnswerFragment

    private var optionsMenu: Menu? = null
    private var toolbar: Toolbar? = null
    private lateinit var persistenceHelper: PersistenceHelper

    private var showingErrors = false

    //endregion

    //region OVERRIDDEN METHODS --------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        persistenceHelper = PersistenceHelper(this)

        // Set up toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (BuildConfig.BUILD_TYPE === "debug") {
            // This is a special debug-build-only hack that allows the developer/tester to complete a game immediately.
            toolbar?.setOnLongClickListener { v ->
                do {
                    currentGameWord = puzzleFragment.currentGameWord
                    currentGameWord?.let {
                        onUpdateAnswer(it.word)
                    }
                } while (puzzleFragment.selectNextErroredGameWord() == true)
                true
            }
        }

        // get puzzle and answer fragments
        puzzleFragment = supportFragmentManager.findFragmentById(R.id.puzzle_fragment) as PuzzleFragment
        answerFragment = supportFragmentManager.findFragmentById(R.id.answer_fragment) as AnswerFragment

        answerFragment.view?.visibility = View.GONE // set answer fragment invisible until puzzle shows up

        // calculate available space for the puzzle
        val displayMetrics = resources.displayMetrics
        val configuration = resources.configuration
        val marginInPixels = resources.getDimension(R.dimen.puzzle_margin)
        val actionBarHeightPixels = getActionBarHeightInPixels(displayMetrics)
        val answerHeightPixels = resources.getDimension(R.dimen.fragment_answer_height)
        val screenWidthDp = configuration.smallestScreenWidthDp
        val screenHeightDp = maxOf(configuration.screenHeightDp, configuration.screenWidthDp)
        val heightFactor = if (screenWidthDp < 400) 1.5f else 1f
        val screenWidthPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                screenWidthDp.toFloat(), displayMetrics)
        val screenHeightPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                screenHeightDp.toFloat(), displayMetrics)
        val puzzleHeightPixels = (screenHeightPixels - actionBarHeightPixels - answerHeightPixels - marginInPixels) * heightFactor
        val puzzleWidthPixels = screenWidthPixels - marginInPixels

        // calculate number of pixels equivalent to 24dp (24dp allows 13 cells on smallest screen supported by Android (320dp width, 426dp height))
        val pixelsPerCell = resources.getDimension(R.dimen.cell_width)
        val gridHeight = (puzzleHeightPixels / pixelsPerCell).toInt()
        val gridWidth = (puzzleWidthPixels / pixelsPerCell).toInt()

        if (gridWidth > 0 && gridHeight > 0) {
            puzzleFragment.initialize(gridWidth, gridHeight)
            loadNewOrExistingGame()
        }

        // pass the InputConnection from the EditText to the keyboard
        answer_keyboard.inputConnection = answerFragment.answerEntryInputConnection
        answer_keyboard.dismissClickListener = {
            hideKeyboardForAnswer()
        }
        answerFragment.keyboardNeededListener = {
            showKeyboardForAnswer()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main_options, menu)
        optionsMenu = menu
        return true
    }

    /**
     * Handles presses on the action bar items.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_showerrors -> showErrors(!showingErrors)
            R.id.action_startnewgame -> promptForNewGame(null)
            R.id.action_help -> showHelpDialog()
            R.id.action_showstats -> showStatsDialog()
            R.id.action_showgameoptions -> showGameOptionsDialog()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onBackPressed() {
        if (isKeyboardVisible()) {
            hideKeyboardForAnswer()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()

        currentGameWord = null
        showingErrors = false
    }

    //endregion

    //region INTERFACE METHODS (PuzzleFragment.PuzzleListener) -------------------------------------

    /*
     * implements PuzzleListener interface for callback from PuzzleFragment
     */
    override fun onPuzzleClick(gameWord: GameWord) {
        setGameWord(gameWord, !gameWord.isAnsweredCorrectly)
    }

    //endregion

    //region INTERFACE FUNCTIONS (AnswerFragment.AnswerListener) ---------------------------

    /*
     * implements interface for receiving callback from AnswerFragment
     */
    override fun onUpdateAnswer(userText: String) {

        // This method may be called by answer dialog during setup (on text change)
        if (puzzleFragment.currentGameWord == null ||
                puzzleFragment.currentGameWord?.userText == userText) {
            return
        }

        puzzleFragment.currentGameWord?.let {
            it.userText = userText
            puzzleFragment.updateUserTextInPuzzle(it)

            // update database with answer
            Thread { persistenceHelper.persistUserEntry(it) }.start()
        }

        // update error indications
        if (showingErrors) {
            showErrors(showingErrors)
        }

        // If user text matches answer, dismiss keyboard
        val currentWordIsCorrect = currentGameWord?.isAnsweredCorrectly == true
        if (currentWordIsCorrect) {
            hideKeyboardForAnswer()
        }

        // if puzzle is complete and correct
        if (currentWordIsCorrect && puzzleFragment.isPuzzleComplete(true)) {

            // prompt user with congrats and new game
            var extraMessage = resources.getString(R.string.dialog_startnewgame_congrats)
            promptForNewGame(extraMessage)

            // else if puzzle is complete but not correct, show errors
        } else if (!showingErrors && puzzleFragment.isPuzzleComplete(false)) {
            showErrors(true)
        }

    }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------
    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    private fun createAnswerPresentation(gameWord: GameWord): AnswerPresentation {
        return AnswerPresentation(gameWord.word, gameWord.userText, gameWord.conjugationLabel,
                gameWord.infinitive, gameWord.translation)
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
        val message = (if (extraMessage != null) extraMessage + "\n" else "") +
                resources.getString(R.string.dialog_startnewgame_prompt)
        AlertDialog.Builder(this)
                .setTitle(message)
                .setNeutralButton(R.string.dialog_startnewgame_yes_with_options) { _, _ -> showGameOptionsDialog() }
                .setPositiveButton(R.string.dialog_startnewgame_yes) { _, _ -> setupNewGame() }
                .setNegativeButton(R.string.dialog_startnewgame_no) { _, _ -> }
                .show()
    }

    /**
     * this is called when db setup completes
     */
    private fun loadNewOrExistingGame() {

        // get current game if any
        currentGameWords = persistenceHelper.currentGameWords

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

    /**
     * Creates new game (called first time app run, or when user starts new game)
     */
    private fun setupNewGame() {
        // Clear game word and hide answer fragment for now
        answerFragment.clearGameWord()
        answerFragment.view?.visibility = View.GONE // set invisible until puzzle shows up
        hideKeyboardForAnswer()

        // clear puzzle fragment of existing game if any
        puzzleFragment.clearExistingGame()
        showErrors(false)

        // setup new game
        compositeDisposable.add(gameSetup.newGame(puzzleFragment.cellGrid, persistenceHelper.currentGameOptions)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { gameWords ->
                            currentGameWords = gameWords
                            persistenceHelper.persistGame(gameWords)
                            createGrid()
                        },
                        { error ->
                            Toast.makeText(this, R.string.error_game_setup_failure, Toast.LENGTH_SHORT).show()
                            Log.e(TAG, "Error setting up game: " + error.message)
                        }))
    }

    private fun createGrid() {
        puzzleFragment.createGrid(this)

        currentGameWord = puzzleFragment.currentGameWord

        // Update answer fragment with current game word
        if (currentGameWord != null) { // this extra check is necessary for case where setting up initial game and no words available in db
            setGameWord(currentGameWord!!, false)
            answerFragment.view?.visibility = View.VISIBLE // set answer dialog fragment visible now that puzzle drawn
        }
    }

    private fun setGameWord(gameWord: GameWord, showKeyboard: Boolean) {
        currentGameWord = gameWord

        // update answer fragment with current game word
        answerFragment.setGameWord(createAnswerPresentation(gameWord))

        // Update keyboard with infinitive
        answer_keyboard.infinitive = gameWord.infinitive

        // If answer not yet correct, show keyboard
        if (showKeyboard) {
            showKeyboardForAnswer()
        }
    }

    private fun setOptionsMenuText(menuItemId: Int, textId: Int) {
        optionsMenu?.findItem(menuItemId)?.setTitle(textId)
    }

    private fun showHelpDialog() {
        AlertDialog.Builder(this)
                .setView(R.layout.fragment_help_dialog)
                .setPositiveButton(R.string.dialog_ok) { _, _ -> }
                .show()
    }

    private fun showStatsDialog() {
        val dlg = StatsDialogFragment()
        dlg.setStats(0, 0)
        dlg.show(supportFragmentManager, "fragment_showstats")
    }

    private fun showGameOptionsDialog() {
        val dlg = GameOptionsDialogFragment()
        dlg.startNewGameListener = { setupNewGame() }
        dlg.show(supportFragmentManager, "fragment_showoptions")
    }

    // note that with api level 13 and above we can use getResources().getConfiguration().screenHeightDp/screenWidthDp to get available screen size
    private fun getActionBarHeightInPixels(displayMetrics: DisplayMetrics): Int {
        // actionBar.getHeight() returns zero in onCreate (i.e. before it is shown)
        // for the following solution, see: http://stackoverflow.com/questions/12301510/how-to-get-the-actionbar-height/13216807#13216807
        var actionBarHeight = 0  // actionBar.getHeight() returns zero in onCreate
        val tv = TypedValue()
        if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, displayMetrics)
        }
        return actionBarHeight
    }

    private fun isKeyboardVisible(): Boolean {
        return answer_keyboard.visibility == View.VISIBLE
    }

    private fun showKeyboardForAnswer() {

        // TODO: animate

        answer_keyboard.visibility = View.VISIBLE
    }

    private fun hideKeyboardForAnswer() {

        // TODO: animate

        answer_keyboard.visibility = View.GONE
    }

    //endregion

}



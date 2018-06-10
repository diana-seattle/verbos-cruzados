package org.indiv.dls.games.vocabrecall.feature

/*
 * Over 4000/5000 words (such as ...), helps improve recall of commonly used vocabulary which is often heard and read
 * but which may be occasionally difficult to recall at the appropriate time due to infrequent personal use.
 * (the focus is on words you tend to already know but may occasionally struggle to recall due to infrequent use)
 * Definitions as clues aren't perfect. Some are too easy and some too hard, but I've done my best to programmatically
 * redact text that gives away the answer. The emphasis is not on trying to stump you, but on giving you practice
 * recalling words so it will become easier during natural conversation. A limited number of hints are available per
 * game for those last few words that are difficult to get.
 *
 * Words such as perpetuate, propagate, legacy, blemish, exacerbate, infiltrate, prolong, endorse, advocate, traipse, reconvene
 *
 * Wordnik: As a game is completed, definitions used in that game are flushed from the the cache and definitions
 * of new words are retrieved to replace them.
 *
 * A crossword puzzle game for strengthening your vocabulary recall ability.
 * We all encounter a rich vocabulary of words heard and read in the media we consume. However, our word recognition is often much stronger than our word recall due to less frequent personal use of those words. Often mid-sentence, we discover that the precise word needed to complete the thought isn't going to materialize, and we have to substitute less descriptive ones.
 * This game takes the form of a crossword puzzle, with dictionary entries as clues. The emphasis is not on trying to stump you, but on giving you practice recalling words so it will become easier during natural conversation. A limited number of extra hints are available per game for those last few words that are difficult to get.
 * Definitions are provided by American Heritage� Dictionary, Wiktionary, The Century Dictionary, and the GNU version of the Collaborative International Dictionary of English. When the word or a portion of the word is included in the definition, asterisks are substituted in place of the word.
 * Examples of words include perpetuate, propagate, legacy, blemish, exacerbate, infiltrate, prolong, endorse, advocate, traipse, reconvene.
 * This game may be played offline. However, playing with an active network connection will provide a greater variety of words.
 *
 * Play Store: https://play.google.com/store/apps/details?id=org.indiv.dls.games.vocabrecall
 */

//TODO: new full set of initial definitions
//TODO: optimize one-time initialization


/*
wordnik: http://developer.wordnik.com/docs.html#!/word/getDefinitions_get_2
wordnik usage stats: http://api.wordnik.com/v4/account.json/apiTokenStatus?api_key=f4e5b019cbc525972530c0bf0a0088162bff83d8464c1883a

similar to:
http://www.makeuseof.com/tag/design-great-looking-crossword-puzzles-for-yourself-windows/

http://dictionary-api.org/api - api.getDictionaries and api.getMorphologies returned nothing (also might be translation only)
http://thesaurus.altervista.org/dictionary-android - returns only very long html format
*/


import java.util.Date

import org.indiv.dls.games.vocabrecall.feature.async.DbSetup
import org.indiv.dls.games.vocabrecall.feature.async.DefinitionRetrieval
import org.indiv.dls.games.vocabrecall.feature.async.GameSetup
import org.indiv.dls.games.vocabrecall.feature.db.ContentHelper
import org.indiv.dls.games.vocabrecall.feature.db.Game
import org.indiv.dls.games.vocabrecall.feature.db.GameWord
import org.indiv.dls.games.vocabrecall.feature.dialog.ConfirmStartNewGameDialogFragment

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * This is the main activity. It houses [PuzzleFragment], and optionally [AnswerFragment] when in landscape mode (on tablets).
 */
class VocabRecallActivity : MyActionBarActivity(), ConfirmStartNewGameDialogFragment.StartNewGameDialogListener,
        AnswerFragment.DualPaneAnswerListener, PuzzleFragment.PuzzleListener {

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        private val TAG = VocabRecallActivity::class.java.simpleName
        private val RESULTCODE_ANSWER = 100
        const val ACTIVITYRESULT_ANSWER = "answer"
        const val ACTIVITYRESULT_CONFIDENT = "confident"

        private var showingErrors = false
    }

    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private val compositeDisposable = CompositeDisposable()
    private var currentGame: Game? = null
    private val gameSetup = GameSetup()
    private val dbSetup = DbSetup()
    private val definitionRetrieval = DefinitionRetrieval()
    private lateinit var puzzleFragment: PuzzleFragment
    private var answerFragment: AnswerFragment? = null // for use in panel
    private var answerActivityLaunched = false // use this to load activity only once when puzzle double clicked on

    private var progressDialog: ProgressDialog? = null
    private var timeProgressDialogShown: Long = 0
    private var helpShownYet = false

    // if no network is available networkInfo will be null, otherwise check if we are connected
    private val isNetworkAvailable: Boolean
        get() {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = cm.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }

    //endregion

    //region OVERRIDDEN METHODS --------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vocabrecall)

        // Set up toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        if (BuildConfig.BUILD_TYPE === "debug") {
            // This is a special debug-build-only hack that allows the developer/tester to complete a game immediately.
            toolbar?.setOnLongClickListener { v ->
                do {
                    currentGameWord = puzzleFragment.currentGameWord
                    currentGameWord?.let {
                        it.word?.toLowerCase()?.let {
                            onFinishAnswerDialog(it, true)
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

        // get action bar height
        val actionBarHeightInPixels = getActionBarHeightInPixels(displayMetrics)

        var puzzleWidthPixels = 0
        var puzzleHeightPixels = 0

        // if answer fragment present (dual pane mode), use landscape orientation
        answerFragment?.let {
            it.isVisible = false // set invisible until puzzle shows up

            // this allows screen to rotate 180deg in landscape mode
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

            // determine puzzle dimensions
            val answerPanelWidthPixels = Math.round(resources.getDimension(R.dimen.fragment_answer_width))
            puzzleWidthPixels = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels) - answerPanelWidthPixels
            puzzleHeightPixels = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) - actionBarHeightInPixels
        } ?: run {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            // determine puzzle dimensions
            puzzleWidthPixels = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels)
            puzzleHeightPixels = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels) - actionBarHeightInPixels
        }

        // initialize puzzle fragment after setting orientation so it knows its size
        puzzleFragment.initialize(puzzleWidthPixels, puzzleHeightPixels)

        // get database
        dbHelper = ContentHelper(this)
        dbHelper?.let {
            compositeDisposable.add(dbSetup.ensureDbLoaded(this, it)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(::onProgressDbSetup,
                            { Toast.makeText(this, R.string.error_initial_setup_failure, Toast.LENGTH_SHORT).show() },
                            this::onFinishDbSetup))
        }
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

        puzzleRepresentation = null
        currentGameWord = null
        dbHelper = null
        showingErrors = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) {
        super.onActivityResult(requestCode, resultCode, result)

        // if response from answer activity
        if (requestCode == RESULTCODE_ANSWER) {
            if (resultCode == Activity.RESULT_OK && result != null) {
                val userText = result.getStringExtra(VocabRecallActivity.ACTIVITYRESULT_ANSWER)
                val confident = result.getBooleanExtra(VocabRecallActivity.ACTIVITYRESULT_CONFIDENT, false)
                onFinishAnswerDialog(userText, confident)
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

    /*
     * Overriding to display answer
     */
    override fun giveAnswer() {
        super.giveAnswer()
        answerFragment?.let {
            // dual pane mode
            it.giveAnswer()
        } ?: run {
            // single pane mode
            onFinishAnswerDialog(currentGameWord!!.word, true)
        }
    }

    /*
     * Overriding to display hint
     */
    override fun give3LetterHint() {
        super.give3LetterHint()
        answerFragment?.let {
            // dual pane mode
            it.give3LetterHint()
        } ?: run {
            // single pane mode
            onFinishAnswerDialog(currentGameWord!!.get3LetterHint(), true)
        }
    }

    //endregion

    //region INTERFACE METHODS (PuzzleFragment.PuzzleListener) -------------------------------------

    /*
     * implements PuzzleListener interface for callback from PuzzleFragment
     */
    override fun onPuzzleClick(gameWord: GameWord?) {
        currentGameWord = gameWord
        puzzleRepresentation = puzzleFragment.puzzleRepresentation

        // update answer fragment with current game word
        answerFragment?.let {
            // dual pane mode
            it.setGameWord()
        } ?: run {
            // single pane mode
            if (!answerActivityLaunched) {
                val intent = Intent(this, AnswerActivity::class.java)
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
    override fun onFinishAnswerDialog(userText: String, confident: Boolean) {

        // in dual pane mode, this method may be called by answer dialog during setup (on text change)
        if (puzzleFragment.currentGameWord == null) {
            return
        }

        puzzleFragment.currentGameWord?.let {
            it.userText = userText.toUpperCase()
            it.isConfident = confident
            puzzleFragment.updateUserTextInPuzzle(it)

            // update database with answer
            Thread { dbHelper?.updateGameWordUserEntry(it) }.start()
        }

        // update error indications
        if (showingErrors) {
            showErrors(showingErrors)
        }

        // if puzzle is complete and correct
        if (puzzleFragment.isPuzzleComplete(true)) {

            // if game not already marked complete, do so now (note that user may not start new game after being prompted to do so)
            if (currentGame?.isGameComplete == false) {
                // save completion status to db
                dbHelper?.let {
                    try {
                        it.markGameComplete(currentGame)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error marking game complete: " + e.message)
                    }
                }
            }

            // prompt user with congrats and new game
            var extraMessage = resources.getString(R.string.dialog_startnewgame_congrats)
            dbHelper?.let {
                val gamesCompleted = it.gamesCompleted
                val wordsCompleted = it.wordCountOfGamesCompleted
                if (gamesCompleted > 1) {
                    extraMessage += "\n\n" + resources.getString(R.string.dialog_startnewgame_congrats2)
                            .replace("!games!", "" + gamesCompleted)
                            .replace("!words!", "" + wordsCompleted)
                }
            }
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

        // determine existing game no
        val newGameNo = (currentGame?.gameNo ?: 0) + 1

        // if dual pane, clear game word and hide answer fragment for now
        answerFragment?.let {
            it.clearGameWord()
            it.isVisible = false // set invisible until puzzle shows up
        }

        // clear puzzle fragment of existing game if any
        puzzleFragment.clearExistingGame()
        showErrors(false)

        // setup new game
        compositeDisposable.add(gameSetup.newGame(dbHelper!!, puzzleFragment.cellGrid, newGameNo)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { game ->
                            currentGame = game
                            createGrid()
                            retrieveNewDefinitions()
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
        currentGame = dbHelper?.currentGame

        // if on very first game, or if no saved game (due to an error), create a new one, otherwise open existing game
        if (currentGame?.gameWords == null || currentGame!!.gameWords.isEmpty() || !puzzleFragment.doWordsFitInGrid(currentGame!!.gameWords)) {
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
        for (gameWord in currentGame!!.gameWords) {
            gameSetup.addToGrid(gameWord, puzzleFragment.cellGrid)
        }
        createGrid()
    }


    /*
     * Handles completion of db setup.
     */
    private fun onFinishDbSetup() {
        if (progressDialog != null) {
            progressDialog!!.dismiss() // dismiss progress dialog
        }
        loadNewOrExistingGame()
    }

    /*
     * Handles progress updates during db setup.
     */
    private fun onProgressDbSetup(progress: Int) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this)
            progressDialog?.apply {
                setMessage("One time initialization...")
                isIndeterminate = false
                max = DbSetup.PROGRESS_RANGE
                setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                setCancelable(false)

                // show the progress dialog
                show()
                timeProgressDialogShown = Date().time
            }
        } else if (!helpShownYet && Date().time > timeProgressDialogShown + 4000) {
            // now that progress dialog has been up a little, show help dialog on top of it
            helpShownYet = true
            showHelpDialog()
        }

        // update progress dialog with progress
        progressDialog!!.progress = progress
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


    private fun createGrid() {
        puzzleFragment.createGrid(this)

        currentGameWord = puzzleFragment.currentGameWord
        puzzleRepresentation = puzzleFragment.puzzleRepresentation

        // if dual panel, update answer fragment with current game word
        if (answerFragment != null) {
            if (currentGameWord != null) { // this extra check is necessary for case where setting up initial game and no words available in db
                answerFragment!!.setGameWord()
                answerFragment!!.isVisible = true // set answer dialog fragment visible now that puzzle drawn
            }
        }
    }

    private fun retrieveNewDefinitions() {
        // Fetch a new set of definitions.
        if (isNetworkAvailable) {
            compositeDisposable.add(definitionRetrieval.retrieveDefinitions(dbHelper!!, 10)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ }) { e -> })
        }
    }

    //endregion

}



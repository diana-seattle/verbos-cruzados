package org.indiv.dls.games.verboscruzados

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import org.indiv.dls.games.verboscruzados.async.GameSetup
import org.indiv.dls.games.verboscruzados.databinding.ActivityMainBinding
import org.indiv.dls.games.verboscruzados.dialog.GameOptionsDialogFragment
import org.indiv.dls.games.verboscruzados.dialog.StatsDialogFragment
import org.indiv.dls.games.verboscruzados.game.GameWord
import org.indiv.dls.games.verboscruzados.game.PersistenceHelper
import kotlin.math.roundToInt


/*
 * Play Store: https://play.google.com/store/apps/details?id=org.indiv.dls.games.verboscruzados
 *
 * Image resources:
 * https://pixnio.com/nature-landscapes/deserts/desert-landscape-herb-canyon-dry-geology-mountain
 * https://pixabay.com/en/canyon-desert-sky-huge-mountains-311233/
 * https://www.pexels.com/photo/america-arid-bushes-california-221148/
 *
 * Font info:
 * Lato font: https://fonts.google.com/specimen/Lato
 * Font guide: https://developer.android.com/guide/topics/ui/look-and-feel/fonts-in-xml
 * System icons: https://developer.android.com/design/downloads/
 *
 * Conjugation resources:
 * https://www.e-spanyol.hu/en/grammar/irregular_ar.php
 */

// todo: review all code
// todo: add tests
// todo: view model / livedata - replace rxjava for game setup? also for SharedPreferences?
// todo: fix tablet pixel C api 30
// todo: fix on foldables

/**
 * This is the main activity. It houses [PuzzleFragment].
 */
class MainActivity : AppCompatActivity() {

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val KEYBOARD_ANIMATION_TIME = 150L
        private const val COUNTDOWN_MAX_TIME = 100000000000L // basically infinite
        private const val COUNTDOWN_INTERVAL = 1000L // one second
    }

    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private lateinit var binding: ActivityMainBinding

    private lateinit var viewModel: MainActivityViewModel

    private val gameSetup = GameSetup()
    private lateinit var puzzleFragment: PuzzleFragment

    private var optionsMenu: Menu? = null
    private lateinit var persistenceHelper: PersistenceHelper

    private var showOnboarding = false
    private var showingErrors = false

    private var elapsedTimerMs = 0L
    private val countDownTimer: CountDownTimer = object : CountDownTimer(COUNTDOWN_MAX_TIME, COUNTDOWN_INTERVAL) {
        override fun onTick(millisUntilFinished: Long) {
            elapsedTimerMs = COUNTDOWN_MAX_TIME - millisUntilFinished
            binding.answerKeyboard.elapsedTime = getElapsedTimeText(viewModel.elapsedGameSecondsRecorded + elapsedTimerMs / 1000)
        }

        override fun onFinish() {}
    }

    //endregion

    //region OVERRIDDEN METHODS --------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, MainActivityViewModel.Factory(this))
                .get(MainActivityViewModel::class.java)

        // Observe changes to the currently selected word.
        viewModel.currentGameWord.observe(this) { gameWord ->
            gameWord?.let {
                // Update keyboard with answer info, and make sure visible.
                binding.answerKeyboard.answerPresentation = createAnswerPresentation(it)
                showKeyboard()

                // Make sure entire word is visible.
                scrollWordIntoView()
            }
        }

        // Observe creation of new game
        viewModel.newlyCreatedGameWords.observe(this) { gameWords ->
            if (gameWords.isNotEmpty()) {
                puzzleFragment.createGridViews()
                scrollSelectedCellIntoViewWithDelay()
                binding.answerKeyboard.elapsedTime = getElapsedTimeText(0L)
                startTimer()
            } else {
                Toast.makeText(this, R.string.error_game_setup_failure, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error setting up game")
            }
        }


        persistenceHelper = PersistenceHelper(this)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        if (BuildConfig.BUILD_TYPE === "debug") {
            // This is a special debug-build-only hack that allows the developer/tester to complete a game immediately.
            binding.toolbar.setOnLongClickListener { _ ->
                do {
                    viewModel.currentGameWord.value?.let {
                        puzzleFragment.updateTextInPuzzleWord(it.word)
                        onAnswerChanged()
                    }
                } while (puzzleFragment.selectNextGameWordAfterCurrent(shouldSelectEmptyOnly = false))
                true
            }
        }

        // get puzzle fragment
        puzzleFragment = supportFragmentManager.findFragmentById(R.id.puzzle_fragment) as PuzzleFragment

        // position the keyboard off screen for animation when first shown.
        binding.answerKeyboard.translationY = viewModel.keyboardHeight

        if (viewModel.gridWidth > 0 && viewModel.gridHeight > 0) {
            puzzleFragment.initialize()
            loadNewOrExistingGame()
        }

        addKeyboardListeners()
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
            R.id.action_startnewgame -> promptForNewGame()
            R.id.action_help -> showHelpDialog()
            R.id.action_showstats -> showStatsDialog()
            R.id.action_showgameoptions -> showGameOptionsDialog()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onBackPressed() {
        if (isKeyboardVisible()) {
            hideKeyboard()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.elapsedGameSecondsRecorded = persistenceHelper.elapsedSeconds
        binding.answerKeyboard.elapsedTime = getElapsedTimeText(viewModel.elapsedGameSecondsRecorded)
        if (!persistenceHelper.currentGameCompleted) {
            startTimer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (!persistenceHelper.currentGameCompleted) {
            stopTimer()
            viewModel.elapsedGameSecondsRecorded += elapsedTimerMs / 1000
            persistenceHelper.elapsedSeconds = viewModel.elapsedGameSecondsRecorded
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        showingErrors = false
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    private fun addKeyboardListeners() {
        binding.answerKeyboard.infinitiveClickListener = {
            puzzleFragment.updateTextInPuzzleWord(it)
            onAnswerChanged()
            if (showOnboarding) {
                showOnboarding = false
                binding.onboardingMessageLayout.visibility = View.GONE
            }
        }
        binding.answerKeyboard.deleteLongClickListener = {
            puzzleFragment.updateTextInPuzzleWord("")
            onAnswerChanged()
        }
        binding.answerKeyboard.deleteClickListener = {
            val conflictingGameWord = puzzleFragment.deleteLetterInPuzzle()
            conflictingGameWord?.let {
                Thread { persistenceHelper.persistUserEntry(it) }.start()
            }
            onAnswerChanged()
        }
        binding.answerKeyboard.letterClickListener = { char ->
            val conflictingGameWord = puzzleFragment.updateLetterInPuzzle(char)
            conflictingGameWord?.let {
                Thread { persistenceHelper.persistUserEntry(it) }.start()
            }
            puzzleFragment.advanceSelectedCellInPuzzle(false)
            scrollSelectedCellIntoView()
            onAnswerChanged()
        }
        binding.answerKeyboard.leftClickListener = {
            puzzleFragment.advanceSelectedCellInPuzzle(true)
            scrollSelectedCellIntoView()
        }
        binding.answerKeyboard.rightClickListener = {
            puzzleFragment.advanceSelectedCellInPuzzle(false)
            scrollSelectedCellIntoView()
        }
        binding.answerKeyboard.nextWordClickListener = {
            selectNextGameWord()
        }
        binding.answerKeyboard.dismissClickListener = {
            hideKeyboard()
        }
    }

    private fun selectNextGameWord(): Boolean {
        return puzzleFragment.selectNextGameWordAfterCurrent(shouldSelectEmptyOnly = true)
                || puzzleFragment.selectNextGameWordAfterCurrent(shouldSelectEmptyOnly = false)
    }

    /**
     * Handles housekeeping after an answer has changed.
     */
    private fun onAnswerChanged() {

        // persist the user's answer
        viewModel.currentGameWord.value?.let {
            Thread { persistenceHelper.persistUserEntry(it) }.start()
        }

        val puzzleIsCompleteWithPossibleErrors = puzzleFragment.isPuzzleComplete(false)
        val puzzleIsCompleteAndCorrect = puzzleIsCompleteWithPossibleErrors && puzzleFragment.isPuzzleComplete(true)
        val puzzleIsCompleteWithErrors = puzzleIsCompleteWithPossibleErrors && !puzzleIsCompleteAndCorrect

        // update error indications
        if (showingErrors || puzzleIsCompleteWithErrors) {
            showErrors(true)

            // auto-advance to the next word when in error-showing mode (with a small delay so it feels less abrupt)
            if (viewModel.currentGameWord.value?.isAnsweredCompletelyAndCorrectly == true) {
                Handler(Looper.getMainLooper()).postDelayed({
                    selectNextGameWord()
                }, 200)
            }
        }

        scrollSelectedCellIntoView()

        if (puzzleIsCompleteAndCorrect) {

            // persist the game stats if this is the first time the current game has been completed (as opposed to modified and re-completed)
            if (!persistenceHelper.currentGameCompleted) {
                persistenceHelper.persistGameStats(viewModel.currentGameWords)
                persistenceHelper.currentGameCompleted = true
                stopTimer()
                viewModel.elapsedGameSecondsRecorded += elapsedTimerMs / 1000
                persistenceHelper.elapsedSeconds = viewModel.elapsedGameSecondsRecorded
            }

            // prompt with congrats and new game
            val completionRate = (viewModel.currentGameWords.size * 60f) / viewModel.elapsedGameSecondsRecorded
            val message = resources.getString(R.string.dialog_startnewgame_completion_message,
                    viewModel.currentGameWords.size, getElapsedTimeText(viewModel.elapsedGameSecondsRecorded), completionRate)
            promptForNewGame(message)
        }
    }

    private fun createAnswerPresentation(gameWord: GameWord): AnswerPresentation {
        return AnswerPresentation(gameWord.word, gameWord.isAcross, gameWord.conjugationTypeLabel,
                gameWord.subjectPronounLabel, gameWord.infinitive, gameWord.translation)
    }

    /*
     * show/hide errors in puzzle
     */
    private fun showErrors(showErrors: Boolean) {
        showingErrors = showErrors
        val showErrorsMenuItem = optionsMenu?.findItem(R.id.action_showerrors)
        showErrorsMenuItem?.setTitle(if (showingErrors) R.string.action_hideerrors else R.string.action_showerrors)
        showErrorsMenuItem?.setIcon(if (showingErrors) R.drawable.ic_baseline_visibility_24px else R.drawable.ic_baseline_visibility_off_24px)
        puzzleFragment.showErrors(showErrors)
    }

    private fun promptForNewGame(completionMessage: CharSequence? = null) {
        hideKeyboard()

        val titleResId = completionMessage?.let { R.string.dialog_startnewgame_prompt_after_winning }
                ?: R.string.dialog_startnewgame_prompt

        AlertDialog.Builder(this)
                .setTitle(titleResId)
                .setMessage(completionMessage)
                .setNeutralButton(R.string.dialog_startnewgame_game_options) { _, _ -> showGameOptionsDialog() }
                .setPositiveButton(R.string.dialog_startnewgame_yes) { _, _ ->
                    setPuzzleBackgroundImage(ImageSelecter.instance.getRandomImageIndex())
                    setupNewGame()
                }
                .setNegativeButton(R.string.dialog_startnewgame_no) { _, _ -> }
                .show()
    }

    /**
     * this is called when db setup completes
     */
    private fun loadNewOrExistingGame() {
        setPuzzleBackgroundImage(persistenceHelper.currentImageIndex)

        // get current game if any
        viewModel.reloadedGameWords.observe(this) { gameWords ->

            // if on very first game, or if no saved game (due to an error), create a new one, otherwise open existing game
            if (gameWords.isEmpty() || !puzzleFragment.doWordsFitInGrid(gameWords)) {
                setupNewGame()
                showOnboarding = true
            } else {
                // Apply the loaded game to the puzzle fragment
                gameWords.forEach {
                    gameSetup.addToGrid(it, viewModel.cellGrid)
                }
                puzzleFragment.createGridViews()
                scrollSelectedCellIntoViewWithDelay()
            }
        }
    }

    /**
     * Creates new game (called first time app run, or when user starts new game)
     */
    private fun setupNewGame() {
        hideKeyboard()

        // clear puzzle fragment of existing game if any
        puzzleFragment.clearExistingGame()
        showErrors(false)

        // setup new game
        viewModel.launchNewGame()
    }

    private fun isNightMode(): Boolean {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    private fun setPuzzleBackgroundImage(imageIndex: Int) {
        // If night mode, override the specified image with the nighttime image.
        val imageResourceId = if (isNightMode()) {
            R.drawable.scene_night
        } else {
            ImageSelecter.instance.getImageResId(imageIndex)
        }

        ResourcesCompat.getDrawable(resources, imageResourceId, null)?.let {
            binding.mainActivityContainerLayout.background = it
        }
        persistenceHelper.currentImageIndex = imageIndex
    }

    private fun scrollSelectedCellIntoViewWithDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            viewModel.currentGameWord.value?.let {
                if (it.isAcross) {
                    scrollWordIntoView()
                } else {
                    scrollSelectedCellIntoView()
                }
            }
        }, 50)
    }

    private fun scrollSelectedCellIntoView() {
        viewModel.currentGameWord.value?.let {
            if (!it.isAcross) {
                val rowOfSelectedCell = it.row + viewModel.charIndexOfSelectedCell
                val yOfSelectedCell = rowOfSelectedCell * viewModel.pixelsPerCell

                val heightForKeyboard = if (isKeyboardVisible()) viewModel.keyboardHeight else 0f
                val availableHeight = viewModel.viewablePuzzleHeight - heightForKeyboard

                // if cell above viewable area, scroll up to it, if below, scroll down to it
                if (yOfSelectedCell < puzzleFragment.scrollPosition) {
                    scrollWordIntoView(true)
                } else if (yOfSelectedCell + viewModel.pixelsPerCell > puzzleFragment.scrollPosition + availableHeight) {
                    scrollWordIntoView(false)
                }
            }
        }
    }

    private fun scrollWordIntoView(defaultToTop: Boolean = true) {
        viewModel.currentGameWord.value?.let {
            val firstRowPosition = it.row
            val lastRowPosition = when {
                it.isAcross -> it.row
                else -> it.row + it.word.length - 1
            }
            val yOfFirstCell = firstRowPosition * viewModel.pixelsPerCell

            val heightForKeyboard = if (isKeyboardVisible()) viewModel.keyboardHeight else 0f
            val availableHeight = viewModel.viewablePuzzleHeight - heightForKeyboard
            val wordHeight = (lastRowPosition - firstRowPosition + 1) * viewModel.pixelsPerCell

            // if there's room to display the whole word
            if (wordHeight < availableHeight) {
                // if first cell is above visible area, scroll up to it, or if last cell is below visible area, scroll down to it
                if (yOfFirstCell < puzzleFragment.scrollPosition) {
                    puzzleFragment.scrollPosition = (yOfFirstCell - viewModel.puzzleMarginTopPixels).roundToInt()
                } else if (yOfFirstCell + wordHeight > puzzleFragment.scrollPosition + availableHeight) {
                    puzzleFragment.scrollPosition = (yOfFirstCell + wordHeight - availableHeight + viewModel.puzzleMarginTopPixels).roundToInt()
                }
            } else {
                if (defaultToTop) {
                    // scroll top of word to top of viewable area
                    puzzleFragment.scrollPosition = (yOfFirstCell - viewModel.puzzleMarginTopPixels).roundToInt()
                } else {
                    // scroll bottom of word to bottom of viewable area
                    puzzleFragment.scrollPosition = (yOfFirstCell + wordHeight - availableHeight + viewModel.puzzleMarginTopPixels).roundToInt()
                }
            }
        }
    }

    private fun showHelpDialog() {
        hideKeyboard()
        AlertDialog.Builder(this)
                .setTitle(R.string.dialog_help_title)
                .setView(R.layout.fragment_help_dialog)
                .setPositiveButton(R.string.dialog_ok) { _, _ -> }
                .show()
    }

    private fun showStatsDialog() {
        hideKeyboard()
        val dlg = StatsDialogFragment()
        dlg.showGameOptionsListener = { showGameOptionsDialog() }
        dlg.show(supportFragmentManager, "fragment_showstats")
    }

    private fun showGameOptionsDialog() {
        hideKeyboard()
        val dlg = GameOptionsDialogFragment()
        dlg.startNewGameListener = {
            setPuzzleBackgroundImage(ImageSelecter.instance.getRandomImageIndex())
            setupNewGame()
        }
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
        return binding.answerKeyboard.visibility == View.VISIBLE
    }

    private fun showKeyboard() {
        if (!isKeyboardVisible()) {
            // Set visible, then animate up to position
            binding.answerKeyboard.visibility = View.VISIBLE

            // Delay very slightly so that animation is seen on app startup
            binding.answerKeyboard.postDelayed({
                ObjectAnimator.ofFloat(binding.answerKeyboard, "translationY", 0f)
                        .setDuration(KEYBOARD_ANIMATION_TIME)
                        .start()
            }, 1)

            if (showOnboarding) {
                binding.onboardingMessageLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun hideKeyboard() {
        if (isKeyboardVisible()) {
            // Animate off screen, then set invisible
            val animator = ObjectAnimator.ofFloat(binding.answerKeyboard, "translationY", viewModel.keyboardHeight)
                    .setDuration(KEYBOARD_ANIMATION_TIME)
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {
                    binding.answerKeyboard.visibility = View.INVISIBLE
                }

                override fun onAnimationEnd(animation: Animator?) {
                    binding.answerKeyboard.visibility = View.INVISIBLE
                }
            })
            animator.start()
            binding.onboardingMessageLayout.visibility = View.GONE
        }
    }

    private fun getElapsedTimeText(elapsedMs: Long): String {
        val minutes = elapsedMs / 60L
        val seconds = elapsedMs % 60L
        return "$minutes:${seconds.toString().padStart(2, '0')}"
    }

    private fun startTimer() {
        elapsedTimerMs = 0L
        countDownTimer.start()
    }

    private fun stopTimer() {
        countDownTimer.cancel()
    }

    //endregion

}



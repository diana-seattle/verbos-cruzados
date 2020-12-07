package org.indiv.dls.games.verboscruzados.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import org.indiv.dls.games.verboscruzados.BuildConfig
import org.indiv.dls.games.verboscruzados.viewmodel.MainActivityViewModel
import org.indiv.dls.games.verboscruzados.viewmodel.MainActivityViewModelFactory
import org.indiv.dls.games.verboscruzados.R
import org.indiv.dls.games.verboscruzados.databinding.ActivityMainBinding
import org.indiv.dls.games.verboscruzados.ui.dialog.GameOptionsDialogFragment
import org.indiv.dls.games.verboscruzados.ui.dialog.StatsDialogFragment
import org.indiv.dls.games.verboscruzados.util.ImageSelecter


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

// TODO: more tests
// TODO: hilt/dagger
// TODO: troubleshoot tablet pixel C api 30 emulator
// TODO: fix on foldables

/**
 * This is the main activity. It houses [PuzzleFragment].
 */
class MainActivity : AppCompatActivity() {

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val KEYBOARD_ANIMATION_TIME = 150L
        private const val COUNTDOWN_MAX_TIME = Long.MAX_VALUE // basically infinite
        private const val COUNTDOWN_INTERVAL = 1000L // one second
    }

    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private lateinit var binding: ActivityMainBinding

    @VisibleForTesting lateinit var viewModel: MainActivityViewModel

    private lateinit var puzzleFragment: PuzzleFragment

    private var optionsMenu: Menu? = null

    // This is elapsed milliseconds since we started the timer. It will be added to the overall saved duration for the game.
    private var elapsedTimerMs = 0L
    private val countDownTimer: CountDownTimer = object : CountDownTimer(COUNTDOWN_MAX_TIME, COUNTDOWN_INTERVAL) {
        override fun onTick(millisUntilFinished: Long) {
            elapsedTimerMs = COUNTDOWN_MAX_TIME - millisUntilFinished
            binding.answerKeyboard.elapsedTime = viewModel.getElapsedTimeText(viewModel.elapsedSecondsSnapshot + elapsedTimerMs / 1000)
        }
        override fun onFinish() {}
    }

    //endregion

    //region OVERRIDDEN METHODS --------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, MainActivityViewModelFactory(this))
                .get(MainActivityViewModel::class.java)

        // Observe changes to the currently selected word.
        viewModel.answerPresentation.observe(this) { answerPresentation ->
            answerPresentation?.let {
                // Update keyboard with answer info, and make sure visible.
                binding.answerKeyboard.answerPresentation = it
                showKeyboard()

                // Make sure entire word is visible.
                scrollWordIntoView()
            }
        }

        // Observe starting or loading of a game
        viewModel.gameStartOrLoadEvent.observe(this) {
            if (viewModel.wordCount > 0) {
                // Apply the game to the puzzle fragment
                puzzleFragment.createGridViewsAndSelectWord()
                scrollWordIntoViewWithDelay()
                showErrors(viewModel.showingErrors)
                if (!viewModel.currentGameCompleted) {
                    startTimer()
                }
            } else {
                Toast.makeText(this, R.string.error_game_setup_failure, Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error setting up game")
            }
        }

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        if (BuildConfig.BUILD_TYPE === "debug") {
            // This is a special debug-build-only hack that allows the developer/tester to complete a game immediately.
            binding.toolbar.setOnLongClickListener { _ ->
                do {
                    viewModel.selectedPuzzleWord.value?.let {
                        viewModel.updateTextOfSelectedWord(it.answer)
                        puzzleFragment.refreshTextOfSelectedWord()
                        onAnswerChanged()
                    }
                } while (viewModel.selectNextGameWordWithWrapAround(selectWordWithBlanks = false))
                true
            }
        }

        // get puzzle fragment
        puzzleFragment = supportFragmentManager.findFragmentById(R.id.puzzle_fragment) as PuzzleFragment

        // position the keyboard off screen for animation when first shown.
        binding.answerKeyboard.translationY = viewModel.keyboardHeight

        setPuzzleBackgroundImage(viewModel.currentImageIndex)

        if (viewModel.gridWidth > 0 && viewModel.gridHeight > 0) {
            // Attempt to load existing game, observer will create new one if not found.
            viewModel.loadGame()
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
            R.id.action_showerrors -> showErrors(!viewModel.showingErrors)
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
        binding.answerKeyboard.elapsedTime = viewModel.getElapsedTimeText(viewModel.persistedElapsedSeconds)
        if (!viewModel.currentGameCompleted) {
            startTimer()
        }
    }

    override fun onPause() {
        super.onPause()
        if (!viewModel.currentGameCompleted) {
            stopTimer()
            viewModel.addToElapsedSeconds(elapsedTimerMs / 1000)
        }
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    private fun addKeyboardListeners() {
        binding.answerKeyboard.infinitiveClickListener = { text ->
            viewModel.updateTextOfSelectedWord(text)
            puzzleFragment.refreshTextOfSelectedWord()
            onAnswerChanged()
            if (viewModel.showOnboardingMessage) {
                viewModel.showOnboardingMessage = false
                binding.onboardingMessageLayout.visibility = View.GONE
            }
        }
        binding.answerKeyboard.deleteLongClickListener = {
            viewModel.updateTextOfSelectedWord("")
            puzzleFragment.refreshTextOfSelectedWord()
            onAnswerChanged()
        }
        binding.answerKeyboard.deleteClickListener = {
            viewModel.updateCharOfSelectedCell(null)
            puzzleFragment.refreshCharOfSelectedCell()
            onAnswerChanged()
        }
        binding.answerKeyboard.letterClickListener = { char ->
            viewModel.updateCharOfSelectedCell(char)
            puzzleFragment.refreshCharOfSelectedCell()

            viewModel.advanceSelectedCellInPuzzle(inBackwardDirection = false)
            puzzleFragment.refreshStyleOfSelectedWord()
            scrollWordIntoView()
            onAnswerChanged()
        }
        binding.answerKeyboard.leftClickListener = {
            viewModel.advanceSelectedCellInPuzzle(inBackwardDirection = true)
            puzzleFragment.refreshStyleOfSelectedWord()
            scrollWordIntoView()
        }
        binding.answerKeyboard.rightClickListener = {
            viewModel.advanceSelectedCellInPuzzle(inBackwardDirection = false)
            puzzleFragment.refreshStyleOfSelectedWord()
            scrollWordIntoView()
        }
        binding.answerKeyboard.nextWordClickListener = {
            viewModel.selectNextGameWordFavoringIncomplete()
        }
        binding.answerKeyboard.dismissClickListener = {
            hideKeyboard()
        }
    }

    /**
     * Handles housekeeping after an answer has changed.
     */
    private fun onAnswerChanged() {
        val puzzleIsCompleteWithPossibleErrors = viewModel.isPuzzleComplete(false)
        val puzzleIsCompleteAndCorrect = puzzleIsCompleteWithPossibleErrors && viewModel.isPuzzleComplete(true)
        val puzzleIsCompleteWithErrors = puzzleIsCompleteWithPossibleErrors && !puzzleIsCompleteAndCorrect

        // update error indications
        if (viewModel.showingErrors || puzzleIsCompleteWithErrors) {
            showErrors(true)

            // auto-advance to the next word when in error-showing mode (with a small delay so it feels less abrupt)
            if (viewModel.isCurrentGameWordAnsweredCompletelyAndCorrectly()) {
                Handler(Looper.getMainLooper()).postDelayed({
                    viewModel.selectNextGameWordFavoringIncomplete()
                }, 200)
            }
        }

        scrollWordIntoView()

        if (puzzleIsCompleteAndCorrect) {

            // persist the game stats if this is the first time the current game has been completed (as opposed to modified and re-completed)
            if (!viewModel.currentGameCompleted) {
                viewModel.persistGameStatistics()
                viewModel.currentGameCompleted = true
                stopTimer()
                viewModel.addToElapsedSeconds(elapsedTimerMs / 1000)
            }

            // prompt with congrats and new game
            val message = resources.getString(R.string.dialog_startnewgame_completion_message,
                    viewModel.wordCount,
                    viewModel.getElapsedTimeText(viewModel.elapsedSecondsSnapshot),
                    viewModel.calculateCompletionRate())
            promptForNewGame(message)
        }
    }

    /*
     * show/hide errors in puzzle
     */
    private fun showErrors(showErrors: Boolean) {
        viewModel.showingErrors = showErrors
        val showErrorsMenuItem = optionsMenu?.findItem(R.id.action_showerrors)
        showErrorsMenuItem?.setTitle(if (viewModel.showingErrors) R.string.action_hideerrors else R.string.action_showerrors)
        showErrorsMenuItem?.setIcon(if (viewModel.showingErrors) R.drawable.ic_baseline_visibility_24px else R.drawable.ic_baseline_visibility_off_24px)
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
     * Creates new game (called first time app run, or when user starts new game)
     */
    private fun setupNewGame() {
        hideKeyboard()

        // clear puzzle fragment of existing game if any
        viewModel.clearGame()
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
        viewModel.currentImageIndex = imageIndex
    }

    private fun scrollWordIntoViewWithDelay() {
        Handler(Looper.getMainLooper()).postDelayed(this::scrollWordIntoView, 50)
    }

    private fun scrollWordIntoView() {
        viewModel.newScrollPositionShowingFullWord(puzzleFragment.scrollPosition)?.let {
            puzzleFragment.scrollPosition = it
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

            if (viewModel.showOnboardingMessage) {
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

    private fun startTimer() {
        elapsedTimerMs = 0L
        countDownTimer.start()
    }

    private fun stopTimer() {
        countDownTimer.cancel()
    }

    //endregion

}



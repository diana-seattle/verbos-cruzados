package org.indiv.dls.games.vocabrecall.feature;

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


import java.util.Date;

import org.indiv.dls.games.vocabrecall.feature.async.DbSetup;
import org.indiv.dls.games.vocabrecall.feature.async.DefinitionRetrieval;
import org.indiv.dls.games.vocabrecall.feature.async.GameSetup;
import org.indiv.dls.games.vocabrecall.feature.db.ContentHelper;
import org.indiv.dls.games.vocabrecall.feature.db.Game;
import org.indiv.dls.games.vocabrecall.feature.db.GameWord;
import org.indiv.dls.games.vocabrecall.feature.dialog.ConfirmStartNewGameDialogFragment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class VocabRecallActivity extends MyActionBarActivity
        implements ConfirmStartNewGameDialogFragment.StartNewGameDialogListener,
        AnswerFragment.DualPaneAnswerListener,
        PuzzleFragment.PuzzleListener {

    //region STATIC LOCAL CONSTANTS ----------------------------------------------------------------

    private static final String TAG = VocabRecallActivity.class.getSimpleName();
    private static final int RESULTCODE_ANSWER = 100;
    public static final String ACTIVITYRESULT_ANSWER = "answer";
    public static final String ACTIVITYRESULT_CONFIDENT = "confident";

    //endregion

    //region CLASS VARIABLES -----------------------------------------------------------------------

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private Game mGame;
    private GameSetup mGameSetup = new GameSetup();
    private DbSetup mDbSetup = new DbSetup();
    private DefinitionRetrieval mDefinitionRetrieval = new DefinitionRetrieval();
    private PuzzleFragment mPuzzleFragment;
    private AnswerFragment mAnswerFragment; // for use in panel
    private boolean mAnswerActivityLaunched = false; // use this to load activity only once when puzzle double clicked on

    private ProgressDialog mProgressDialog;
    private long timeProgressDialogShown = 0;
    private boolean mHelpShownYet = false;

    protected static boolean sShowingErrors = false;

    //endregion

    //region OVERRIDDEN METHODS --------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabrecall);

        // Set up toolbar
        setToolbar(findViewById(R.id.toolbar));
        setSupportActionBar(getToolbar());
        if (BuildConfig.BUILD_TYPE == "debug") {
            getToolbar().setOnLongClickListener(v -> {
                Integer startingRow = mPuzzleFragment.getCurrentGameWord().getRow();
                do {
                    Companion.setSCurrentGameWord(mPuzzleFragment.getCurrentGameWord());
                    String answer = Companion.getSCurrentGameWord().getWord().toLowerCase();
                    onFinishAnswerDialog(answer, true);
                } while ((startingRow = mPuzzleFragment.selectNextErroredGameWord(startingRow)) != null);
                return true;
            });
        }

        Resources r = getResources();

        DisplayMetrics displayMetrics = r.getDisplayMetrics();

        // get puzzle fragment
        mPuzzleFragment = (PuzzleFragment) getSupportFragmentManager().findFragmentById(R.id.puzzle_fragment);

        // get answer fragment if present (this will be found only in dual pane mode)
        mAnswerFragment = (AnswerFragment) getSupportFragmentManager().findFragmentById(R.id.answer_fragment);
        int puzzleWidthPixels = 0;
        int puzzleHeightPixels = 0;

        // get action bar height
        int actionBarHeightInPixels = getActionBarHeightInPixels(displayMetrics);


        // if answer fragment present (dual pane mode), use landscape orientation
        if (mAnswerFragment != null) {
            mAnswerFragment.setVisible(false); // set invisible until puzzle shows up

            // this allows screen to rotate 180deg in landscape mode
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

            // determine puzzle dimensions
            int answerPanelWidthPixels = Math.round(r.getDimension(R.dimen.fragment_answer_width));
            puzzleWidthPixels = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels) - answerPanelWidthPixels;
            puzzleHeightPixels = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels) - actionBarHeightInPixels;
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            // determine puzzle dimensions
            puzzleWidthPixels = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
            puzzleHeightPixels = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels) - actionBarHeightInPixels;
        }

        // initialize puzzle fragment after setting orientation so it knows its size
        mPuzzleFragment.initialize(puzzleWidthPixels, puzzleHeightPixels);


        // get database
        Companion.setSDbHelper(new ContentHelper(this));
        mCompositeDisposable.add(mDbSetup.ensureDbLoaded(this, Companion.getSDbHelper())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onProgressDbSetup,
                        error -> Toast.makeText(this, R.string.error_initial_setup_failure, Toast.LENGTH_SHORT).show(),
                        this::onFinishDbSetup
                ));

        // Note that database not set up yet at this point (happening in other thread).
        // When it completes, it will call DbSetup().
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_options, menu);
        setOptionsMenu(menu);
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();

        // reset static variables since these may stick around after activity destroyed
        Companion.setSPuzzleRepresentation(null);
        Companion.setSCurrentGameWord(null);
        Companion.setSDbHelper(null);
        sShowingErrors = false;
        Companion.setSDbSetupComplete(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);

        // if response from answer activity
        if (requestCode == RESULTCODE_ANSWER) {
            if (resultCode == Activity.RESULT_OK) {
                String userText = result.getStringExtra(VocabRecallActivity.ACTIVITYRESULT_ANSWER);
                boolean confident = result.getBooleanExtra(VocabRecallActivity.ACTIVITYRESULT_CONFIDENT, false);
                onFinishAnswerDialog(userText, confident);
            }
            mAnswerActivityLaunched = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        int i = item.getItemId();
        if (i == R.id.action_showerrors) {
            showErrors(!sShowingErrors);
            return true;
        } else if (i == R.id.action_startnewgame) {
            promptForNewGame(null);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /*
     * overriding to display answer
     */
    @Override
    protected void giveAnswer() {
        super.giveAnswer();
        if (mAnswerFragment != null) { // dual pane mode
            mAnswerFragment.giveAnswer();
        } else { // else single pane mode and answer activity not visible
            onFinishAnswerDialog(Companion.getSCurrentGameWord().getWord(), true);
        }
    }

    /*
     * overriding to display hint
     */
    @Override
    protected void give3LetterHint() {
        super.give3LetterHint();
        if (mAnswerFragment != null) { // dual pane mode
            mAnswerFragment.give3LetterHint();
        } else { // else single pane mode and answer activity not visible
            onFinishAnswerDialog(Companion.getSCurrentGameWord().get3LetterHint(), true);
        }
    }

    //endregion

    //region INTERFACE METHODS (PuzzleFragment.PuzzleListener) -------------------------------------

    /*
     * implements PuzzleListener interface for callback from PuzzleFragment
     */
    @Override
    public void onPuzzleClick(GameWord gameWord) {
        Companion.setSCurrentGameWord(gameWord);
        Companion.setSPuzzleRepresentation(mPuzzleFragment.getPuzzleRepresentation());

        if (mAnswerFragment != null) {
            mAnswerFragment.setGameWord(); // update answer fragment with current game word
        } else {
            if (!mAnswerActivityLaunched) {
                Intent intent = new Intent(this, AnswerActivity.class);
                startActivityForResult(intent, RESULTCODE_ANSWER);
                mAnswerActivityLaunched = true;
            }
        }
    }

    //endregion

    //region INTERFACE METHODS (AnswerFragment.DualPaneAnswerListener) -----------------------------

    /*
     * implements interface for receiving callback from AnswerFragment
     */
    @Override
    public void onFinishAnswerDialog(String userText, boolean confident) {

        // in dual pane mode, this method may be called by answer dialog during setup (on text change)
        if (mPuzzleFragment == null || mPuzzleFragment.getCurrentGameWord() == null) {
            return;
        }

        final GameWord currentGameWord = mPuzzleFragment.getCurrentGameWord();
        currentGameWord.setUserText(userText.toUpperCase());
        currentGameWord.setConfident(confident);
        mPuzzleFragment.updateUserTextInPuzzle(currentGameWord);

        // update database with answer
        new Thread(() -> Companion.getSDbHelper().updateGameWordUserEntry(currentGameWord)).start();

        // update error indications
        if (sShowingErrors) {
            showErrors(sShowingErrors);
        }

        // if puzzle is complete and correct
        if (mPuzzleFragment.isPuzzleComplete(true)) {

            // if game not already marked complete, do so now (note that user may not start new game after being prompted to do so)
            if (!mGame.isGameComplete()) {

                // save completion status to db
                try {
                    Companion.getSDbHelper().markGameComplete(mGame);
                } catch (Exception e) {
                    Log.e(TAG, "error marking game complete: " + e.getMessage());
                }

                // update stats
                Companion.setSGamesCompleted(Companion.getSDbHelper().getGamesCompleted());
                Companion.setSWordsCompleted(Companion.getSDbHelper().getWordCountOfGamesCompleted());
            }

            // prompt user with congrats and new game
            String extraMessage = getResources().getString(R.string.dialog_startnewgame_congrats);
            if (Companion.getSGamesCompleted() > 1) {
                extraMessage += "\n\n" + getResources().getString(R.string.dialog_startnewgame_congrats2).replace("!games!", "" + Companion.getSGamesCompleted()).replace("!words!", "" + Companion.getSWordsCompleted());
            }
            promptForNewGame(extraMessage);

            // else if puzzle is complete but not correct, show errors
        } else if (!sShowingErrors && mPuzzleFragment.isPuzzleComplete(false)) {
            showErrors(true);
        }

    }

    //endregion

    //region INTERFACE METHODS (ConfirmStartNewGameDialogFragment.StartNewGameDialogListener) ------

    /**
     * create new game (called first time app run, or when user starts new game)
     * implements interface for receiving callback from ConfirmStartNewGameDialogFragment
     */
    @Override
    public void setupNewGame() {

        // determine existing game no
        final int newGameNo = mGame != null ? mGame.getGameNo() + 1 : 1;

        // if dual pane, clear game word and hide answer fragment for now
        if (mAnswerFragment != null) {
            mAnswerFragment.clearGameWord();
            mAnswerFragment.setVisible(false); // set invisible until puzzle shows up
        }

        // clear puzzle fragment of existing game if any
        mPuzzleFragment.clearExistingGame();
        showErrors(false);

        // setup new game
        mCompositeDisposable.add(mGameSetup.newGame(Companion.getSDbHelper(), mPuzzleFragment.getCellGrid(), newGameNo)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(game -> {
                    mGame = game;
                    createGrid();
                    retrieveNewDefinitions();
                }, error -> {
                    Toast.makeText(this, R.string.error_game_setup_failure, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error setting up game: " + error.getMessage());
                }));
    }

    //endregion

    //region PUBLIC CLASS METHODS ------------------------------------------------------------------
    //endregion

    //region PRIVATE METHODS -----------------------------------------------------------------------

    /*
     * show/hide errors in puzzle
     */
    private void showErrors(boolean showErrors) {
        sShowingErrors = showErrors;
        setOptionsMenuText(R.id.action_showerrors, sShowingErrors ? R.string.action_hideerrors : R.string.action_showerrors);
        mPuzzleFragment.showErrors(showErrors);
    }

    private void promptForNewGame(String extraMessage) {
        ConfirmStartNewGameDialogFragment dlg = new ConfirmStartNewGameDialogFragment();
        if (extraMessage != null) {
            dlg.setExtraMessage(extraMessage);
        }
        dlg.showDlg((ConfirmStartNewGameDialogFragment.StartNewGameDialogListener) this,
                getSupportFragmentManager(), "fragment_startnewgame");
    }

    /**
     * this is called when db setup completes
     */
    private void loadNewOrExistingGame() {

        // get current game if any
        mGame = Companion.getSDbHelper().getCurrentGame();

        // if on very first game, or if no saved game (due to an error), create a new one, otherwise open existing game
        if (mGame == null || mGame.getGameWords() == null || mGame.getGameWords().size() == 0 || !mPuzzleFragment.doWordsFitInGrid(mGame.getGameWords())) {
            setupNewGame();
        } else {
            restoreExistingGame();
            if (mGame.getGameNo() > 1) {
                // if any games won yet, update stats
                Companion.setSGamesCompleted(Companion.getSDbHelper().getGamesCompleted());
                if (Companion.getSGamesCompleted() > 0) {
                    Companion.setSWordsCompleted(Companion.getSDbHelper().getWordCountOfGamesCompleted());
                }
            }
        }
    }


    /**
     * open existing game
     */
    public void restoreExistingGame() {
        // copy game words to cell grid
        for (GameWord gameWord : mGame.getGameWords()) {
            mGameSetup.addToGrid(gameWord, mPuzzleFragment.getCellGrid());
        }
        createGrid();
    }


    /*
     * Handles completion of db setup.
     */
    public void onFinishDbSetup() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss(); // dismiss progress dialog
        }
        Companion.setSDbSetupComplete(true);
        loadNewOrExistingGame();
    }

    /*
     * Handles progress updates during db setup.
     */
    public void onProgressDbSetup(Integer progress) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("One time initialization...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(DbSetup.PROGRESS_RANGE);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);

            // show the progress dialog
            mProgressDialog.show();
            timeProgressDialogShown = new Date().getTime();

        } else if (!mHelpShownYet && new Date().getTime() > timeProgressDialogShown + 4000) {
            // now that progress dialog has been up a little, show help dialog on top of it
            mHelpShownYet = true;
            showHelpDialog();
        }

        // update progress dialog with progress
        mProgressDialog.setProgress(progress);
    }


    // note that with api level 13 and above we can use getResources().getConfiguration().screenHeightDp/screenWidthDp to get available screen size
    private int getActionBarHeightInPixels(DisplayMetrics displayMetrics) {
        // actionBar.getHeight() returns zero in onCreate (i.e. before it is shown)
        // for the following solution, see: http://stackoverflow.com/questions/12301510/how-to-get-the-actionbar-height/13216807#13216807
        int actionBarHeight = 0;  // actionBar.getHeight() returns zero in onCreate
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, displayMetrics);
        }
        return actionBarHeight;
    }


    private void createGrid() {
        mPuzzleFragment.createGrid();

        Companion.setSCurrentGameWord(mPuzzleFragment.getCurrentGameWord());
        Companion.setSPuzzleRepresentation(mPuzzleFragment.getPuzzleRepresentation());

        // if dual panel, update answer fragment with current game word
        if (mAnswerFragment != null) {
            if (Companion.getSCurrentGameWord() != null) { // this extra check is necessary for case where setting up initial game and no words available in db
                mAnswerFragment.setGameWord();
                mAnswerFragment.setVisible(true); // set answer dialog fragment visible now that puzzle drawn
            }
        }
    }

    private void retrieveNewDefinitions() {
        // Fetch a new set of definitions.
        if (isNetworkAvailable()) {
            mCompositeDisposable.add(mDefinitionRetrieval.retrieveDefinitions(Companion.getSDbHelper(), 10)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                    }, e -> {
                    }));
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null, otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    //endregion

}



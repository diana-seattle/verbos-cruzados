package org.indiv.dls.games.vocabrecall.feature;

import java.util.List;

import org.indiv.dls.games.vocabrecall.feature.db.ContentHelper;
import org.indiv.dls.games.vocabrecall.feature.db.GameWord;
import org.indiv.dls.games.vocabrecall.feature.dialog.ConfirmStartNewGameDialogFragment;
import org.indiv.dls.games.vocabrecall.feature.dialog.HelpDialogFragment;
import org.indiv.dls.games.vocabrecall.feature.dialog.StatsDialogFragment;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public abstract class MyActionBarActivity extends AppCompatActivity {

    // some global static variables
    public static GameWord sCurrentGameWord;
    public static List<TextView> sPuzzleRepresentation;
    protected static ContentHelper sDbHelper;
    protected static int sGamesCompleted = 0;
    protected static int sWordsCompleted = 0;
    protected static boolean sShowingErrors = false;
    protected static boolean sDbSetupComplete = false;


    protected Menu mOptionsMenu;
    protected Toolbar mToolbar;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (sCurrentGameWord != null) {
            Resources r = getResources();

            // modify menu items
            MenuItem menuItemMiniClue = mOptionsMenu.findItem(R.id.action_give3letters);
            if (menuItemMiniClue != null) {
                menuItemMiniClue.setTitle(r.getString(R.string.action_give3letters) + sCurrentGameWord.getGame().getMiniCluesMenuText());
                if (!sCurrentGameWord.getGame().isMiniClueRemaining()) {
                    menuItemMiniClue.setEnabled(false);
                }
            }

            MenuItem menuItemFullAnswer = mOptionsMenu.findItem(R.id.action_giveanswer);
            if (menuItemFullAnswer != null) {
                menuItemFullAnswer.setTitle(r.getString(R.string.action_giveanswer) + sCurrentGameWord.getGame().getFullCluesMenuText());
                if (!sCurrentGameWord.getGame().isFullClueRemaining()) {
                    menuItemFullAnswer.setEnabled(false);
                }
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        int i = item.getItemId();
        if (i == android.R.id.home) {// in response to back button on answer activity, close answer activity
            // (back button enabled by calling getSupportActionBar().setDisplayHomeAsUpEnabled(true) in Activity.onCreate, see http://stackoverflow.com/questions/10108774/android-actionbar-back-button)
            setResult(Activity.RESULT_CANCELED);
            finish();
            return true;
        } else if (i == R.id.action_showerrors) {
            showErrors(!sShowingErrors);
            return true;
        } else if (i == R.id.action_startnewgame) {
            promptForNewGame(null);
            return true;
        } else if (i == R.id.action_help) {
            showHelpDialog();
            return true;
        } else if (i == R.id.action_showstats) {
            showStatsDialog();
            return true;
        } else if (i == R.id.action_give3letters) {
            give3LetterHint();
            return true;
        } else if (i == R.id.action_giveanswer) {
            giveAnswer();
            return true;
        } else if (i == R.id.action_playagainsoon) {//				MyActionBarActivity.sDbHelper.setWordPlaySoon(sCurrentGameWord.getWord(), true);
            new Thread(() -> MyActionBarActivity.sDbHelper.setWordPlaySoon(sCurrentGameWord.getWord(), true)).start();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    /*
     * override to show/hide errors in puzzle
     */
    protected void showErrors(boolean showErrors) {
        sShowingErrors = showErrors;
        // set menu text to opposite of what we're currently doing
        setOptionsMenuText(R.id.action_showerrors, sShowingErrors ? R.string.action_hideerrors : R.string.action_showerrors);
    }

    /*
     * override to display answer
     */
    protected void giveAnswer() {
        sCurrentGameWord.getGame().setFullClues(sCurrentGameWord.getGame().getFullClues() + 1);
        new Thread(() -> MyActionBarActivity.sDbHelper.saveFullClues(sCurrentGameWord.getGame())).start();
        // subclass handles the rest
    }

    /*
     * override to display 3 letter hint
     */
    protected void give3LetterHint() {
        sCurrentGameWord.getGame().setMiniClues(sCurrentGameWord.getGame().getMiniClues() + 1);
        new Thread(() -> MyActionBarActivity.sDbHelper.saveMiniClues(sCurrentGameWord.getGame())).start();
        // subclass handles the rest
    }


    protected void setOptionsMenuText(int menuItemId, String text) {
        mOptionsMenu.findItem(menuItemId).setTitle(text);
    }

    protected void setOptionsMenuText(int menuItemId, int textId) {
        if (mOptionsMenu != null) {
            mOptionsMenu.findItem(menuItemId).setTitle(textId);
        }
    }

    public void showHelpDialog() {
        new HelpDialogFragment().show(getSupportFragmentManager(), "fragment_showhelp");
    }

    protected void promptForNewGame(String extraMessage) {
        ConfirmStartNewGameDialogFragment dlg = new ConfirmStartNewGameDialogFragment();
        if (extraMessage != null) {
            dlg.setExtraMessage(extraMessage);
        }
        dlg.showDlg((ConfirmStartNewGameDialogFragment.StartNewGameDialogListener) this,
                getSupportFragmentManager(), "fragment_startnewgame");
    }

    // ----- private methods -------//

    private void showStatsDialog() {
        ContentHelper.WordsSolvedStats stats = sDbHelper.getWordsSolvedStats();
        StatsDialogFragment dlg = new StatsDialogFragment();
        dlg.setStats(sGamesCompleted, sWordsCompleted, stats);
        dlg.show(getSupportFragmentManager(), "fragment_showstats");
    }

}

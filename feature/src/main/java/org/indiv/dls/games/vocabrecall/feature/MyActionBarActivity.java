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

    //region CLASS VARIABLES -----------------------------------------------------------------------

    // some static variables
    public static GameWord sCurrentGameWord;
    public static List<TextView> sPuzzleRepresentation;
    protected static ContentHelper sDbHelper;
    protected static int sGamesCompleted = 0;
    protected static int sWordsCompleted = 0;
    protected static boolean sDbSetupComplete = false;

    protected Menu mOptionsMenu;
    protected Toolbar mToolbar;

    //endregion

    //region OVERRIDDEN METHODS --------------------------------------------------------------------

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
        if (i == R.id.action_help) {
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

    //endregion

    //region PUBLIC CLASS METHODS ------------------------------------------------------------------
    //endregion

    //region PROTECTED CLASS METHODS ---------------------------------------------------------------

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

    protected void setOptionsMenuText(int menuItemId, int textId) {
        if (mOptionsMenu != null) {
            mOptionsMenu.findItem(menuItemId).setTitle(textId);
        }
    }

    protected void showHelpDialog() {
        new HelpDialogFragment().show(getSupportFragmentManager(), "fragment_showhelp");
    }

    //endregion

    //region PRIVATE METHODS -----------------------------------------------------------------------

    private void showStatsDialog() {
        ContentHelper.WordsSolvedStats stats = sDbHelper.getWordsSolvedStats();
        StatsDialogFragment dlg = new StatsDialogFragment();
        dlg.setStats(sGamesCompleted, sWordsCompleted, stats);
        dlg.show(getSupportFragmentManager(), "fragment_showstats");
    }

    //endregion

}

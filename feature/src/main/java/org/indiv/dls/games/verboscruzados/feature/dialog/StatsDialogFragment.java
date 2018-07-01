package org.indiv.dls.games.verboscruzados.feature.dialog;

import org.indiv.dls.games.verboscruzados.feature.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;
import android.widget.FrameLayout;
import android.widget.TextView;

public class StatsDialogFragment extends DialogFragment {

    private int mGamesCompleted;
    private int mWordsCompleted;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Dialog)); // wrap activity with ContextThemeWrapper to get better dialog styling
        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) { /* nothing to do but close */ }
        });
        final FrameLayout frameView = new FrameLayout(getActivity());
        builder.setView(frameView);

        // create the AlertDialog object 
        Dialog dialog = builder.create();
        dialog.getLayoutInflater().inflate(R.layout.fragment_stats_dialog, frameView);

        // fill in stats
        appendTextToView((TextView) frameView.findViewById(R.id.textview_gamescompleted), " " + mGamesCompleted);
        appendTextToView((TextView) frameView.findViewById(R.id.textview_wordscompleted), " " + mWordsCompleted);

        return dialog;
    }


    //---------------------------------------------------//
    //------------ end of overridden methods ------------//
    //---------------------------------------------------//


    public void setStats(int gamesCompleted, int wordsCompleted) {
        mGamesCompleted = gamesCompleted;
        mWordsCompleted = wordsCompleted;
    }

    private void appendTextToView(TextView textView, String text) {
        textView.setText(textView.getText() + text);
    }
}

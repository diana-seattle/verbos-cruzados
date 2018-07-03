package org.indiv.dls.games.verboscruzados.feature.dialog;

import org.indiv.dls.games.verboscruzados.feature.R;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class StatsDialogFragment extends DialogFragment {

    private int mGamesCompleted;
    private int mWordsCompleted;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_stats_dialog, null);

        // use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.dialog_ok, (dialog, id) -> { })
                .setView(view);

        // create the AlertDialog object 
        Dialog dialog = builder.create();

        // fill in stats
        appendTextToView(view.findViewById(R.id.textview_gamescompleted), " " + mGamesCompleted);
        appendTextToView(view.findViewById(R.id.textview_wordscompleted), " " + mWordsCompleted);

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

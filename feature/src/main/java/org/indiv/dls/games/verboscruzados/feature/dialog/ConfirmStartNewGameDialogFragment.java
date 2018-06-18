package org.indiv.dls.games.verboscruzados.feature.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.ContextThemeWrapper;
import android.widget.TextView;

import org.indiv.dls.games.verboscruzados.feature.R;

public class ConfirmStartNewGameDialogFragment extends DialogFragment {

    //region PUBLIC INTERFACES ---------------------------------------------------------------------

    // interface for activity to implement to receive result
    public interface StartNewGameDialogListener {
        void setupNewGame();
    }

    //endregion

    //region CLASS VARIABLES -----------------------------------------------------------------------

    private String mExtraMessage;
    private StartNewGameDialogListener mStartNewGameDialogListener;

    //endregion

    //region OVERRIDEN METHODS ---------------------------------------------------------------------

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Dialog)); // wrap activity with ContextThemeWrapper to get better dialog styling
        String message = ((mExtraMessage != null) ? (mExtraMessage + "\n\n") : "") + getResources().getString(R.string.dialog_startnewgame_prompt);
        builder.setMessage(message)
                .setPositiveButton(R.string.dialog_startnewgame_yes,
                        (dialog, id) -> mStartNewGameDialogListener.setupNewGame())
                .setNegativeButton(R.string.dialog_startnewgame_no,
                        (dialog, id) -> { /* nothing to do */ });
        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        // increase font size of message (after view created or else textview will be null)
        Dialog dlg = getDialog();
        if (dlg != null) {
            TextView textView = dlg.findViewById(android.R.id.message);
            if (textView != null) {
                textView.setTextSize(22);
            }
        }
    }

    //endregion

    //region PUBLIC CLASS METHODS ------------------------------------------------------------------

    // set extra message to include with dialog
    public void setExtraMessage(String extraMessage) {
        mExtraMessage = extraMessage;
    }


    public void showDlg(StartNewGameDialogListener listener, FragmentManager supportFragmentManager, String tag) {
        mStartNewGameDialogListener = listener;
        show(supportFragmentManager, tag);
    }

    //endregion

    //region PRIVATE METHODS -----------------------------------------------------------------------
    //endregion

}

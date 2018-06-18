package org.indiv.dls.games.verboscruzados.feature.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;
import android.widget.FrameLayout;

import org.indiv.dls.games.verboscruzados.feature.R;

public class HelpDialogFragment extends DialogFragment {

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
        AlertDialog dialog = builder.create();
        dialog.getLayoutInflater().inflate(R.layout.fragment_help_dialog, frameView);

        return dialog;
    }

}

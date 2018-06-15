package org.indiv.dls.games.vocabrecall.feature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

// see: http://android-developers.blogspot.com/2012/05/using-dialogfragments.html

public class AnswerFragment extends Fragment {

    //region PUBLIC INTERFACES ---------------------------------------------------------------------

    // interface for activity to implement to receive result
    public interface DualPaneAnswerListener {
        void onFinishAnswerDialog(String userText, boolean confident);
    }

    //endregion

    //region STATIC LOCAL CONSTANTS ----------------------------------------------------------------

    private static final String PREFERENCE_KEY_FONT = "fontSize";
    private static final int FONT_EXTRA_SMALL = 16; // in SP
    private static final int FONT_SMALL = 19;
    private static final int FONT_MEDIUM = 22;
    private static final int FONT_LARGE = 26;
    private static final int FONT_EXTRA_LARGE = 30;

    private final static int COLOR_ANSWER = 0xFF0099cc;  // a little darker than puzzle background

    //endregion

    //region CLASS VARIABLES -----------------------------------------------------------------------

    private Menu mOptionsMenu;


    private EditText mTextEditorAnswer;
    private TextView mTextViewLetterCount;
    private LinearLayout mPuzzleRepresentationLayout;
    private HorizontalScrollView mPuzzleScrollView;
    private String mWord;
    private String mWordHint;
    private int mWordLength;
    private View mFragmentView; // for some reason getView() sometimes returns null, so hold onto a copy of the view
    private DualPaneAnswerListener mDualPaneAnswerListener;
    private Activity mActivity;
    private ScrollView mScrollViewDefinitions;

    //endregion

    //region OVERRIDDEN METHODS --------------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // by inflating the view in onCreateView instead of onCreateDialog, the view can be
        // displayed either as a dialog or as a fragment
        mFragmentView = inflater.inflate(R.layout.fragment_answer, container);

        // keep a copy of this because sometimes getActivity() returns null
        mActivity = getActivity();
        if (mActivity instanceof DualPaneAnswerListener) {
            mDualPaneAnswerListener = (DualPaneAnswerListener) getActivity();
        }

        // enable to add font submenu item
        setHasOptionsMenu(true);

        // puzzle representation
        mPuzzleScrollView = mFragmentView.findViewById(R.id.puzzle_representation_scrollview);
        mPuzzleRepresentationLayout = mFragmentView.findViewById(R.id.puzzle_representation);
        mPuzzleRepresentationLayout.setOnClickListener(v -> showSoftKeyboardForAnswer());

        // text editor
        mTextEditorAnswer = mFragmentView.findViewById(R.id.txt_answer);
        mTextEditorAnswer.setTextColor(COLOR_ANSWER);

        mTextEditorAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateLetterCount();
                updatePuzzleRepresentation();
            }
        });
        mTextEditorAnswer.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // doing this on text change results in keyboard being prematurely dismissed
                updateDualPaneActivityWithAnswer(getUserEntry(), true);
            }
            return false;
        });

        // get letter count text view  
        mTextViewLetterCount = mFragmentView.findViewById(R.id.lbl_letter_count);


        // confirmation buttons
        Button buttonTentative = mFragmentView.findViewById(R.id.button_tentative);
        buttonTentative.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateActivityWithAnswer(false);
            }
        });
        Button buttonConfident = mFragmentView.findViewById(R.id.button_confident);
        buttonConfident.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateActivityWithAnswer(true);
            }
        });

        // set definition scroll view
        mScrollViewDefinitions = mFragmentView.findViewById(R.id.scrollView_definitions);

        // get user preference for font
        updateFontSize(getFontSizeUserPreference());

        // deletion button
        ImageView deletionButton = mFragmentView.findViewById(R.id.imagebutton_delete);
        deletionButton.setOnClickListener(v -> {
            mTextEditorAnswer.setText("");
            updateDualPaneActivityWithAnswer("", true);
        });

        // wordnik image
        View wordnikImg = mFragmentView.findViewById(R.id.image_wordnik);
        wordnikImg.setOnLongClickListener(v -> {
            Uri uri = Uri.parse("https://www.wordnik.com/words/" + mWord.toLowerCase());
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
            return true;
        });

        return mFragmentView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        mOptionsMenu = menu;

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.answerfragment_fontoptions, menu);

        // if font menu is present, initialize it to current font setting and update UI
        MenuItem fontMenuItem = menu.findItem(R.id.action_setfont);
        if (fontMenuItem != null) {
            //fontMenuItem.add
            int menuItemId = R.id.action_font_medium;
            int fontSizeDescId = R.string.action_font_medium;
            switch (getFontSizeUserPreference()) {
                case FONT_EXTRA_SMALL:
                    menuItemId = R.id.action_font_extra_small;
                    fontSizeDescId = R.string.action_font_extra_small;
                    break;
                case FONT_SMALL:
                    menuItemId = R.id.action_font_small;
                    fontSizeDescId = R.string.action_font_small;
                    break;
                case FONT_MEDIUM:
                    menuItemId = R.id.action_font_medium;
                    fontSizeDescId = R.string.action_font_medium;
                    break;
                case FONT_LARGE:
                    menuItemId = R.id.action_font_large;
                    fontSizeDescId = R.string.action_font_large;
                    break;
                case FONT_EXTRA_LARGE:
                    menuItemId = R.id.action_font_extra_large;
                    fontSizeDescId = R.string.action_font_extra_large;
                    break;
            }
            updateFontMenuState(menuItemId, fontSizeDescId);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        int i = item.getItemId();
        if (i == R.id.action_font_extra_small) {
            updateFontSize(item.getItemId(), FONT_EXTRA_SMALL, R.string.action_font_extra_small);
            return true;
        } else if (i == R.id.action_font_small) {
            updateFontSize(item.getItemId(), FONT_SMALL, R.string.action_font_small);
            return true;
        } else if (i == R.id.action_font_medium) {
            updateFontSize(item.getItemId(), FONT_MEDIUM, R.string.action_font_medium);
            return true;
        } else if (i == R.id.action_font_large) {
            updateFontSize(item.getItemId(), FONT_LARGE, R.string.action_font_large);
            return true;
        } else if (i == R.id.action_font_extra_large) {
            updateFontSize(item.getItemId(), FONT_EXTRA_LARGE, R.string.action_font_extra_large);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    //endregion

    //region PUBLIC CLASS METHODS ------------------------------------------------------------------

    public void giveAnswer() {
        mTextEditorAnswer.setText(mWord.toLowerCase());
        updateDualPaneActivityWithAnswer(mWord, true);
    }

    public void give3LetterHint() {
        mTextEditorAnswer.setText(mWordHint.toLowerCase());
        updateDualPaneActivityWithAnswer(mWordHint, true);
    }

    public void updateFontSize(int fontSize) {
        // update definition views
        View view = mFragmentView;  // for some reason getView() sometimes returns null, so use cached copy of object
        ((TextView) view.findViewById(R.id.txt_answer)).setTextSize(Math.max(fontSize, FONT_MEDIUM));
        ((TextView) view.findViewById(R.id.textview_definitions_ahd)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.textview_definitions_wiktionary)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.textview_definitions_webster)).setTextSize(fontSize);
        ((TextView) view.findViewById(R.id.textview_definitions_century)).setTextSize(fontSize);
    }

    public void setVisible(boolean visible) {
        if (visible) {
            mFragmentView.setVisibility(View.VISIBLE);
        } else {
            mFragmentView.setVisibility(View.GONE);
        }
    }

    public void setGameWord(AnswerPresentation answerPresentation) {
        // if dual pane mode, update game word, otherwise do it when fragment created
        if (mPuzzleRepresentationLayout != null) {
            updateGameWord(answerPresentation);
        }
        mWord = answerPresentation.getWord();
        mWordHint = answerPresentation.getWordHint();
    }

    // called by activity
    public void clearGameWord() {
        // if dual pane mode, update game word, otherwise do it when dialog done drawing itself
        if (mPuzzleRepresentationLayout != null) {
            mPuzzleRepresentationLayout.removeAllViews();
            mTextEditorAnswer.setText("");
            View view = mFragmentView;  // for some reason getView() sometimes returns null, so use cached copy of object
            ((TextView) view.findViewById(R.id.textview_definitions_ahd)).setText(""); // in dual panel mode, there may be existing text
            ((TextView) view.findViewById(R.id.textview_definitions_wiktionary)).setText(""); // in dual panel mode, there may be existing text
            ((TextView) view.findViewById(R.id.textview_definitions_century)).setText(""); // in dual panel mode, there may be existing text
            ((TextView) view.findViewById(R.id.textview_definitions_webster)).setText(""); // in dual panel mode, there may be existing text
        }
    }

    public void hideSoftKeyboardForAnswer() {
        // this works when called from onClick, but not from onCreateView (don't know why)
        InputMethodManager keyboard = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(mTextEditorAnswer.getWindowToken(), 0);
    }

    //endregion

    //region PRIVATE METHODS -----------------------------------------------------------------------

    private void updateGameWord(AnswerPresentation answerPresentation) {
        mWordLength = answerPresentation.getWord().length();

        Map mapOpposingCellValues = new HashMap();
        for (PuzzleCellValue v : answerPresentation.getOpposingPuzzleCellValues()) {
            mapOpposingCellValues.put(v.getPosition(), v);
        }

        // update puzzle representation
        mPuzzleRepresentationLayout.removeAllViews();  // may have previous contents when displayed in dual pane
        for (int i = 0; i < mWordLength; i++) {
            PuzzleRepresentationCellTextView textView = new PuzzleRepresentationCellTextView(getContext());
            mPuzzleRepresentationLayout.addView(textView);
            if (mapOpposingCellValues.containsKey(i)) {
                PuzzleCellValue puzzleCellValue = (PuzzleCellValue)mapOpposingCellValues.get(i);
                textView.fillTextView(puzzleCellValue.getChar(), puzzleCellValue.getConfident());
            } else {
                textView.setTextColor(COLOR_ANSWER);
            }
        }
        mPuzzleScrollView.fullScroll(ScrollView.FOCUS_LEFT);
        mPuzzleScrollView.postInvalidate(); // doing this to fix issue where resizing puzzle representation sometimes leaves black in dual pane mode


        // set text in editor (and puzzle representation and letter count via the editor's TextWatcher handler)
        if (answerPresentation.getUserText() != null) {
            mTextEditorAnswer.setText(answerPresentation.getUserText().toLowerCase());
        } else {
            mTextEditorAnswer.setText("");
        }

        // update definition views
        View view = mFragmentView;  // for some reason getView() sometimes returns null, so use cached copy of object
        updateDefinitionViews(answerPresentation.getAhdDefinitions(), view.findViewById(R.id.textview_attribution_ahd),
                view.findViewById(R.id.textview_definitions_ahd));
        updateDefinitionViews(answerPresentation.getWiktionaryDefinitions(), view.findViewById(R.id.textview_attribution_wiktionary),
                view.findViewById(R.id.textview_definitions_wiktionary));
        updateDefinitionViews(answerPresentation.getCenturyDefinitions(), view.findViewById(R.id.textview_attribution_century),
                view.findViewById(R.id.textview_definitions_century));
        updateDefinitionViews(answerPresentation.getWebsterDefinitions(), view.findViewById(R.id.textview_attribution_webster),
                view.findViewById(R.id.textview_definitions_webster));

        // make sure definitions scrolled back up to the top
        mScrollViewDefinitions.fullScroll(ScrollView.FOCUS_UP);
    }

    private void updateDefinitionViews(List<String> definitions, TextView textViewAttribution, TextView textViewDefinitions) {
        // update definitions
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < definitions.size(); i++) {
            if (i > 0) buffer.append("\n");
            buffer.append(definitions.get(i));
        }
        textViewDefinitions.setText(buffer.toString());

        // show or hide attribution and definition views
        boolean showDefinitionViews = (definitions.size() > 0);
        textViewDefinitions.setVisibility(showDefinitionViews ? View.VISIBLE : View.GONE);
        textViewAttribution.setVisibility(showDefinitionViews ? View.VISIBLE : View.GONE);
    }

    private void updatePuzzleRepresentation() {
        String answerText = getUserEntry().toUpperCase();
        int answerLength = answerText.length();
        for (int i = 0; i < mPuzzleRepresentationLayout.getChildCount(); i++) {
            TextView v = (TextView) mPuzzleRepresentationLayout.getChildAt(i);
            if (COLOR_ANSWER == v.getTextColors().getDefaultColor()) {
                v.setText(String.valueOf((i < answerLength) ? answerText.charAt(i) : ""));
            }
        }
    }

    private void updateActivityWithAnswer(boolean confident) {

        // Return input text to activity
        String answerText = getUserEntry();
        if (answerText.length() > mWordLength) {
            answerText = answerText.substring(0, mWordLength);
        }

        // if dual pane
        if (mDualPaneAnswerListener != null) {
            updateDualPaneActivityWithAnswer(answerText, confident);
        } else {
            // set result for single pane mode
            Intent result = new Intent();
            result.putExtra(VocabRecallActivity.ACTIVITYRESULT_ANSWER, answerText);
            result.putExtra(VocabRecallActivity.ACTIVITYRESULT_CONFIDENT, confident);
            mActivity.setResult(Activity.RESULT_OK, result);
            mActivity.finish();

            hideSoftKeyboardForAnswer();
        }
    }

    private void updateDualPaneActivityWithAnswer(String answerText, boolean confident) {
        if (mDualPaneAnswerListener != null) {
            mDualPaneAnswerListener.onFinishAnswerDialog(answerText, confident);
            hideSoftKeyboardForAnswer();
        }
    }

    private String getUserEntry() {
        return mTextEditorAnswer.getText().toString().trim();
    }

    private void updateLetterCount() {
        int letterCount = getUserEntry().length();
        String letterCountText = letterCount + " / " + mWordLength;
        mTextViewLetterCount.setText(letterCountText);
    }

    private void showSoftKeyboardForAnswer() {
        // this works when called from onClick, but not from onCreateView (don't know why)
        InputMethodManager keyboard = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(mTextEditorAnswer, 0);

        // this works when called from onCreateView, but not from onClick
//	     mTextEditorAnswer.requestFocus();
//	     mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private int getFontSizeUserPreference() {
        SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        int fontSize = preferences.getInt(PREFERENCE_KEY_FONT, FONT_MEDIUM);
        return fontSize;
    }


    private void setFontSizeUserPreference(int fontSize) {
        SharedPreferences.Editor prefsEditor = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
        prefsEditor.putInt(PREFERENCE_KEY_FONT, fontSize); // save preference
        prefsEditor.commit();
    }

    private void updateFontMenuState(int menuItemId, int fontSizeDescId) {
        setOptionsMenuChecked(menuItemId, true);
        setOptionsMenuText(R.id.action_setfont, getResources().getString(R.string.action_setfont) + " (" + getResources().getString(fontSizeDescId) + ")");
    }

    protected void setOptionsMenuChecked(int menuItemId, boolean checked) {
        mOptionsMenu.findItem(menuItemId).setChecked(checked);
    }

    protected void setOptionsMenuText(int menuItemId, String text) {
        mOptionsMenu.findItem(menuItemId).setTitle(text);
    }


    private void updateFontSize(int menuItemId, int fontSize, int fontSizeDescId) {
        updateFontMenuState(menuItemId, fontSizeDescId);
        updateFontSize(fontSize);
        setFontSizeUserPreference(fontSize);
    }

    //endregion

}

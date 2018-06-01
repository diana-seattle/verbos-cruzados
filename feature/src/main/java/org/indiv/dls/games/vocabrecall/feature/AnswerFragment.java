package org.indiv.dls.games.vocabrecall.feature;

import java.util.ArrayList;
import java.util.List;

import org.indiv.dls.games.vocabrecall.feature.db.Definition;
import org.indiv.dls.games.vocabrecall.feature.db.GameWord;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
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
import android.widget.TextView.OnEditorActionListener;

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
    public static final int FONT_EXTRA_SMALL = 16; // in SP
    public static final int FONT_SMALL = 19;
    public static final int FONT_MEDIUM = 22;
    public static final int FONT_LARGE = 26;
    public static final int FONT_EXTRA_LARGE = 30;

    private final static int COLOR_ANSWER = 0xFF0099cc;  // a little darker than puzzle background

    //endregion

    //region CLASS VARIABLES -----------------------------------------------------------------------

    public static int sFontSize = FONT_MEDIUM;  // static variable so only has to be set once for all instances of activity
    private Menu mOptionsMenu;


    private EditText mTextEditorAnswer;
    private TextView mTextViewLetterCount;
    private LinearLayout mLayoutPuzzleRepresentation;
    private HorizontalScrollView mPuzzleScrollView;
    private int mWordLength;
    private View mFragmentView; // for some reason getView() sometimes returns null, so hold onto a copy of the view
    private DualPaneAnswerListener mAnswerDialogListener;
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
            mAnswerDialogListener = (DualPaneAnswerListener) getActivity();
        }

        // enable to add font submenu item
        setHasOptionsMenu(true);

        // puzzle representation
        mPuzzleScrollView = (HorizontalScrollView) mFragmentView.findViewById(R.id.puzzle_representation_scrollview);
        mLayoutPuzzleRepresentation = (LinearLayout) mFragmentView.findViewById(R.id.puzzle_representation);
        mLayoutPuzzleRepresentation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSoftKeyboardForAnswer();
            }
        });


        // text editor
        mTextEditorAnswer = (EditText) mFragmentView.findViewById(R.id.txt_answer);
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
        mTextEditorAnswer.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    updateDualPaneActivityWithAnswer(getUserEntry(), true); // doing this on text change results in keyboard being prematurely dismissed
                }
                return false;
            }
        });

        // get letter count text view  
        mTextViewLetterCount = (TextView) mFragmentView.findViewById(R.id.lbl_letter_count);


        // confirmation buttons
        Button buttonTentative = (Button) mFragmentView.findViewById(R.id.button_tentative);
        buttonTentative.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateActivityWithAnswer(false);
            }
        });
        Button buttonConfident = (Button) mFragmentView.findViewById(R.id.button_confident);
        buttonConfident.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateActivityWithAnswer(true);
            }
        });

        // set definition scroll view
        mScrollViewDefinitions = (ScrollView) mFragmentView.findViewById(R.id.scrollView_definitions);

        // get user preference for font
        sFontSize = getFontSizeUserPreference();
        updateFontSize(sFontSize);

        // deletion button
        ImageView deletionButton = (ImageView) mFragmentView.findViewById(R.id.imagebutton_delete);
        deletionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mTextEditorAnswer.setText("");
                updateDualPaneActivityWithAnswer("", true);
            }
        });


        // wordnik image
        View wordnikImg = mFragmentView.findViewById(R.id.image_wordnik);
        wordnikImg.setOnLongClickListener(v -> {
            GameWord gameWord = MyActionBarActivity.sCurrentGameWord;
            Uri uri = Uri.parse("https://www.wordnik.com/words/" + gameWord.getWord().toLowerCase());
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
            return true;
        });


        // Show soft keyboard automatically
        // (this works when called from onCreateView, but not from onClick)
//	     mTextEditorAnswer.requestFocus();
//	     getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return mFragmentView;
    }


    @Override
    public void onResume() { // called when activity in foreground again
        super.onResume();

        // if showing fragment in separate activity, need to update game word info here
        if (MyActionBarActivity.sCurrentGameWord != null) {
            updateGameWord();
        } else {
            // if single pane, and phone turned off, then back on, and user returns to answer activity, definitions will be empty, so handle that case
            if (mAnswerDialogListener == null) {
                mActivity.setResult(Activity.RESULT_CANCELED);
                mActivity.finish();
            }
        }
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
            switch (sFontSize) {
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
        String answer = MyActionBarActivity.sCurrentGameWord.getWord().toLowerCase();
        mTextEditorAnswer.setText(answer);
        updateDualPaneActivityWithAnswer(answer, true);
    }

    public void give3LetterHint() {
        String hint = MyActionBarActivity.sCurrentGameWord.get3LetterHint().toLowerCase();
        mTextEditorAnswer.setText(hint);
        updateDualPaneActivityWithAnswer(hint, true);
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

    // called by main activity in dual pane mode
    public void setGameWord() {
//    public void setGameWord(GameWord gameWord, List<TextView> puzzleRepresentation) {
//        mGameWord = gameWord;
//        mWordLength = mGameWord.getWord().length();
//        mPuzzleRepresentation = puzzleRepresentation;

        // if dual pane mode, update game word, otherwise do it when dialog done drawing itself 
        if (mLayoutPuzzleRepresentation != null) {
            updateGameWord();
        }
    }

    // called by activity
    public void clearGameWord() {
//        mGameWord = null;
//        mWordLength = 0;
//        if (mPuzzleRepresentation != null) {
//        	mPuzzleRepresentation.clear();
//        }

        // if dual pane mode, update game word, otherwise do it when dialog done drawing itself 
        if (mLayoutPuzzleRepresentation != null) {
            mLayoutPuzzleRepresentation.removeAllViews();
            mTextEditorAnswer.setText("");
            View view = mFragmentView;  // for some reason getView() sometimes returns null, so use cached copy of object
            ((TextView) view.findViewById(R.id.textview_definitions_ahd)).setText(""); // in dual panel mode, there may be existing text
            ((TextView) view.findViewById(R.id.textview_definitions_wiktionary)).setText(""); // in dual panel mode, there may be existing text
            ((TextView) view.findViewById(R.id.textview_definitions_century)).setText(""); // in dual panel mode, there may be existing text
            ((TextView) view.findViewById(R.id.textview_definitions_webster)).setText(""); // in dual panel mode, there may be existing text
        }
    }

    //endregion

    //region PRIVATE METHODS -----------------------------------------------------------------------

    private void updateGameWord() {
        GameWord gameWord = MyActionBarActivity.sCurrentGameWord;
        mWordLength = gameWord.getWord().length();

        // update puzzle representation
        mLayoutPuzzleRepresentation.removeAllViews();  // may have previous contents when displayed in dual pane 
        for (TextView v : MyActionBarActivity.sPuzzleRepresentation) {
            mLayoutPuzzleRepresentation.addView(v);
            if (v.getText() == null || v.getText().length() == 0) {
                v.setTextColor(COLOR_ANSWER);
            } else {
//				v.setTextAppearance(mActivity, R.style.boldText);
            }
        }
        mPuzzleScrollView.fullScroll(ScrollView.FOCUS_LEFT);
        mPuzzleScrollView.postInvalidate(); // doing this to fix issue where resizing puzzle representation sometimes leaves black in dual pane mode


        // set text in editor (and puzzle representation and letter count via the editor's TextWatcher handler)
        if (gameWord.getUserText() != null) {
            mTextEditorAnswer.setText(gameWord.getUserText().toLowerCase());
        } else {
            mTextEditorAnswer.setText("");
        }

        // split definitions by source
        List<Definition> definitions = gameWord.getWordInfo().getDefinitions();
        List<Definition> ahdDefinitions = new ArrayList<Definition>();
        List<Definition> wiktionaryDefinitions = new ArrayList<Definition>();
        List<Definition> websterDefinitions = new ArrayList<Definition>();
        List<Definition> centuryDefinitions = new ArrayList<Definition>();
        for (Definition d : definitions) {
            if (d.isSourceAhd()) {
                ahdDefinitions.add(d);
            } else if (d.isSourceWiktionary()) {
                wiktionaryDefinitions.add(d);
            } else if (d.isSourceWebster()) {
                websterDefinitions.add(d);
            } else if (d.isSourceCentury()) {
                centuryDefinitions.add(d);
            }
        }
        // update definition views
        View view = mFragmentView;  // for some reason getView() sometimes returns null, so use cached copy of object
        updateDefinitionViews(ahdDefinitions, (TextView) view.findViewById(R.id.textview_attribution_ahd),
                (TextView) view.findViewById(R.id.textview_definitions_ahd));
        updateDefinitionViews(wiktionaryDefinitions, (TextView) view.findViewById(R.id.textview_attribution_wiktionary),
                (TextView) view.findViewById(R.id.textview_definitions_wiktionary));
        updateDefinitionViews(centuryDefinitions, (TextView) view.findViewById(R.id.textview_attribution_century),
                (TextView) view.findViewById(R.id.textview_definitions_century));
        updateDefinitionViews(websterDefinitions, (TextView) view.findViewById(R.id.textview_attribution_webster),
                (TextView) view.findViewById(R.id.textview_definitions_webster));

        // make sure definitions scrolled back up to the top
        mScrollViewDefinitions.fullScroll(ScrollView.FOCUS_UP);
    }


    private void updateDefinitionViews(List<Definition> definitions, TextView textViewAttribution, TextView textViewDefinitions) {
        // update definitions
        textViewDefinitions.setText(""); // in dual panel mode, there may be existing text
        for (int i = 0; i < definitions.size(); i++) {
            if (i > 0) textViewDefinitions.append("\n");
            textViewDefinitions.append(definitions.get(i).getFullText(i + 1));
        }
        // show or hide attribution and definition views
        boolean showDefinitionViews = (definitions.size() > 0);
        textViewDefinitions.setVisibility(showDefinitionViews ? View.VISIBLE : View.GONE);
        textViewAttribution.setVisibility(showDefinitionViews ? View.VISIBLE : View.GONE);
    }

    private void updatePuzzleRepresentation() {
        String answerText = getUserEntry().toUpperCase();
        int answerLength = answerText.length();
        for (int i = 0; i < mLayoutPuzzleRepresentation.getChildCount(); i++) {
            TextView v = (TextView) mLayoutPuzzleRepresentation.getChildAt(i);
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
        if (mAnswerDialogListener != null) {
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
        if (mAnswerDialogListener != null) {
            mAnswerDialogListener.onFinishAnswerDialog(answerText, confident);
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

    private void hideSoftKeyboardForAnswer() {
        // this works when called from onClick, but not from onCreateView (don't know why)
        InputMethodManager keyboard = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(mTextEditorAnswer.getWindowToken(), 0);
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
        sFontSize = fontSize;
        updateFontMenuState(menuItemId, fontSizeDescId);
        updateFontSize(fontSize);
        setFontSizeUserPreference(fontSize);
    }

    //endregion

}

package org.indiv.dls.games.verboscruzados.feature

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_answer.*

/**
 * Enum for user-specifiable font size for the answer fragment.
 */
enum class FontSize(val sizeInSp: Int, val nameResId: Int) {
    FONT_EXTRA_SMALL(16, R.string.action_font_extra_small),
    FONT_SMALL(19, R.string.action_font_small),
    FONT_MEDIUM(22, R.string.action_font_medium),
    FONT_LARGE(26, R.string.action_font_large),
    FONT_EXTRA_LARGE(30, R.string.action_font_extra_large);

    companion object {
        fun getFontForSize(sizeInSp: Int): FontSize {
            for (value in values()) {
                if (value.sizeInSp == sizeInSp) {
                    return value
                }
            }
            return FONT_MEDIUM
        }
    }
}

/**
 * Fragment containing the clues and answer entry.
 */
class AnswerFragment : Fragment() {

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        private val PREFERENCE_KEY_FONT = "fontSize"
        private val COLOR_ANSWER = -0xff6634  // a little darker than puzzle background
    }

    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private var optionsMenu: Menu? = null

    private var word: String? = null
    private var wordLength: Int = 0

    private val userEntry: String
        get() = txt_answer.text.toString().trim { it <= ' ' }

    private var fontSizeUserPreference: Int
        get() = activity?.getPreferences(Context.MODE_PRIVATE)?.getInt(PREFERENCE_KEY_FONT, FontSize.FONT_MEDIUM.sizeInSp)
                ?: FontSize.FONT_MEDIUM.sizeInSp
        set(fontSize) {
            activity?.getPreferences(Context.MODE_PRIVATE)?.edit()
                    ?.putInt(PREFERENCE_KEY_FONT, fontSize)
                    ?.apply()
        }

    //endregion

    //region PUBLIC INTERFACES ---------------------------------------------------------------------

    // interface for activity to implement to receive result
    interface DualPaneAnswerListener {
        fun onFinishAnswerDialog(userText: String)
    }

    //endregion

    //region OVERRIDDEN FUNCTIONS ------------------------------------------------------------------

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_answer, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // enable to add font submenu item
        setHasOptionsMenu(true)

        // puzzle representation
        puzzle_representation.setOnClickListener { showSoftKeyboardForAnswer() }

        val specialLetterListener = { v: View -> insertLetter(v as TextView) }
        button_accented_a.setOnClickListener(specialLetterListener)
        button_accented_e.setOnClickListener(specialLetterListener)
        button_accented_i.setOnClickListener(specialLetterListener)
        button_accented_o.setOnClickListener(specialLetterListener)
        button_umlaut_u.setOnClickListener(specialLetterListener)
        button_tilde_n.setOnClickListener(specialLetterListener)

        // text editor
        txt_answer.setTextColor(COLOR_ANSWER)

        txt_answer.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                updateLetterCount()
                updatePuzzleRepresentation()
            }
        })
        txt_answer.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // doing this on text change results in keyboard being prematurely dismissed
                updateDualPaneActivityWithAnswer(userEntry)
            }
            false
        }

        // confirmation button
        button_submit.setOnClickListener { updateActivityWithAnswer() }

        // get user preference for font
        updateViewFontSize(fontSizeUserPreference)

        // deletion button
        imagebutton_delete.setOnClickListener {
            txt_answer.setText("")
            updateDualPaneActivityWithAnswer("")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        optionsMenu = menu

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater?.inflate(R.menu.answerfragment_fontoptions, menu)

        // if font menu is present, initialize it to current font setting and update UI
        menu?.findItem(R.id.action_setfont)?.let {
            val fontSize = FontSize.getFontForSize(fontSizeUserPreference)
            val menuItemId = when (fontSize) {
                FontSize.FONT_EXTRA_SMALL -> R.id.action_font_extra_small
                FontSize.FONT_SMALL -> R.id.action_font_small
                FontSize.FONT_MEDIUM -> R.id.action_font_medium
                FontSize.FONT_LARGE -> R.id.action_font_large
                FontSize.FONT_EXTRA_LARGE -> R.id.action_font_extra_large
            }
            updateFontMenuState(menuItemId, fontSize.nameResId)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * Handles presses on the action bar items
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_font_extra_small -> FontSize.FONT_EXTRA_SMALL
            R.id.action_font_small -> FontSize.FONT_SMALL
            R.id.action_font_medium -> FontSize.FONT_MEDIUM
            R.id.action_font_large -> FontSize.FONT_LARGE
            R.id.action_font_extra_large -> FontSize.FONT_EXTRA_LARGE
            else -> null
        }?.let {
            updateFontSize(item.itemId, it)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    //endregion

    //region PUBLIC CLASS FUNCTIONS ----------------------------------------------------------------

    fun setGameWord(answerPresentation: AnswerPresentation) {
        updateGameWord(answerPresentation)
        word = answerPresentation.word
    }

    // called by activity
    fun clearGameWord() {
        puzzle_representation.removeAllViews()
        txt_answer.setText("")
        textview_sentence_clue.text = "" // in dual panel mode, there may be existing text
        textview_infinitive_clue.text = ""
    }

    fun hideSoftKeyboardForAnswer() {
        // this works when called from onClick, but not from onCreateView
        activity?.getSystemService(Context.INPUT_METHOD_SERVICE)?.let {
            (it as InputMethodManager).hideSoftInputFromWindow(txt_answer.windowToken, 0)
        }
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    private fun updateGameWord(answerPresentation: AnswerPresentation) {
        wordLength = answerPresentation.word.length

        // update puzzle representation
        puzzle_representation.removeAllViews()  // may have previous contents when displayed in dual pane
        for (i in 0 until wordLength) {
            val textView = PuzzleRepresentationCellTextView(context!!)
            puzzle_representation.addView(textView)
            answerPresentation.opposingPuzzleCellValues[i]?.let {
                textView.fillTextView(it)
            } ?: run {
                textView.setTextColor(COLOR_ANSWER)
            }
        }
        puzzle_representation_scrollview.fullScroll(ScrollView.FOCUS_LEFT)
        puzzle_representation_scrollview.postInvalidate() // doing this to fix issue where resizing puzzle representation sometimes leaves black in dual pane mode


        // set text in editor (and puzzle representation and letter count via the editor's TextWatcher handler)
        txt_answer.setText(answerPresentation.userText?.toLowerCase() ?: "")

        // update clue views
        textview_sentence_clue.text = answerPresentation.sentenceClue
        textview_infinitive_clue.text = answerPresentation.infinitiveClue
    }

    private fun updatePuzzleRepresentation() {
        val answerText = userEntry.toUpperCase()
        val answerLength = answerText.length
        for (i in 0 until puzzle_representation.childCount) {
            val textView = puzzle_representation.getChildAt(i) as TextView
            if (COLOR_ANSWER == textView.textColors.defaultColor) {
                textView.text = if (i < answerLength) answerText[i].toString() else ""
            }
        }
    }

    private fun updateActivityWithAnswer() {

        // Return input text to activity
        var answerText = userEntry
        if (answerText.length > wordLength) {
            answerText = answerText.substring(0, wordLength)
        }

        // if dual pane
        if (activity is DualPaneAnswerListener) {
            updateDualPaneActivityWithAnswer(answerText)
        } else {
            // set result for single pane mode
            val result = Intent()
            result.putExtra(MainActivity.ACTIVITYRESULT_ANSWER, answerText)
            activity?.setResult(Activity.RESULT_OK, result)
            activity?.finish()

            hideSoftKeyboardForAnswer()
        }
    }

    private fun updateDualPaneActivityWithAnswer(answerText: String) {
        activity?.let {
            if (it is DualPaneAnswerListener) {
                it.onFinishAnswerDialog(answerText)
                hideSoftKeyboardForAnswer()
            }
        }
    }

    private fun updateLetterCount() {
        val letterCount = userEntry.length
        val letterCountText = letterCount.toString() + " / " + wordLength
        letter_count.text = letterCountText
    }

    private fun showSoftKeyboardForAnswer() {
        // this works when called from onClick, but not from onCreateView
        (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(txt_answer, 0)

        // this works when called from onCreateView, but not from onClick
        //	     mTextEditorAnswer.requestFocus();
        //	     activity?.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private fun setOptionsMenuChecked(menuItemId: Int, checked: Boolean) {
        optionsMenu?.findItem(menuItemId)?.isChecked = checked
    }

    private fun setOptionsMenuText(menuItemId: Int, text: String) {
        optionsMenu?.findItem(menuItemId)?.title = text
    }

    private fun updateFontMenuState(menuItemId: Int, fontSizeDescId: Int) {
        setOptionsMenuChecked(menuItemId, true)
        setOptionsMenuText(R.id.action_setfont, resources.getString(R.string.action_setfont) + " (" + resources.getString(fontSizeDescId) + ")")
    }

    private fun updateFontSize(menuItemId: Int, fontSize: FontSize) {
        updateFontMenuState(menuItemId, fontSize.nameResId)
        updateViewFontSize(fontSize.sizeInSp)
        fontSizeUserPreference = fontSize.sizeInSp
    }

    /**
     * Updates font size of answer and definition views
     */
    private fun updateViewFontSize(sizeInSp: Int) {
        txt_answer.textSize = Math.max(sizeInSp, FontSize.FONT_MEDIUM.sizeInSp).toFloat()
        val size = sizeInSp.toFloat()
    }

    private fun insertLetter(letterView: TextView) {
        // Note that selection may happen in the backward direction causing end to be larger than start.
        val selectionStart = minOf(txt_answer.selectionStart, txt_answer.selectionEnd)
        val selectionEnd = maxOf(txt_answer.selectionStart, txt_answer.selectionEnd)
        txt_answer.text.replace(selectionStart, selectionEnd, letterView.text)
        txt_answer.setSelection(selectionStart + 1)
    }

    //endregion

}

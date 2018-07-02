package org.indiv.dls.games.verboscruzados.feature

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_answer.*

/**
 * Fragment containing the clues and answer entry.
 */
class AnswerFragment : Fragment() {

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        private const val COLOR_ANSWER = -0xff6634  // a little darker than puzzle background
    }

    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private var infinitive: String = ""
    private var word: String = ""
    private var wordLength: Int = 0

    private val userEntry: String
        get() = txt_answer.text.toString().trim { it <= ' ' }

    //endregion

    //region PUBLIC INTERFACES ---------------------------------------------------------------------

    // interface for activity to implement to receive result
    interface AnswerListener {
        fun onUpdateAnswer(userText: String)
    }

    //endregion

    //region OVERRIDDEN FUNCTIONS ------------------------------------------------------------------

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_answer, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val specialLetterListener = { v: View -> insertText((v as TextView).text.toString()) }
        button_accented_a.setOnClickListener(specialLetterListener)
        button_accented_e.setOnClickListener(specialLetterListener)
        button_accented_i.setOnClickListener(specialLetterListener)
        button_accented_o.setOnClickListener(specialLetterListener)
        button_umlaut_u.setOnClickListener(specialLetterListener)
        button_tilde_n.setOnClickListener(specialLetterListener)
        button_infinitive.setOnClickListener { _: View -> insertText(infinitive) }

        // text editor
        txt_answer.setTextColor(COLOR_ANSWER)

        txt_answer.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                updateActivityWithAnswer()

                // If user text matches answer, dismiss keyboard
                if (userEntry.toLowerCase() == word.toLowerCase()) {
                    hideSoftKeyboardForAnswer()
                }
            }
        })
    }

    //endregion

    //region PUBLIC CLASS FUNCTIONS ----------------------------------------------------------------

    fun setGameWord(answerPresentation: AnswerPresentation) {
        updateGameWord(answerPresentation)
        word = answerPresentation.word
        infinitive = answerPresentation.infinitive

        // If answer not yet correct, show keyboard
        if (userEntry.toLowerCase() != word.toLowerCase()) {
            showSoftKeyboardForAnswer()
        }
    }

    // called by activity
    fun clearGameWord() {
        txt_answer.setText("")
        txt_answer_layout.hint = ""
        txt_answer_layout.error = ""
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    private fun updateGameWord(answerPresentation: AnswerPresentation) {
        wordLength = answerPresentation.word.length

        // set text in editor
        txt_answer.setText(answerPresentation.userText?.toLowerCase() ?: "")
        txt_answer.setSelection(answerPresentation.userText?.length ?: 0)

        // update clue views
        txt_answer_layout.hint = answerPresentation.conjugationLabel
        txt_answer_layout.error = "${answerPresentation.infinitive} (${answerPresentation.translation})"
    }

    private fun updateActivityWithAnswer() {
        // Return input text to activity
        var answerText = userEntry
        if (answerText.length > wordLength) {
            answerText = answerText.substring(0, wordLength)
        }

        (activity as? AnswerListener)?.onUpdateAnswer(answerText)
    }

    private fun insertText(text: String) {
        // Note that selection may happen in the backward direction causing end to be larger than start.
        val selectionStart = minOf(txt_answer.selectionStart, txt_answer.selectionEnd)
        val selectionEnd = maxOf(txt_answer.selectionStart, txt_answer.selectionEnd)
        txt_answer.text.replace(selectionStart, selectionEnd, text)
        txt_answer.setSelection(selectionStart + text.length)
    }

    private fun showSoftKeyboardForAnswer() {
        // this works when called from onClick, but not from onCreateView
        (activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(txt_answer, 0)

        // this works when called from onCreateView, but not from onClick
        //	     mTextEditorAnswer.requestFocus();
        //	     activity?.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private fun hideSoftKeyboardForAnswer() {
        // this works when called from onClick, but not from onCreateView
        activity?.getSystemService(Context.INPUT_METHOD_SERVICE)?.let {
            (it as InputMethodManager).hideSoftInputFromWindow(txt_answer.windowToken, 0)
        }
    }

    //endregion

}

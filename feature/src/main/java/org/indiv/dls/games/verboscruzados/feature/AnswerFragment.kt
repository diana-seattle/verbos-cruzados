package org.indiv.dls.games.verboscruzados.feature

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.fragment_answer.*

/**
 * Fragment containing the clues and answer entry.
 */
class AnswerFragment : Fragment() {

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private var infinitive: String = ""
    private var word: String = ""

    private val userEntry: String
        get() = txt_answer.text.toString().trim { it <= ' ' }

    //endregion


    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    /**
     * Input connection from the answer EditText.
     */
    val answerEntryInputConnection: InputConnection
        get() = txt_answer.onCreateInputConnection(EditorInfo())

    // Caller can set this to be notified when the user needs to see the keyboard
    var keyboardNeededListener: (() -> Unit)? = null

    //endregion


    //region PUBLIC INTERFACES ---------------------------------------------------------------------

    // TODO: change AnswerListener to function reference!!

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

        // Disable the default Android keyboard
        txt_answer.setRawInputType(txt_answer.inputType)
        txt_answer.setTextIsSelectable(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            txt_answer.showSoftInputOnFocus = false
        }
        txt_answer.setOnTouchListener { view, event ->
            view.onTouchEvent(event) // handle the event first
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                hideDefaultKeyboard()
            }
            keyboardNeededListener?.invoke()
            true
        }

        txt_answer.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                (activity as? AnswerListener)?.onUpdateAnswer(userEntry)
            }
        })
    }

    //endregion

    //region PUBLIC CLASS FUNCTIONS ----------------------------------------------------------------

    fun setGameWord(answerPresentation: AnswerPresentation) {
        updateGameWord(answerPresentation.userText)
        word = answerPresentation.word
        infinitive = answerPresentation.infinitive
    }

    // called by activity
    fun clearGameWord() {
        txt_answer.setText("")
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    private fun updateGameWord(userText: String?) {
        // set text in editor
        txt_answer.setText(userText?: "")
        txt_answer.setSelection(userText?.length ?: 0)
    }

    private fun insertText(text: String) {
        // Note that selection may happen in the backward direction causing end to be larger than start.
        val selectionStart = minOf(txt_answer.selectionStart, txt_answer.selectionEnd)
        val selectionEnd = maxOf(txt_answer.selectionStart, txt_answer.selectionEnd)
        txt_answer.text.replace(selectionStart, selectionEnd, text)
        txt_answer.setSelection(selectionStart + text.length)
    }

    private fun getDefaultKeyboard(): InputMethodManager? {
        return activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    }

    private fun hideDefaultKeyboard() {
        // this works when called from onClick, but not from onCreateView
        getDefaultKeyboard()?.hideSoftInputFromWindow(txt_answer.windowToken, 0)
    }

    //endregion

}

package org.indiv.dls.games.verboscruzados.feature.component

import android.content.Context
import android.os.Vibrator
import android.text.Html
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.keyboard_minimal.view.*
import org.indiv.dls.games.verboscruzados.feature.AnswerPresentation
import org.indiv.dls.games.verboscruzados.feature.R
import org.indiv.dls.games.verboscruzados.feature.R.id.keyboard_button_infinitive
import org.indiv.dls.games.verboscruzados.feature.R.id.keyboard_conjugation_type_label
import org.indiv.dls.games.verboscruzados.feature.R.id.keyboard_subject_pronoun_label
import org.indiv.dls.games.verboscruzados.feature.R.id.keyboard_translation

/**
 * Minimal keyboard for entering answers while covering the least amount of puzzle possible.
 *
 * See: https://stackoverflow.com/questions/9577304/how-to-make-an-android-custom-keyboard/45005691#45005691
 */
open class MinimalKeyboard @JvmOverloads constructor(context: Context,
                                                     attrs: AttributeSet? = null,
                                                     defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {


    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        val VIBRATION_MSEC = 25L
    }
    //endregion


    //region INITIALIZER ---------------------------------------------------------------------------

    init {
        inflate(context, R.layout.keyboard_minimal, this)

        // Get instance of Vibrator from current Context
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        val letterClickListener: (View) -> Unit = {
            (it as? TextView)?.let {
                vibrator.vibrate(VIBRATION_MSEC)
                letterClickListener?.invoke(it.text[0])
            }
        }

        button_a.setOnClickListener { letterClickListener.invoke(it) }
        button_b.setOnClickListener { letterClickListener.invoke(it) }
        button_c.setOnClickListener { letterClickListener.invoke(it) }
        button_d.setOnClickListener { letterClickListener.invoke(it) }
        button_e.setOnClickListener { letterClickListener.invoke(it) }
        button_f.setOnClickListener { letterClickListener.invoke(it) }
        button_g.setOnClickListener { letterClickListener.invoke(it) }
        button_h.setOnClickListener { letterClickListener.invoke(it) }
        button_i.setOnClickListener { letterClickListener.invoke(it) }
        button_j.setOnClickListener { letterClickListener.invoke(it) }
        button_k.setOnClickListener { letterClickListener.invoke(it) }
        button_l.setOnClickListener { letterClickListener.invoke(it) }
        button_m.setOnClickListener { letterClickListener.invoke(it) }
        button_n.setOnClickListener { letterClickListener.invoke(it) }
        button_o.setOnClickListener { letterClickListener.invoke(it) }
        button_p.setOnClickListener { letterClickListener.invoke(it) }
        button_q.setOnClickListener { letterClickListener.invoke(it) }
        button_r.setOnClickListener { letterClickListener.invoke(it) }
        button_s.setOnClickListener { letterClickListener.invoke(it) }
        button_t.setOnClickListener { letterClickListener.invoke(it) }
        button_u.setOnClickListener { letterClickListener.invoke(it) }
        button_v.setOnClickListener { letterClickListener.invoke(it) }
        button_w.setOnClickListener { letterClickListener.invoke(it) }
        button_x.setOnClickListener { letterClickListener.invoke(it) }
        button_y.setOnClickListener { letterClickListener.invoke(it) }
        button_z.setOnClickListener { letterClickListener.invoke(it) }
        button_a_accent.setOnClickListener { letterClickListener.invoke(it) }
        button_e_accent.setOnClickListener { letterClickListener.invoke(it) }
        button_i_accent.setOnClickListener { letterClickListener.invoke(it) }
        button_o_accent.setOnClickListener { letterClickListener.invoke(it) }
        button_u_accent.setOnClickListener { letterClickListener.invoke(it) }
        button_u_umlaut.setOnClickListener { letterClickListener.invoke(it) }
        button_n_tilde.setOnClickListener { letterClickListener.invoke(it) }
        button_close.setOnClickListener {
            vibrator.vibrate(VIBRATION_MSEC)
            dismissClickListener?.invoke()
        }
        keyboard_button_infinitive.setOnClickListener {
            vibrator.vibrate(VIBRATION_MSEC)
            infinitiveClickListener?.invoke(answerPresentation?.infinitive ?: "")
        }
        button_delete.setOnClickListener {
            vibrator.vibrate(VIBRATION_MSEC)
            val selectedText = inputConnection?.getSelectedText(0)
            if (selectedText.isNullOrEmpty()) {
                // no selection, so delete previous character
                inputConnection?.deleteSurroundingText(1, 0)
            } else {
                // delete the selection
                inputConnection?.commitText("", 1)
            }
        }
        button_delete.setOnLongClickListener {
            // delete all characters before the cursor
            inputConnection?.deleteSurroundingText(50, 0) == true
        }
        button_left_arrow.setOnClickListener {
            vibrator.vibrate(VIBRATION_MSEC)
            leftClickListener?.invoke()
        }
        button_right_arrow.setOnClickListener {
            vibrator.vibrate(VIBRATION_MSEC)
            rightClickListener?.invoke()
        }
        button_next_word.setOnClickListener {
            vibrator.vibrate(VIBRATION_MSEC)
            nextWordClickListener?.invoke()
        }
    }

    //endregion

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    // Caller must set this to the current EditText's input connection.
    var inputConnection: InputConnection? = null

    // Caller can set this to be notified when the user wants to dismiss the keyboard
    var dismissClickListener: (() -> Unit)? = null

    // Caller can set this to be notified when the user wants to select the next word
    var nextWordClickListener: (() -> Unit)? = null

    // Caller can set this to be notified when the user clicks on a letter
    var letterClickListener: ((Char) -> Unit)? = null

    // Caller can set this to be notified when the user clicks on the infinitive
    var infinitiveClickListener: ((String) -> Unit)? = null

    // Caller can set this to be notified when the user clicks on the left/up arrow
    var leftClickListener: (() -> Unit)? = null

    // Caller can set this to be notified when the user clicks on the right/down arrow
    var rightClickListener: (() -> Unit)? = null

    // Caller must set this for the display of the clue info and for the infinitive button to insert the infinitive text.
    var answerPresentation: AnswerPresentation? = null
        set(value) {
            field = value
            value?.apply {
                keyboard_conjugation_type_label.text = conjugationTypeLabel
                keyboard_button_infinitive.text = Html.fromHtml("<u>$infinitive</u>")
                keyboard_translation.text = "($translation)"
                keyboard_subject_pronoun_label.text = subjectPronounLabel
                keyboard_subject_pronoun_label.visibility = if (subjectPronounLabel.isEmpty()) GONE else VISIBLE
            }
        }

    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------
    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------
    //endregion
}

package org.indiv.dls.games.verboscruzados.feature.component

import android.content.Context
import android.os.Vibrator
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.keyboard_minimal.view.*
import org.indiv.dls.games.verboscruzados.feature.AnswerPresentation
import org.indiv.dls.games.verboscruzados.feature.R
import org.indiv.dls.games.verboscruzados.feature.Vibration

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
    //endregion

    //region INITIALIZER ---------------------------------------------------------------------------

    init {
        inflate(context, R.layout.keyboard_minimal, this)

        // Get instance of Vibrator from current Context
        val vibration = context?.let {
            Vibration(it)
        }
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        val letterClickListener: (View) -> Unit = {
            (it as? TextView)?.let {
                vibration.vibrate()
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
        button_hide_keyboard.setOnClickListener {
            vibration.vibrate()
            dismissClickListener?.invoke()
        }
        keyboard_button_infinitive.setOnClickListener {
            vibration.vibrate()
            infinitiveClickListener?.invoke(answerPresentation?.infinitive ?: "")
        }
        button_delete.setOnClickListener {
            vibration.vibrate()
            deleteClickListener?.invoke()
        }
        button_delete.setOnLongClickListener() {
            vibration.vibrate()
            deleteLongClickListener?.invoke()
            true
        }
        button_left_arrow.setOnClickListener {
            vibration.vibrate()
            leftClickListener?.invoke()
        }
        button_right_arrow.setOnClickListener {
            vibration.vibrate()
            rightClickListener?.invoke()
        }
        button_next_word.setOnClickListener {
            vibration.vibrate()
            nextWordClickListener?.invoke()
        }
    }

    //endregion

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    // Caller can set this to be notified when the user wants to dismiss the keyboard
    var dismissClickListener: (() -> Unit)? = null

    // Caller can set this to be notified when the user wants to select the next word
    var nextWordClickListener: (() -> Unit)? = null

    // Caller can set this to be notified when the user clicks on the delete button
    var deleteClickListener: (() -> Unit)? = null

    // Caller can set this to be notified when the user long clicks on the delete button
    var deleteLongClickListener: (() -> Unit)? = null

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
                keyboard_button_infinitive.text = infinitive
                keyboard_translation.text = "($translation)"
                keyboard_subject_pronoun_label.text = subjectPronounLabel
                keyboard_subject_pronoun_label.visibility = if (subjectPronounLabel.isEmpty()) GONE else VISIBLE
                button_left_arrow.setImageDrawable(if (across) backwardArrowDrawable else upArrowDrawable)
                button_right_arrow.setImageDrawable(if (across) forwardArrowDrawable else downwardArrowDrawable)
            }
        }

    var elapsedSeconds: Long = 0
        set(value) {
            field = value
            val minutes = value / 60
            val seconds = value % 60
            keyboard_timer.text = "$minutes:${seconds.toString().padStart(2, '0')}"
        }

    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    val forwardArrowDrawable = resources.getDrawable(R.drawable.ic_baseline_arrow_forward_24px, null)
    val backwardArrowDrawable = resources.getDrawable(R.drawable.ic_baseline_arrow_back_24px, null)
    val upArrowDrawable = resources.getDrawable(R.drawable.ic_baseline_arrow_upward_24px, null)
    val downwardArrowDrawable = resources.getDrawable(R.drawable.ic_baseline_arrow_downward_24px, null)

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------
    //endregion
}

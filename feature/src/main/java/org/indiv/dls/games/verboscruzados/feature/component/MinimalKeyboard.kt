package org.indiv.dls.games.verboscruzados.feature.component

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.keyboard_minimal.view.*
import org.indiv.dls.games.verboscruzados.feature.R

/**
 * Minimal keyboard for entering answers while covering the least amount of puzzle possible.
 *
 * See: https://stackoverflow.com/questions/9577304/how-to-make-an-android-custom-keyboard/45005691#45005691
 */
open class MinimalKeyboard @JvmOverloads constructor(context: Context,
                                                     attrs: AttributeSet? = null,
                                                     defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    //region INITIALIZER ---------------------------------------------------------------------------

    init {
        inflate(context, R.layout.keyboard_minimal, this)

        val letterClickListener: (View) -> Unit = {
            (it as? TextView)?.let {
                inputConnection?.commitText(it.text, 1)
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
        button_u_umlaut.setOnClickListener { letterClickListener.invoke(it) }
        button_n_tilde.setOnClickListener { letterClickListener.invoke(it) }
        button_enter.setOnClickListener { dismissClickListener?.invoke() }
        button_infinitive.setOnClickListener {
            inputConnection?.commitText(infinitive, 1)
        }
        button_delete.setOnClickListener {
            val selectedText = inputConnection?.getSelectedText(0)
            if (selectedText.isNullOrEmpty()) {
                // no selection, so delete previous character
                inputConnection?.deleteSurroundingText(1, 0)
            } else {
                // delete the selection
                inputConnection?.commitText("", 1)
            }
        }
    }

    //endregion

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    // Caller must set this to the current EditText's input connection.
    var inputConnection: InputConnection? = null

    // Caller can set this to be notified when the user wants to dismiss the keyboard
    var dismissClickListener: (() -> Unit)? = null

    // Caller must set this for the infinitive button to insert the infinitive text.
    var infinitive: String = ""

    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------
    //endregion


    //region OVERRIDE FUNCTIONS --------------------------------------------------------------------

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------
    //endregion
}

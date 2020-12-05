package org.indiv.dls.games.verboscruzados.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import org.indiv.dls.games.verboscruzados.model.AnswerPresentation
import org.indiv.dls.games.verboscruzados.R
import org.indiv.dls.games.verboscruzados.Vibration
import org.indiv.dls.games.verboscruzados.databinding.KeyboardMinimalBinding

/**
 * Minimal keyboard for entering answers while covering the least amount of puzzle possible.
 *
 * See: https://stackoverflow.com/questions/9577304/how-to-make-an-android-custom-keyboard/45005691#45005691
 */
open class MinimalKeyboard @JvmOverloads constructor(context: Context,
                                                     attrs: AttributeSet? = null,
                                                     defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private var binding: KeyboardMinimalBinding

    private val forwardArrowDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_arrow_forward_24px, null)
    private val backwardArrowDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_arrow_back_24px, null)
    private val upArrowDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_arrow_upward_24px, null)
    private val downwardArrowDrawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_baseline_arrow_downward_24px, null)

    //endregion

    //region COMPANION OBJECT ----------------------------------------------------------------------
    //endregion

    //region INITIALIZER ---------------------------------------------------------------------------

    init {
        binding = KeyboardMinimalBinding.inflate(LayoutInflater.from(context), this)

        // Get instance of Vibrator from current Context
        val vibration = context.let {
            Vibration(it)
        }

        val letterClickListener: (View) -> Unit = {
            (it as? TextView)?.let {
                vibration.vibrate()
                letterClickListener?.invoke(it.text[0])
            }
        }

        binding.buttonA.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonB.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonC.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonD.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonE.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonF.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonG.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonH.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonI.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonJ.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonK.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonL.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonM.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonN.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonO.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonP.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonQ.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonR.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonS.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonT.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonU.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonV.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonW.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonX.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonY.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonZ.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonAAccent.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonEAccent.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonIAccent.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonOAccent.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonUAccent.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonUUmlaut.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonNTilde.setOnClickListener { letterClickListener.invoke(it) }
        binding.buttonHideKeyboard.setOnClickListener {
            vibration.vibrate()
            dismissClickListener?.invoke()
        }
        binding.keyboardButtonInfinitive.setOnClickListener {
            vibration.vibrate()
            infinitiveClickListener?.invoke(answerPresentation?.infinitive ?: "")
        }
        binding.buttonDelete.setOnClickListener {
            vibration.vibrate()
            deleteClickListener?.invoke()
        }
        binding.buttonDelete.setOnLongClickListener() {
            vibration.vibrate()
            deleteLongClickListener?.invoke()
            true
        }
        binding.buttonLeftArrow.setOnClickListener {
            vibration.vibrate()
            leftClickListener?.invoke()
        }
        binding.buttonRightArrow.setOnClickListener {
            vibration.vibrate()
            rightClickListener?.invoke()
        }
        binding.buttonNextWord.setOnClickListener {
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
                binding.keyboardConjugationTypeLabel.text = conjugationTypeLabel
                binding.keyboardButtonInfinitive.text = infinitive
                binding.keyboardTranslation.text = "($translation)"
                binding.keyboardSubjectPronounLabel.text = subjectPronounLabel
                binding.keyboardSubjectPronounLabel.visibility = if (subjectPronounLabel.isEmpty()) GONE else VISIBLE
                binding.buttonLeftArrow.setImageDrawable(if (across) backwardArrowDrawable else upArrowDrawable)
                binding.buttonRightArrow.setImageDrawable(if (across) forwardArrowDrawable else downwardArrowDrawable)
            }
        }

    var elapsedTime: String = ""
        set(value) {
            field = value
            binding.keyboardTimer.text = value
        }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------
    //endregion
}

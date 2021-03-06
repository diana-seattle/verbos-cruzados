package org.indiv.dls.games.verboscruzados.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatDialogFragment
import org.indiv.dls.games.verboscruzados.R
import org.indiv.dls.games.verboscruzados.databinding.FragmentGameOptionsDialogBinding
import org.indiv.dls.games.verboscruzados.util.GamePersistenceImpl
import org.indiv.dls.games.verboscruzados.model.ConjugationType
import org.indiv.dls.games.verboscruzados.model.InfinitiveEnding
import org.indiv.dls.games.verboscruzados.model.IrregularityCategory
import org.indiv.dls.games.verboscruzados.model.SubjectPronoun
import org.indiv.dls.games.verboscruzados.util.IdGenerator
import org.indiv.dls.games.verboscruzados.util.PersistenceConversions

/**
 * Dialog for selecting game options.
 */
class GameOptionsDialogFragment : AppCompatDialogFragment() {

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    var startNewGameListener: (() -> Unit)? = null

    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private lateinit var binding: FragmentGameOptionsDialogBinding

    private lateinit var gamePersistence: GamePersistenceImpl

    private val checkboxMap = mutableMapOf<String, CheckBox>()

    //endregion

    //region OVERRIDDEN FUNCTIONS ------------------------------------------------------------------

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentGameOptionsDialogBinding.inflate(LayoutInflater.from(context))

        gamePersistence = GamePersistenceImpl(requireContext(), PersistenceConversions(IdGenerator))

        // wrap activity with ContextThemeWrapper to get better dialog styling
        val dialog = AlertDialog.Builder(requireContext())
                .setNeutralButton(R.string.dialog_options_newgame) { _, _ ->
                    saveOptions()
                    startNewGameListener?.invoke()
                }
                .setPositiveButton(R.string.dialog_options_okay) { _, _ -> saveOptions() }
                .setView(binding.root)
                .create()

        // Set up map of option keys to checkboxes for easy access later.
        checkboxMap[IrregularityCategory.REGULAR.name] = binding.optionRegularityRegular
        checkboxMap[IrregularityCategory.SPELLING_CHANGE.name] = binding.optionRegularitySpellingChange
        checkboxMap[IrregularityCategory.STEM_CHANGE.name] = binding.optionRegularityStemChange
        checkboxMap[IrregularityCategory.IRREGULAR.name] = binding.optionRegularityOtherIrregular

        checkboxMap[InfinitiveEnding.AR.name] = binding.optionInfinitiveEndingAr
        checkboxMap[InfinitiveEnding.IR.name] = binding.optionInfinitiveEndingIr
        checkboxMap[InfinitiveEnding.ER.name] = binding.optionInfinitiveEndingEr

        checkboxMap[ConjugationType.PRESENT.name] = binding.optionTensePresent
        checkboxMap[ConjugationType.PRETERIT.name] = binding.optionTensePreterit
        checkboxMap[ConjugationType.IMPERFECT.name] = binding.optionTenseImperfect
        checkboxMap[ConjugationType.CONDITIONAL.name] = binding.optionTenseConditional
        checkboxMap[ConjugationType.FUTURE.name] = binding.optionTenseFuture
        checkboxMap[ConjugationType.IMPERATIVE.name] = binding.optionTenseImperative
        checkboxMap[ConjugationType.SUBJUNCTIVE_PRESENT.name] = binding.optionTenseSubjunctivePresent
        checkboxMap[ConjugationType.SUBJUNCTIVE_IMPERFECT.name] = binding.optionTenseSubjunctiveImperfect
        checkboxMap[ConjugationType.GERUND.name] = binding.optionTenseGerund
        checkboxMap[ConjugationType.PAST_PARTICIPLE.name] = binding.optionTensePastParticiple

        checkboxMap[SubjectPronoun.YO.name] = binding.optionSubjectPronounSingular
        checkboxMap[SubjectPronoun.TU.name] = binding.optionSubjectPronounSingular
        checkboxMap[SubjectPronoun.EL_ELLA_USTED.name] = binding.optionSubjectPronounSingular
        checkboxMap[SubjectPronoun.ELLOS_ELLAS_USTEDES.name] = binding.optionSubjectPronounPlural
        checkboxMap[SubjectPronoun.NOSOTROS.name] = binding.optionSubjectPronounPlural
        checkboxMap[SubjectPronoun.VOSOTROS.name] = binding.optionSubjectPronounVosotros

        initializeOptions()

        return dialog
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    private fun initializeOptions() {
        val optionMap = gamePersistence.currentGameOptions

        // Initialize checkboxes to checked or not based on persisted preferences
        checkboxMap.keys.forEach {
            checkboxMap[it]?.isChecked = optionMap[it] ?: false
        }
    }

    private fun saveOptions() {
        val optionMap = mutableMapOf<String, Boolean>()
        checkboxMap.keys.forEach {
            optionMap[it] = checkboxMap[it]?.isChecked ?: false
        }

        gamePersistence.currentGameOptions = optionMap
    }

    //endregion
}

package org.indiv.dls.games.verboscruzados.feature.dialog

import org.indiv.dls.games.verboscruzados.feature.R

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.CheckBox
import org.indiv.dls.games.verboscruzados.feature.game.PersistenceHelper
import org.indiv.dls.games.verboscruzados.feature.model.ConjugationType
import org.indiv.dls.games.verboscruzados.feature.model.InfinitiveEnding
import org.indiv.dls.games.verboscruzados.feature.model.IrregularityCategory
import org.indiv.dls.games.verboscruzados.feature.model.SubjectPronoun

/**
 * Dialog for selecting game options.
 */
class GameOptionsDialogFragment : DialogFragment() {

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    var startNewGameListener: (() -> Unit)? = null

    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private val checkboxMap = mutableMapOf<String, CheckBox>()
//    private lateinit var optionRegularityRegularCheckbox: CheckBox
//    private lateinit var optionRegularitySpellingChangeCheckbox: CheckBox
//    private lateinit var optionRegularityStemChangeCheckbox: CheckBox
//    private lateinit var optionRegularityOtherIrregularCheckbox: CheckBox
//
//    private lateinit var optionInfinitiveEndingArCheckbox: CheckBox
//    private lateinit var optionInfinitiveEndingIrCheckbox: CheckBox
//    private lateinit var optionInfinitiveEndingErCheckbox: CheckBox
//
//    private lateinit var optionTenseModePresentCheckbox: CheckBox
//    private lateinit var optionTenseModePreteritCheckbox: CheckBox
//    private lateinit var optionTenseModeImperfectCheckbox: CheckBox
//    private lateinit var optionTenseModeConditionalCheckbox: CheckBox
//    private lateinit var optionTenseModeFutureCheckbox: CheckBox
//    private lateinit var optionTenseModeImperativeCheckbox: CheckBox
//    private lateinit var optionTenseModeSubjunctivePresentCheckbox: CheckBox
//    private lateinit var optionTenseModeSubjunctiveImperfectCheckbox: CheckBox
//    private lateinit var optionTenseModeGerundCheckbox: CheckBox
//    private lateinit var optionTenseModePastParticipleCheckbox: CheckBox
//
//    private lateinit var optionSubjectPronounYoCheckbox: CheckBox
//    private lateinit var optionSubjectPronounTuCheckbox: CheckBox
//    private lateinit var optionSubjectPronounElEllaUdCheckbox: CheckBox
//    private lateinit var optionSubjectPronounEllosEllasUdsCheckbox: CheckBox
//    private lateinit var optionSubjectPronounNosotrosCheckbox: CheckBox
//    private lateinit var optionSubjectPronounVosotrosCheckbox: CheckBox

    //endregion

    //region OVERRIDDEN FUNCTIONS ------------------------------------------------------------------

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.fragment_game_options_dialog, null)

        // wrap activity with ContextThemeWrapper to get better dialog styling
        val dialog = AlertDialog.Builder(activity!!)
                .setNeutralButton(R.string.dialog_options_newgame) { _, _ ->
                    saveOptions()
                    startNewGameListener?.invoke()
                }
                .setPositiveButton(R.string.dialog_options_okay) { _, _ -> saveOptions() }
                .setNegativeButton(R.string.dialog_options_cancel) { _, _ -> }
                .setView(view)
                .create()

        dialog.setOnShowListener {
            val textColor = resources.getColor(R.color.colorAccent)
            dialog.getButton(DialogInterface.BUTTON_NEUTRAL)?.setTextColor(textColor)
            dialog.getButton(DialogInterface.BUTTON_POSITIVE)?.setTextColor(textColor)
        }

        // Set up map of option keys to checkboxes for easy access later.
        checkboxMap[IrregularityCategory.REGULAR.name] = view.findViewById(R.id.option_regularity_regular)
        checkboxMap[IrregularityCategory.SPELLING_CHANGE.name] = view.findViewById(R.id.option_regularity_spelling_change)
        checkboxMap[IrregularityCategory.STEM_CHANGE.name] = view.findViewById(R.id.option_regularity_stem_change)
        checkboxMap[IrregularityCategory.IRREGULAR.name] = view.findViewById(R.id.option_regularity_other_irregular)

        checkboxMap[InfinitiveEnding.AR.name] = view.findViewById(R.id.option_infinitive_ending_ar)
        checkboxMap[InfinitiveEnding.IR.name] = view.findViewById(R.id.option_infinitive_ending_ir)
        checkboxMap[InfinitiveEnding.ER.name] = view.findViewById(R.id.option_infinitive_ending_er)

        checkboxMap[ConjugationType.PRESENT.name] = view.findViewById(R.id.option_tense_present)
        checkboxMap[ConjugationType.PRETERIT.name] = view.findViewById(R.id.option_tense_preterit)
        checkboxMap[ConjugationType.IMPERFECT.name] = view.findViewById(R.id.option_tense_imperfect)
        checkboxMap[ConjugationType.CONDITIONAL.name] = view.findViewById(R.id.option_tense_conditional)
        checkboxMap[ConjugationType.FUTURE.name] = view.findViewById(R.id.option_tense_future)
        checkboxMap[ConjugationType.IMPERATIVE.name] = view.findViewById(R.id.option_tense_imperative)
        checkboxMap[ConjugationType.SUBJUNCTIVE_PRESENT.name] = view.findViewById(R.id.option_tense_subjunctive_present)
        checkboxMap[ConjugationType.SUBJUNCTIVE_IMPERFECT.name] = view.findViewById(R.id.option_tense_subjunctive_imperfect)
        checkboxMap[ConjugationType.GERUND.name] = view.findViewById(R.id.option_tense_gerund)
        checkboxMap[ConjugationType.PAST_PARTICIPLE.name] = view.findViewById(R.id.option_tense_past_participle)

        checkboxMap[SubjectPronoun.YO.name] = view.findViewById(R.id.option_subject_pronoun_yo)
        checkboxMap[SubjectPronoun.TU.name] = view.findViewById(R.id.option_subject_pronoun_tu)
        checkboxMap[SubjectPronoun.EL_ELLA_USTED.name] = view.findViewById(R.id.option_subject_pronoun_el_ella_ud)
        checkboxMap[SubjectPronoun.ELLOS_ELLAS_USTEDES.name] = view.findViewById(R.id.option_subject_pronoun_ellos_ellas_uds)
        checkboxMap[SubjectPronoun.NOSOTROS.name] = view.findViewById(R.id.option_subject_pronoun_nosotros)
        checkboxMap[SubjectPronoun.VOSOTROS.name] = view.findViewById(R.id.option_subject_pronoun_vosotros)

        initializeOptions()

        return dialog
    }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------
    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    private fun initializeOptions() {
        val persistenceHelper = PersistenceHelper(activity!!)
        val optionMap = persistenceHelper.currentGameOptions

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

        val persistenceHelper = PersistenceHelper(activity!!)
        persistenceHelper.persistGameOptions(optionMap)
    }

    //endregion
}

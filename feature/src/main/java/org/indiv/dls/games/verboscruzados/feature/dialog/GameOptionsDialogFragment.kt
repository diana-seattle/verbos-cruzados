package org.indiv.dls.games.verboscruzados.feature.dialog

import org.indiv.dls.games.verboscruzados.feature.R

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.view.ContextThemeWrapper
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
    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private lateinit var optionRegularityRegularCheckbox: CheckBox
    private lateinit var optionRegularitySpellingChangeCheckbox: CheckBox
    private lateinit var optionRegularityStemChangeCheckbox: CheckBox
    private lateinit var optionRegularityOtherIrregularCheckbox: CheckBox

    private lateinit var optionInfinitiveEndingArCheckbox: CheckBox
    private lateinit var optionInfinitiveEndingIrCheckbox: CheckBox
    private lateinit var optionInfinitiveEndingErCheckbox: CheckBox

    private lateinit var optionTenseModePresentCheckbox: CheckBox
    private lateinit var optionTenseModePreteritCheckbox: CheckBox
    private lateinit var optionTenseModeImperfectCheckbox: CheckBox
    private lateinit var optionTenseModeConditionalCheckbox: CheckBox
    private lateinit var optionTenseModeFutureCheckbox: CheckBox
    private lateinit var optionTenseModeImperativeCheckbox: CheckBox
    private lateinit var optionTenseModeSubjunctivePresentCheckbox: CheckBox
    private lateinit var optionTenseModeSubjunctiveImperfectCheckbox: CheckBox
    private lateinit var optionTenseModeGerundCheckbox: CheckBox
    private lateinit var optionTenseModePastParticipleCheckbox: CheckBox

    private lateinit var optionSubjectPronounYoCheckbox: CheckBox
    private lateinit var optionSubjectPronounTuCheckbox: CheckBox
    private lateinit var optionSubjectPronounElEllaUdCheckbox: CheckBox
    private lateinit var optionSubjectPronounEllosEllasUdsCheckbox: CheckBox
    private lateinit var optionSubjectPronounNosotrosCheckbox: CheckBox
    private lateinit var optionSubjectPronounVosotrosCheckbox: CheckBox

    //endregion

    //region PUBLIC INTERFACES ---------------------------------------------------------------------
    //endregion

    //region OVERRIDDEN FUNCTIONS ------------------------------------------------------------------

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = activity!!.layoutInflater
        val view = inflater.inflate(R.layout.fragment_game_options_dialog, null)

        // wrap activity with ContextThemeWrapper to get better dialog styling
//        val dialog = AlertDialog.Builder(ContextThemeWrapper(activity, android.R.style.Theme_Dialog))
        val dialog = AlertDialog.Builder(activity!!)
                .setNeutralButton(R.string.dialog_options_newgame) { _, _ ->
                    saveOptions()


                    // todo call listener

                }
                .setPositiveButton(R.string.dialog_options_okay) { _, _ -> saveOptions() }
                .setNegativeButton(R.string.dialog_options_cancel) { _, _ -> }
                .setView(view)
                .create()

        optionRegularityRegularCheckbox = view.findViewById(R.id.option_regularity_regular)
        optionRegularitySpellingChangeCheckbox = view.findViewById(R.id.option_regularity_spelling_change)
        optionRegularityStemChangeCheckbox = view.findViewById(R.id.option_regularity_stem_change)
        optionRegularityOtherIrregularCheckbox = view.findViewById(R.id.option_regularity_other_irregular)

        optionInfinitiveEndingArCheckbox = view.findViewById(R.id.option_infinitive_ending_ar)
        optionInfinitiveEndingIrCheckbox = view.findViewById(R.id.option_infinitive_ending_ir)
        optionInfinitiveEndingErCheckbox = view.findViewById(R.id.option_infinitive_ending_er)

        optionTenseModePresentCheckbox = view.findViewById(R.id.option_tense_present)
        optionTenseModePreteritCheckbox = view.findViewById(R.id.option_tense_preterit)
        optionTenseModeImperfectCheckbox = view.findViewById(R.id.option_tense_imperfect)
        optionTenseModeConditionalCheckbox = view.findViewById(R.id.option_tense_conditional)
        optionTenseModeFutureCheckbox = view.findViewById(R.id.option_tense_future)
        optionTenseModeImperativeCheckbox = view.findViewById(R.id.option_tense_imperative)
        optionTenseModeSubjunctivePresentCheckbox = view.findViewById(R.id.option_tense_subjunctive_present)
        optionTenseModeSubjunctiveImperfectCheckbox = view.findViewById(R.id.option_tense_subjunctive_imperfect)
        optionTenseModeGerundCheckbox = view.findViewById(R.id.option_tense_gerund)
        optionTenseModePastParticipleCheckbox = view.findViewById(R.id.option_tense_past_participle)

        optionSubjectPronounYoCheckbox = view.findViewById(R.id.option_subject_pronoun_yo)
        optionSubjectPronounTuCheckbox = view.findViewById(R.id.option_subject_pronoun_tu)
        optionSubjectPronounElEllaUdCheckbox = view.findViewById(R.id.option_subject_pronoun_el_ella_ud)
        optionSubjectPronounEllosEllasUdsCheckbox = view.findViewById(R.id.option_subject_pronoun_ellos_ellas_uds)
        optionSubjectPronounNosotrosCheckbox = view.findViewById(R.id.option_subject_pronoun_nosotros)
        optionSubjectPronounVosotrosCheckbox = view.findViewById(R.id.option_subject_pronoun_vosotros)

        initializeOptions()

        return dialog
    }

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------
    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    private fun initializeOptions() {
        val persistenceHelper = PersistenceHelper(activity!!)
        val map = persistenceHelper.currentGameOptions

        optionRegularityRegularCheckbox.isChecked = map[IrregularityCategory.REGULAR.name] ?: false
        optionRegularitySpellingChangeCheckbox.isChecked = map[IrregularityCategory.SPELLING_CHANGE.name] ?: false
        optionRegularityStemChangeCheckbox.isChecked = map[IrregularityCategory.STEM_CHANGE.name] ?: false
        optionRegularityOtherIrregularCheckbox.isChecked = map[IrregularityCategory.IRREGULAR.name] ?: false

        optionInfinitiveEndingArCheckbox.isChecked = map[InfinitiveEnding.AR.name] ?: false
        optionInfinitiveEndingIrCheckbox.isChecked = map[InfinitiveEnding.IR.name] ?: false
        optionInfinitiveEndingErCheckbox.isChecked = map[InfinitiveEnding.ER.name] ?: false

        optionTenseModePresentCheckbox.isChecked = map[ConjugationType.PRESENT.name] ?: false
        optionTenseModePreteritCheckbox.isChecked = map[ConjugationType.PRETERIT.name] ?: false
        optionTenseModeImperfectCheckbox.isChecked = map[ConjugationType.IMPERFECT.name] ?: false
        optionTenseModeConditionalCheckbox.isChecked = map[ConjugationType.CONDITIONAL.name] ?: false
        optionTenseModeFutureCheckbox.isChecked = map[ConjugationType.FUTURE.name] ?: false
        optionTenseModeImperativeCheckbox.isChecked = map[ConjugationType.IMPERATIVE.name] ?: false
        optionTenseModeSubjunctivePresentCheckbox.isChecked = map[ConjugationType.SUBJUNCTIVE_PRESENT.name] ?: false
        optionTenseModeSubjunctiveImperfectCheckbox.isChecked = map[ConjugationType.SUBJUNCTIVE_IMPERFECT.name] ?: false
        optionTenseModeGerundCheckbox.isChecked = map[ConjugationType.GERUND.name] ?: false
        optionTenseModePastParticipleCheckbox.isChecked = map[ConjugationType.PAST_PARTICIPLE.name] ?: false

        optionSubjectPronounYoCheckbox.isChecked = map[SubjectPronoun.YO.name] ?: false
        optionSubjectPronounTuCheckbox.isChecked = map[SubjectPronoun.TU.name] ?: false
        optionSubjectPronounElEllaUdCheckbox.isChecked = map[SubjectPronoun.EL_ELLA_USTED.name] ?: false
        optionSubjectPronounEllosEllasUdsCheckbox.isChecked = map[SubjectPronoun.ELLOS_ELLAS_USTEDES.name] ?: false
        optionSubjectPronounNosotrosCheckbox.isChecked = map[SubjectPronoun.NOSOTROS.name] ?: false
        optionSubjectPronounVosotrosCheckbox.isChecked = map[SubjectPronoun.VOSOTROS.name] ?: false
    }

    private fun saveOptions() {
        val map = mutableMapOf<String, Boolean>()
        map[IrregularityCategory.REGULAR.name] = optionRegularityRegularCheckbox.isChecked
        map[IrregularityCategory.SPELLING_CHANGE.name] = optionRegularitySpellingChangeCheckbox.isChecked
        map[IrregularityCategory.STEM_CHANGE.name] = optionRegularityStemChangeCheckbox.isChecked
        map[IrregularityCategory.IRREGULAR.name] = optionRegularityOtherIrregularCheckbox.isChecked

        map[InfinitiveEnding.AR.name] = optionInfinitiveEndingArCheckbox.isChecked
        map[InfinitiveEnding.IR.name] = optionInfinitiveEndingIrCheckbox.isChecked
        map[InfinitiveEnding.ER.name] = optionInfinitiveEndingErCheckbox.isChecked

        map[ConjugationType.PRESENT.name] = optionTenseModePresentCheckbox.isChecked
        map[ConjugationType.PRETERIT.name] = optionTenseModePreteritCheckbox.isChecked
        map[ConjugationType.IMPERFECT.name] = optionTenseModeImperfectCheckbox.isChecked
        map[ConjugationType.CONDITIONAL.name] = optionTenseModeConditionalCheckbox.isChecked
        map[ConjugationType.FUTURE.name] = optionTenseModeFutureCheckbox.isChecked
        map[ConjugationType.IMPERATIVE.name] = optionTenseModeImperativeCheckbox.isChecked
        map[ConjugationType.SUBJUNCTIVE_PRESENT.name] = optionTenseModeSubjunctivePresentCheckbox.isChecked
        map[ConjugationType.SUBJUNCTIVE_IMPERFECT.name] = optionTenseModeSubjunctiveImperfectCheckbox.isChecked
        map[ConjugationType.GERUND.name] = optionTenseModeGerundCheckbox.isChecked
        map[ConjugationType.PAST_PARTICIPLE.name] = optionTenseModePastParticipleCheckbox.isChecked

        map[SubjectPronoun.YO.name] = optionSubjectPronounYoCheckbox.isChecked
        map[SubjectPronoun.TU.name] = optionSubjectPronounTuCheckbox.isChecked
        map[SubjectPronoun.EL_ELLA_USTED.name] = optionSubjectPronounElEllaUdCheckbox.isChecked
        map[SubjectPronoun.ELLOS_ELLAS_USTEDES.name] = optionSubjectPronounEllosEllasUdsCheckbox.isChecked
        map[SubjectPronoun.NOSOTROS.name] = optionSubjectPronounNosotrosCheckbox.isChecked
        map[SubjectPronoun.VOSOTROS.name] = optionSubjectPronounVosotrosCheckbox.isChecked

        val persistenceHelper = PersistenceHelper(activity!!)
        persistenceHelper.persistGameOptions(map)
    }

    //endregion
}

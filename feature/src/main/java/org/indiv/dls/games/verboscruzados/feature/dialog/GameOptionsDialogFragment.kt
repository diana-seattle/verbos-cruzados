package org.indiv.dls.games.verboscruzados.feature.dialog

import org.indiv.dls.games.verboscruzados.feature.R

import android.app.Dialog
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

        optionRegularityRegularCheckbox.isSelected = map[IrregularityCategory.REGULAR.name] ?: false
        optionRegularitySpellingChangeCheckbox.isSelected = map[IrregularityCategory.SPELLING_CHANGE.name] ?: false
        optionRegularityStemChangeCheckbox.isSelected = map[IrregularityCategory.STEM_CHANGE.name] ?: false
        optionRegularityOtherIrregularCheckbox.isSelected = map[IrregularityCategory.IRREGULAR.name] ?: false

        optionInfinitiveEndingArCheckbox.isSelected = map[InfinitiveEnding.AR.name] ?: false
        optionInfinitiveEndingIrCheckbox.isSelected = map[InfinitiveEnding.IR.name] ?: false
        optionInfinitiveEndingErCheckbox.isSelected = map[InfinitiveEnding.ER.name] ?: false

        optionTenseModePresentCheckbox.isSelected = map[ConjugationType.PRESENT.name] ?: false
        optionTenseModePreteritCheckbox.isSelected = map[ConjugationType.PRETERIT.name] ?: false
        optionTenseModeImperfectCheckbox.isSelected = map[ConjugationType.IMPERFECT.name] ?: false
        optionTenseModeConditionalCheckbox.isSelected = map[ConjugationType.CONDITIONAL.name] ?: false
        optionTenseModeFutureCheckbox.isSelected = map[ConjugationType.FUTURE.name] ?: false
        optionTenseModeImperativeCheckbox.isSelected = map[ConjugationType.IMPERATIVE.name] ?: false
        optionTenseModeSubjunctivePresentCheckbox.isSelected = map[ConjugationType.SUBJUNCTIVE_PRESENT.name] ?: false
        optionTenseModeSubjunctiveImperfectCheckbox.isSelected = map[ConjugationType.SUBJUNCTIVE_IMPERFECT.name] ?: false
        optionTenseModeGerundCheckbox.isSelected = map[ConjugationType.GERUND.name] ?: false
        optionTenseModePastParticipleCheckbox.isSelected = map[ConjugationType.PAST_PARTICIPLE.name] ?: false

        optionSubjectPronounYoCheckbox.isSelected = map[SubjectPronoun.YO.name] ?: false
        optionSubjectPronounTuCheckbox.isSelected = map[SubjectPronoun.TU.name] ?: false
        optionSubjectPronounElEllaUdCheckbox.isSelected = map[SubjectPronoun.EL_ELLA_USTED.name] ?: false
        optionSubjectPronounEllosEllasUdsCheckbox.isSelected = map[SubjectPronoun.ELLOS_ELLAS_USTEDES.name] ?: false
        optionSubjectPronounNosotrosCheckbox.isSelected = map[SubjectPronoun.NOSOTROS.name] ?: false
        optionSubjectPronounVosotrosCheckbox.isSelected = map[SubjectPronoun.VOSOTROS.name] ?: false
    }

    private fun saveOptions() {
        val map = mutableMapOf<String, Boolean>()
        map[IrregularityCategory.REGULAR.name] = optionRegularityRegularCheckbox.isSelected
        map[IrregularityCategory.SPELLING_CHANGE.name] = optionRegularitySpellingChangeCheckbox.isSelected
        map[IrregularityCategory.STEM_CHANGE.name] = optionRegularityStemChangeCheckbox.isSelected
        map[IrregularityCategory.IRREGULAR.name] = optionRegularityOtherIrregularCheckbox.isSelected

        map[InfinitiveEnding.AR.name] = optionInfinitiveEndingArCheckbox.isSelected
        map[InfinitiveEnding.IR.name] = optionInfinitiveEndingIrCheckbox.isSelected
        map[InfinitiveEnding.ER.name] = optionInfinitiveEndingErCheckbox.isSelected

        map[ConjugationType.PRESENT.name] = optionTenseModePresentCheckbox.isSelected
        map[ConjugationType.PRETERIT.name] = optionTenseModePreteritCheckbox.isSelected
        map[ConjugationType.IMPERFECT.name] = optionTenseModeImperfectCheckbox.isSelected
        map[ConjugationType.CONDITIONAL.name] = optionTenseModeConditionalCheckbox.isSelected
        map[ConjugationType.FUTURE.name] = optionTenseModeFutureCheckbox.isSelected
        map[ConjugationType.IMPERATIVE.name] = optionTenseModeImperativeCheckbox.isSelected
        map[ConjugationType.SUBJUNCTIVE_PRESENT.name] = optionTenseModeSubjunctivePresentCheckbox.isSelected
        map[ConjugationType.SUBJUNCTIVE_IMPERFECT.name] = optionTenseModeSubjunctiveImperfectCheckbox.isSelected
        map[ConjugationType.GERUND.name] = optionTenseModeGerundCheckbox.isSelected
        map[ConjugationType.PAST_PARTICIPLE.name] = optionTenseModePastParticipleCheckbox.isSelected

        map[SubjectPronoun.YO.name] = optionSubjectPronounYoCheckbox.isSelected
        map[SubjectPronoun.TU.name] = optionSubjectPronounTuCheckbox.isSelected
        map[SubjectPronoun.EL_ELLA_USTED.name] = optionSubjectPronounElEllaUdCheckbox.isSelected
        map[SubjectPronoun.ELLOS_ELLAS_USTEDES.name] = optionSubjectPronounEllosEllasUdsCheckbox.isSelected
        map[SubjectPronoun.NOSOTROS.name] = optionSubjectPronounNosotrosCheckbox.isSelected
        map[SubjectPronoun.VOSOTROS.name] = optionSubjectPronounVosotrosCheckbox.isSelected

        val persistenceHelper = PersistenceHelper(activity!!)
        persistenceHelper.persistGameOptions(map)
    }

    //endregion
}

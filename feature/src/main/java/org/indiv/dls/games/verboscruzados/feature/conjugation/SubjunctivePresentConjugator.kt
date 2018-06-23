package org.indiv.dls.games.verboscruzados.feature.conjugation

import org.indiv.dls.games.verboscruzados.feature.model.ConjugationType
import org.indiv.dls.games.verboscruzados.feature.model.InfinitiveEnding
import org.indiv.dls.games.verboscruzados.feature.model.Irregularity
import org.indiv.dls.games.verboscruzados.feature.model.SubjectPronoun
import org.indiv.dls.games.verboscruzados.feature.model.Verb

/**
 * Subjunctive Present tense conjugator
 */
open class SubjunctivePresentConjugator : Conjugator {
    private val presentConjugator = PresentConjugator()
    private val arSubjectSuffixMap = mapOf(
            SubjectPronoun.YO to "e",
            SubjectPronoun.TU to "es",
            SubjectPronoun.EL_ELLA_USTED to "e",
            SubjectPronoun.ELLOS_ELLAS_USTEDES to "en",
            SubjectPronoun.NOSOTROS to "emos",
            SubjectPronoun.VOSOTROS to "éis")
    private val irErSubjectSuffixMap = mapOf(
            SubjectPronoun.YO to "a",
            SubjectPronoun.TU to "as",
            SubjectPronoun.EL_ELLA_USTED to "a",
            SubjectPronoun.ELLOS_ELLAS_USTEDES to "an",
            SubjectPronoun.NOSOTROS to "amos",
            SubjectPronoun.VOSOTROS to "áis")
    private val mapOfSuffixMaps = mapOf(
            InfinitiveEnding.AR to arSubjectSuffixMap,
            InfinitiveEnding.IR to irErSubjectSuffixMap,
            InfinitiveEnding.ER to irErSubjectSuffixMap)

    override fun conjugate(verb: Verb, subjectPronoun: SubjectPronoun): String {
        return verb.customConjugation?.invoke(subjectPronoun, ConjugationType.SUBJUNCTIVE_PRESENT)
                ?: run {
                    val suffix = mapOfSuffixMaps[verb.infinitiveEnding]!![subjectPronoun]!!
                    val isSpecialYoRoot = presentConjugator.isStemChangeAppliedToYoRoot(verb)
                    val yoRoot = getYoRoot(verb)
                    val subjunctiveRoot = if (!isSpecialYoRoot && subjectPronoun.isNosotrosOrVosotros)
                        getIrAlteredRoot(yoRoot, verb.irregularities)
                    else
                        yoRoot
                    val root = getRootWithSpellingChange(subjunctiveRoot, "o", suffix)
                    return root + suffix
                }
    }

    private fun getYoRoot(verb: Verb): String {
        val presentYoForm = presentConjugator.conjugate(verb, SubjectPronoun.YO)
        return presentYoForm.dropLast(if (presentYoForm.endsWith("oy")) 2 else 1)
    }

}
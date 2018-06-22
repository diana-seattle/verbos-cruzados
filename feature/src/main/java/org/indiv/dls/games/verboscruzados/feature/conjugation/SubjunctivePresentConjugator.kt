package org.indiv.dls.games.verboscruzados.feature.conjugation

import org.indiv.dls.games.verboscruzados.feature.model.ConjugationType
import org.indiv.dls.games.verboscruzados.feature.model.InfinitiveEnding
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
            SubjectPronoun.YO to "e",
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
        return verb.customConjugation?.invoke(subjectPronoun, ConjugationType.SUBJUNCTIVE_PRESENT) ?: run {
            val suffix = mapOfSuffixMaps[verb.infinitiveEnding]!![subjectPronoun]!!
            val subjunctiveRoot = when(subjectPronoun) {
                SubjectPronoun.NOSOTROS, SubjectPronoun.VOSOTROS -> verb.root
                else -> getYoRoot(verb, subjectPronoun)
            }
            val root = getRootWithSpellingChange(subjunctiveRoot, verb.infinitiveEnding, suffix)
            return root + suffix
        }
    }

    private fun getYoRoot(verb: Verb, subjectPronoun: SubjectPronoun): String {
        val presentYoForm = presentConjugator.conjugate(verb, SubjectPronoun.YO)
        return if (presentYoForm.endsWith("oy")) {
            presentYoForm.substring(0, presentYoForm.length - 2)
        } else {
            presentYoForm.substring(0, presentYoForm.length -1)
        }
    }
}
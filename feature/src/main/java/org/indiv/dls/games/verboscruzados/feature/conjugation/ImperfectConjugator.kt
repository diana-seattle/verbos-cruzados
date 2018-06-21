package org.indiv.dls.games.verboscruzados.feature.conjugation

import org.indiv.dls.games.verboscruzados.feature.model.ConjugationType
import org.indiv.dls.games.verboscruzados.feature.model.InfinitiveEnding
import org.indiv.dls.games.verboscruzados.feature.model.Irregularity
import org.indiv.dls.games.verboscruzados.feature.model.SubjectPronoun
import org.indiv.dls.games.verboscruzados.feature.model.Verb

/**
 * Present tense conjugator
 */
class ImperfectConjugator : Conjugator {
    private val arSubjectSuffixMap = mapOf(
            SubjectPronoun.YO to "aba",
            SubjectPronoun.TU to "abas",
            SubjectPronoun.EL_ELLA_USTED to "aba",
            SubjectPronoun.ELLOS_ELLAS_USTEDES to "aban",
            SubjectPronoun.NOSOTROS to "ábamos",
            SubjectPronoun.VOSOTROS to "abais")
    private val irErSubjectSuffixMap = mapOf(
            SubjectPronoun.YO to "ía",
            SubjectPronoun.TU to "ías",
            SubjectPronoun.EL_ELLA_USTED to "ía",
            SubjectPronoun.ELLOS_ELLAS_USTEDES to "ían",
            SubjectPronoun.NOSOTROS to "íamos",
            SubjectPronoun.VOSOTROS to "íais")
    private val mapOfSuffixMaps = mapOf(
            InfinitiveEnding.AR to arSubjectSuffixMap,
            InfinitiveEnding.IR to irErSubjectSuffixMap,
            InfinitiveEnding.ER to irErSubjectSuffixMap)

    override fun conjugate(verb: Verb, subjectPronoun: SubjectPronoun): String {
        return verb.customConjugation?.invoke(subjectPronoun, ConjugationType.IMPERFECT) ?: run {
            val suffix = mapOfSuffixMaps[verb.infinitiveEnding]!![subjectPronoun]!!
            val root = verb.root
            return root + suffix
        }
    }
}
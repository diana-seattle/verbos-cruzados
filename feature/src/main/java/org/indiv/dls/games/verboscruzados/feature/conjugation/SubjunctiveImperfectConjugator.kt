package org.indiv.dls.games.verboscruzados.feature.conjugation

import org.indiv.dls.games.verboscruzados.feature.model.ConjugationType
import org.indiv.dls.games.verboscruzados.feature.model.InfinitiveEnding
import org.indiv.dls.games.verboscruzados.feature.model.Irregularity
import org.indiv.dls.games.verboscruzados.feature.model.SubjectPronoun
import org.indiv.dls.games.verboscruzados.feature.model.Verb

/**
 * Subjunctive Imperfect conjugator
 */
class SubjunctiveImperfectConjugator : Conjugator {
    private val preteritConjugator = PreteritConjugator()
    private val subjectSuffixMap = mapOf(
            SubjectPronoun.YO to "ra",
            SubjectPronoun.TU to "ras",
            SubjectPronoun.EL_ELLA_USTED to "ra",
            SubjectPronoun.ELLOS_ELLAS_USTEDES to "ran",
            SubjectPronoun.NOSOTROS to "ramos",
            SubjectPronoun.VOSOTROS to "rais")

    override fun conjugate(verb: Verb, subjectPronoun: SubjectPronoun): String {
        return verb.customConjugation?.invoke(subjectPronoun, ConjugationType.SUBJUNCTIVE_IMPERFECT) ?: run {
            val suffix = subjectSuffixMap[subjectPronoun]!!
            val root = getPreteritRoot(verb, subjectPronoun)
            return root + suffix
        }
    }

    private fun getPreteritRoot(verb: Verb, subjectPronoun: SubjectPronoun): String {
        val preteritForm = preteritConjugator.conjugate(verb, SubjectPronoun.ELLOS_ELLAS_USTEDES)
        var preteritRoot = preteritForm.substring(0, preteritForm.length - 3)
        if (subjectPronoun == SubjectPronoun.NOSOTROS) {
            if (verb.infinitiveEnding == InfinitiveEnding.AR) {
                preteritRoot = replaceLastOccurrence(preteritRoot, 'a', "á")
            } else {
                preteritRoot = replaceLastOccurrence(preteritRoot, 'e', "é")
            }
        }
        return preteritRoot
    }
}
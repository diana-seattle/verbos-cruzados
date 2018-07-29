package org.indiv.dls.games.verboscruzados.feature.conjugation

import org.indiv.dls.games.verboscruzados.feature.model.ConjugationType
import org.indiv.dls.games.verboscruzados.feature.model.SubjectPronoun
import org.indiv.dls.games.verboscruzados.feature.model.Verb

/**
 * Future tense conjugator
 */
class FutureConjugator : Conjugator {
    private val subjectSuffixMap = mapOf(
            SubjectPronoun.YO to "é",
            SubjectPronoun.TU to "ás",
            SubjectPronoun.EL_ELLA_USTED to "á",
            SubjectPronoun.ELLOS_ELLAS_USTEDES to "án",
            SubjectPronoun.NOSOTROS to "emos",
            SubjectPronoun.VOSOTROS to "éis")

    override fun conjugate(verb: Verb, subjectPronoun: SubjectPronoun): String {
        return verb.customConjugation?.invoke(subjectPronoun, ConjugationType.FUTURE) ?: run {
            val suffix = subjectSuffixMap[subjectPronoun]!!
            val root = verb.altInfinitiveRoot ?: verb.infinitive
            return root + suffix
        }
    }
}
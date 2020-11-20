package org.indiv.dls.games.verboscruzados.conjugation

import org.indiv.dls.games.verboscruzados.model.ConjugationType
import org.indiv.dls.games.verboscruzados.model.SubjectPronoun
import org.indiv.dls.games.verboscruzados.model.Verb

/**
 * Conditional tense conjugator
 */
class ConditionalConjugator : Conjugator {
    private val subjectSuffixMap = mapOf(
            SubjectPronoun.YO to "ía",
            SubjectPronoun.TU to "ías",
            SubjectPronoun.EL_ELLA_USTED to "ía",
            SubjectPronoun.ELLOS_ELLAS_USTEDES to "ían",
            SubjectPronoun.NOSOTROS to "íamos",
            SubjectPronoun.VOSOTROS to "íais")

    override fun conjugate(verb: Verb, subjectPronoun: SubjectPronoun): String {
        return verb.customConjugation?.invoke(subjectPronoun, ConjugationType.CONDITIONAL) ?: run {
            val suffix = subjectSuffixMap[subjectPronoun]!!
            val root = verb.altInfinitiveRoot ?: verb.infinitive
            return root + suffix
        }
    }
}
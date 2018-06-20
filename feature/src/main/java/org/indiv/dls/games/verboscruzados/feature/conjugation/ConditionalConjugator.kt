package org.indiv.dls.games.verboscruzados.feature.conjugation

import org.indiv.dls.games.verboscruzados.feature.model.ConjugationType
import org.indiv.dls.games.verboscruzados.feature.model.InfinitiveEnding
import org.indiv.dls.games.verboscruzados.feature.model.Irregularity
import org.indiv.dls.games.verboscruzados.feature.model.SubjectPronoun
import org.indiv.dls.games.verboscruzados.feature.model.Verb

/**
 * Present tense conjugator
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
        return verb.customConjugation?.invoke(subjectPronoun, ConjugationType.PRESENT) ?: run {
            val suffix = subjectSuffixMap[subjectPronoun]!!
            val root = verb.altInfinitiveRoot ?: verb.infinitive
            return root + suffix
        }
    }
}
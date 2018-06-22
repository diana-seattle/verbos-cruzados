package org.indiv.dls.games.verboscruzados.feature.conjugation

import org.indiv.dls.games.verboscruzados.feature.model.ConjugationType
import org.indiv.dls.games.verboscruzados.feature.model.InfinitiveEnding
import org.indiv.dls.games.verboscruzados.feature.model.Irregularity
import org.indiv.dls.games.verboscruzados.feature.model.SubjectPronoun
import org.indiv.dls.games.verboscruzados.feature.model.Verb

/**
 * Imperative conjugator
 */
class ImperativeConjugator : SubjunctivePresentConjugator() {
    private val presentConjugator = PresentConjugator()

    override fun conjugate(verb: Verb, subjectPronoun: SubjectPronoun): String {
        return verb.customConjugation?.invoke(subjectPronoun, ConjugationType.IMPERATIVE) ?: run {
            when (subjectPronoun) {
                SubjectPronoun.TU -> presentConjugator.conjugate(verb, SubjectPronoun.EL_ELLA_USTED)
                SubjectPronoun.VOSOTROS -> verb.root + "ad"
                else -> super.conjugate(verb, subjectPronoun)
            }
        }
    }
}
package org.indiv.dls.games.verboscruzados.conjugation

import org.indiv.dls.games.verboscruzados.model.ConjugationType
import org.indiv.dls.games.verboscruzados.model.SubjectPronoun
import org.indiv.dls.games.verboscruzados.model.Verb

/**
 * Imperative conjugator
 */
class ImperativeConjugator : SubjunctivePresentConjugator() {
    private val presentConjugator = PresentConjugator()

    override fun conjugate(verb: Verb, subjectPronoun: SubjectPronoun): String {
        return verb.customConjugation?.invoke(subjectPronoun, ConjugationType.IMPERATIVE) ?: run {
            when (subjectPronoun) {
                SubjectPronoun.YO -> "-"
                SubjectPronoun.TU -> verb.irregularImperativeTu
                        ?: presentConjugator.conjugate(verb, SubjectPronoun.EL_ELLA_USTED)
                SubjectPronoun.VOSOTROS -> verb.infinitive.dropLast(1) + "d"
                else -> super.conjugate(verb, subjectPronoun)
            }
        }
    }
}
package org.indiv.dls.games.verboscruzados.feature.conjugation

import org.indiv.dls.games.verboscruzados.feature.model.ConjugationType
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
        return verb.customConjugation?.invoke(subjectPronoun, ConjugationType.SUBJUNCTIVE_IMPERFECT)
                ?: run {
                    val suffix = subjectSuffixMap[subjectPronoun]!!
                    val root = getPreteritRoot(verb, subjectPronoun)
                    return root + suffix
                }
    }

    private fun getPreteritRoot(verb: Verb, subjectPronoun: SubjectPronoun): String {
        val preteritForm = preteritConjugator.conjugate(verb, SubjectPronoun.ELLOS_ELLAS_USTEDES)
        var preteritRoot = preteritForm.dropLast(3) // dropping "ron" of "aron" or "ieron"
        if (subjectPronoun == SubjectPronoun.NOSOTROS) {
            val lastLetterOfRoot = addAccent(preteritRoot.last())
            preteritRoot = preteritRoot.dropLast(1) + lastLetterOfRoot
        }
        return preteritRoot
    }

    private fun addAccent(char: Char): Char {
        return when (char) {
            'a' -> 'á'
            'e' -> 'é'
            else -> char
        }
    }
}
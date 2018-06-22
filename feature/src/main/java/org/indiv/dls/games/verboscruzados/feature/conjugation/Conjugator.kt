package org.indiv.dls.games.verboscruzados.feature.conjugation

import org.indiv.dls.games.verboscruzados.feature.model.ConjugationType
import org.indiv.dls.games.verboscruzados.feature.model.InfinitiveEnding
import org.indiv.dls.games.verboscruzados.feature.model.SubjectPronoun
import org.indiv.dls.games.verboscruzados.feature.model.Verb

interface Conjugator {
    fun conjugate(verb: Verb, subjectPronoun: SubjectPronoun): String
}

val conjugatorMap = mapOf(
        ConjugationType.PRESENT to PresentConjugator(),
        ConjugationType.PRETERIT to PreteritConjugator(),
        ConjugationType.IMPERFECT to ImperfectConjugator(),
        ConjugationType.CONDITIONAL to ConditionalConjugator(),
        ConjugationType.FUTURE to FutureConjugator(),
        ConjugationType.IMPERATIVE to ImperativeConjugator(),
        ConjugationType.SUBJUNCTIVE_PRESENT to SubjunctivePresentConjugator(),
        ConjugationType.SUBJUNCTIVE_IMPERFECT to SubjunctiveImperfectConjugator()
)

/**
 * Gets root with spelling changes (e.g. llegar -> llegue and llegué).
 * For use with several conjugation types.
 */
internal fun getRootWithSpellingChange(root: String, infinitiveEnding: InfinitiveEnding, suffix: String): String {
    if (infinitiveEnding == InfinitiveEnding.AR &&
            suffix.startsWith("e") || suffix.startsWith("é")) {
        root.apply {
            return when {
                endsWith("c") -> substring(0, length - 1) + "qu" // e.g. tocar -> toquemos
                endsWith("z") -> substring(0, length - 1) + "c"  // e.g. gozar -> gocemos, alcanzar -> alcancé
                endsWith("g") -> this + "u"                      // e.g. negar -> neguemos
                endsWith("gu") -> substring(0, length - 1) + "ü" // e.g. averiguar -> averigüemos
                else -> this
            }
        }
    } else if (infinitiveEnding != InfinitiveEnding.AR &&
            (suffix.startsWith("a") || suffix.startsWith("á") || suffix.startsWith("o"))) {
        root.apply {
            return when {
                endsWith("qu") -> substring(0, length - 2) + "c" // e.g. delinquir -> delincamos
                endsWith("c") -> substring(0, length - 1) + "z"  // e.g. vencer -> venzo, venzamos
                endsWith("gu") -> substring(0, length - 1)       // e.g. distinguir -> distingo, distingamos
                endsWith("g") -> substring(0, length - 1) + "j"  // e.g. proteger -> protejo, protejamos
                else -> this
            }
        }
    }
    return root
}

/**
 * Replaces last occurrence of a character with a string
 */
internal fun replaceLastOccurrence(source: String, char: Char, replacement: String): String {
    val index = source.lastIndexOf(char)
    if (index != -1) {
        val start = source.substring(0, index)
        val end = if (index < source.length - 1) source.subSequence(index + 1, source.length) else ""
        return start + replacement + end
    }
    return source
}


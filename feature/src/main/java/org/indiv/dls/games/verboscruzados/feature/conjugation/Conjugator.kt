package org.indiv.dls.games.verboscruzados.feature.conjugation

import org.indiv.dls.games.verboscruzados.feature.model.ConjugationType
import org.indiv.dls.games.verboscruzados.feature.model.InfinitiveEnding
import org.indiv.dls.games.verboscruzados.feature.model.Irregularity
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
internal fun getRootWithSpellingChange(root: String, oldSuffix: String, newSuffix: String): String {
    val hardOldSuffix = oldSuffix.startsWith("a") || oldSuffix.startsWith("o")
    if (hardOldSuffix && (newSuffix.startsWith("e") || newSuffix.startsWith("é"))) {
        root.apply {
            return when {
                endsWith("c") -> dropLast(1) + "qu" // e.g. tocar -> toquemos
                endsWith("z") -> dropLast(1) + "c"  // e.g. gozar -> gocemos, alcanzar -> alcancé
                endsWith("g") -> this + "u"            // e.g. negar -> neguemos
                endsWith("gu") -> dropLast(1) + "ü" // e.g. averiguar -> averigüemos
                else -> this
            }
        }
    } else if (!hardOldSuffix &&
            (newSuffix.startsWith("a") || newSuffix.startsWith("á") || newSuffix.startsWith("o"))) {
        root.apply {
            return when {
                endsWith("qu") -> dropLast(2) + "c" // e.g. delinquir -> delincamos
                endsWith("c") -> dropLast(1) + "z"  // e.g. vencer -> venzo, venzamos
                endsWith("gu") -> dropLast(1)       // e.g. distinguir -> distingo, distingamos
                endsWith("g") -> dropLast(1) + "j"  // e.g. proteger -> protejo, protejamos
                else -> this
            }
        }
    }
    return root
}

internal fun getIrAlteredRoot(root: String, irregularities: List<Irregularity>): String {
    return when {
        irregularities.contains(Irregularity.STEM_CHANGE_E_to_I) -> replaceLastOccurrence(root, 'e', "i")
        irregularities.contains(Irregularity.STEM_CHANGE_E_to_IE) -> replaceLastOccurrence(root, 'e', "i")
        irregularities.contains(Irregularity.STEM_CHANGE_O_to_UE) -> replaceLastOccurrence(root, 'o', "u")
        else -> root
    }
}

/**
 * Replaces last occurrence of a character with a string
 */
internal fun replaceLastOccurrence(source: String, char: Char, replacement: String): String {
    val index = source.lastIndexOf(char)
    if (index != -1) {
        val end = if (index < source.length - 1) source.substring(index + 1, source.length) else ""
        return source.substring(0, index) + replacement + end
    }
    return source
}


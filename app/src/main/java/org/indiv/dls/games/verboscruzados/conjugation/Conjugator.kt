package org.indiv.dls.games.verboscruzados.conjugation

import org.indiv.dls.games.verboscruzados.model.ConjugationType
import org.indiv.dls.games.verboscruzados.model.Irregularity
import org.indiv.dls.games.verboscruzados.model.SubjectPronoun
import org.indiv.dls.games.verboscruzados.model.Verb

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
    val hardOldSuffix = oldSuffix.take(1) in listOf("a", "o")
    if (root.takeLast(1) in listOf("o", "u") &&
            root.takeLast(2) !in listOf("gu", "qu") &&
            oldSuffix.take(2) in listOf("ir", "ír") &&
            newSuffix.take(1) !in listOf("i", "í", "y")) {
        return root + "y"  // e.g. construir -> construyo, oír -> oyes
    } else if (hardOldSuffix && (newSuffix.startsWith("e") || newSuffix.startsWith("é"))) {
        root.apply {
            return when {
                endsWith("c") -> dropLast(1) + "qu" // e.g. tocar -> toquemos
                endsWith("z") -> dropLast(1) + "c"  // e.g. gozar -> gocemos, alcanzar -> alcancé
                endsWith("g") -> this + "u"            // e.g. negar -> neguemos
                endsWith("gu") -> dropLast(1) + "ü" // e.g. averiguar -> averigüemos
                else -> this
            }
        }
    } else if (!hardOldSuffix && newSuffix.take(1) in listOf("a", "á", "o")) {
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
        irregularities.contains(Irregularity.STEM_CHANGE_E_to_I) -> replaceInLastSyllable(root, "e", "i")
        irregularities.contains(Irregularity.STEM_CHANGE_E_to_ACCENTED_I) -> replaceInLastSyllable(root, "e", "i")
        irregularities.contains(Irregularity.STEM_CHANGE_E_to_IE) -> replaceInLastSyllable(root, "e", "i")
        irregularities.contains(Irregularity.STEM_CHANGE_O_to_UE) -> replaceInLastSyllable(root, "o", "u")
        else -> root
    }
}

internal fun replaceInLastSyllable(text: String, old: String, new: String): String {
    val index = text.lastIndexOf(old)
    if (index != -1) {
        val textBeginning = text.substring(0, index) // drops all but the ending containing the stem
        val textEnd = text.substring(textBeginning.length)
        return textBeginning + textEnd.replace(old, new)
    }
    return text
}

internal fun anyVowels(text: String): Boolean {
    return text.matches(Regex(".*[aeiouáéíóú]+.*"))
}

internal fun anyStrongVowels(text: String): Boolean {
    return text.matches(Regex(".*[aeoáéíóú]+.*"))
}

internal fun isWeakOneSyllableRoot(root: String): Boolean {
    return !anyVowels(root.dropLast(2)) && !anyStrongVowels(root.takeLast(2)) && anyVowels(root.takeLast(1))
}

internal fun removeStartingAccent(text: String): String {
    return when (text.take(1)) {
        "á" -> "a" + text.drop(1)
        "é" -> "e" + text.drop(1)
        "í" -> "i" + text.drop(1)
        "ó" -> "o" + text.drop(1)
        else -> text
    }
}
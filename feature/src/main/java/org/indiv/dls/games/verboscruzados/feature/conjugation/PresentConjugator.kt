package org.indiv.dls.games.verboscruzados.feature.conjugation

import org.indiv.dls.games.verboscruzados.feature.model.ConjugationType
import org.indiv.dls.games.verboscruzados.feature.model.InfinitiveEnding
import org.indiv.dls.games.verboscruzados.feature.model.Irregularity
import org.indiv.dls.games.verboscruzados.feature.model.SubjectPronoun
import org.indiv.dls.games.verboscruzados.feature.model.Verb

/**
 * Present tense conjugator
 */
class PresentConjugator : Conjugator {
    private val arSubjectSuffixMap = mapOf(
            SubjectPronoun.YO to "o",
            SubjectPronoun.TU to "as",
            SubjectPronoun.EL_ELLA_USTED to "a",
            SubjectPronoun.ELLOS_ELLAS_USTEDES to "an",
            SubjectPronoun.NOSOTROS to "amos",
            SubjectPronoun.VOSOTROS to "áis")
    private val irSubjectSuffixMap = mapOf(
            SubjectPronoun.YO to "o",
            SubjectPronoun.TU to "es",
            SubjectPronoun.EL_ELLA_USTED to "e",
            SubjectPronoun.ELLOS_ELLAS_USTEDES to "en",
            SubjectPronoun.NOSOTROS to "imos",
            SubjectPronoun.VOSOTROS to "ís")
    private val erSubjectSuffixMap = mapOf(
            SubjectPronoun.YO to "o",
            SubjectPronoun.TU to "es",
            SubjectPronoun.EL_ELLA_USTED to "e",
            SubjectPronoun.ELLOS_ELLAS_USTEDES to "en",
            SubjectPronoun.NOSOTROS to "emos",
            SubjectPronoun.VOSOTROS to "éis")
    private val mapOfSuffixMaps = mapOf(
            InfinitiveEnding.AR to arSubjectSuffixMap,
            InfinitiveEnding.IR to irSubjectSuffixMap,
            InfinitiveEnding.ER to erSubjectSuffixMap)

    override fun conjugate(verb: Verb, subjectPronoun: SubjectPronoun): String {
        return verb.customConjugation?.invoke(subjectPronoun, ConjugationType.PRESENT) ?: run {
            val defaultSuffix = mapOfSuffixMaps[verb.infinitiveEnding]!![subjectPronoun]!!
            val root = getSpecialYoRootIfAny(verb, subjectPronoun)
                    ?: applyStemChange(getRootWithSpellingChange(verb.root, verb.infinitiveEnding.ending, defaultSuffix),
                            subjectPronoun, verb.irregularities)
            // Examples: guiar, fluir, huir, criar, dar, ver
            val suffix = when {
                subjectPronoun == SubjectPronoun.NOSOTROS && verb.infinitive.endsWith("ír") -> "í" + defaultSuffix.drop(1) // e.g sonreír -> sonreímos
                subjectPronoun == SubjectPronoun.VOSOTROS  && isWeakOneSyllableRoot(root) -> {
                    removeStartingAccent(defaultSuffix) // no accent because single syllable with week vowel
                }
                else -> defaultSuffix
            }
            return root + suffix
        }
    }

    private fun applyStemChange(root: String, subjectPronoun: SubjectPronoun, irregularities: List<Irregularity>): String {
        if (subjectPronoun == SubjectPronoun.NOSOTROS || subjectPronoun == SubjectPronoun.VOSOTROS) {
            return root
        } else {
            return when {
                irregularities.contains(Irregularity.STEM_CHANGE_E_to_I) -> replaceInLastSyllable(root, "e", "i")
                irregularities.contains(Irregularity.STEM_CHANGE_E_to_ACCENTED_I) -> replaceInLastSyllable(root, "e", "í")
                irregularities.contains(Irregularity.SPELLING_CHANGE_I_to_ACCENTED_I) -> replaceInLastSyllable(root, "i", "í")
                irregularities.contains(Irregularity.SPELLING_CHANGE_U_to_ACCENTED_U) -> replaceInLastSyllable(root, "u", "ú")
                irregularities.contains(Irregularity.STEM_CHANGE_I_to_IE) -> replaceInLastSyllable(root, "i", "ie")
                irregularities.contains(Irregularity.STEM_CHANGE_U_to_UE) -> replaceInLastSyllable(root, "u", "ue")
                irregularities.contains(Irregularity.STEM_CHANGE_O_to_UE) -> {
                    val replacement = when {
                        root.length <= 5 && root.startsWith("o") -> "hue"  // oler -> huele
                        root.takeLast(5).contains("go") -> "üe"        // avergonzar -> avergüenzo
                        else -> "ue"
                    }
                    replaceInLastSyllable(root, "o", replacement)
                }
                irregularities.contains(Irregularity.STEM_CHANGE_E_to_IE) -> {
                    val replacement = if (root.lastIndexOf("e") == 0) "ye" else "ie"  // errar -> yerro
                    replaceInLastSyllable(root, "e", replacement)
                }
                else -> root
            }
        }
    }

    private fun getSpecialYoRootIfAny(verb: Verb, subjectPronoun: SubjectPronoun): String? {
        if (subjectPronoun == SubjectPronoun.YO) {
            val e2iStemChange = verb.irregularities.contains(Irregularity.STEM_CHANGE_E_to_I)
            if (verb.irregularities.contains(Irregularity.SPELLING_CHANGE_YO_GO)) {
                verb.root.apply {
                    return when {
                        endsWith("l") || endsWith("n") || endsWith("s") -> this + "g" // e.g. salgo, tengo, asgo
                        endsWith("a") || endsWith("o") -> this + "ig"   // e.g. caer -> caigo, oír -> oigo
                        endsWith("ec") && e2iStemChange -> dropLast(2) + "ig"  // e.g. decir -> digo
                        endsWith("c") -> dropLast(1) + "g"  // e.g. hacer -> hago
                        else -> null
                    }
                }
            } else if (verb.irregularities.contains(Irregularity.SPELLING_CHANGE_YO_ZC)) {
                verb.root.apply {
                    return this.dropLast(1) + "zc"  // e.g. conocer -> conozco
                }
            }
        }
        return null
    }
}
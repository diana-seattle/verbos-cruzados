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
            val suffix = mapOfSuffixMaps[verb.infinitiveEnding]!![subjectPronoun]!!
            val root = getSpecialYoRootIfAny(verb, subjectPronoun)
                    ?: applyStemChange(getRootWithSpellingChange(verb.root, verb.infinitiveEnding, suffix),
                            subjectPronoun, verb.irregularities)
            return root + suffix
        }
    }

    private fun applyStemChange(root: String, subjectPronoun: SubjectPronoun, irregularities: List<Irregularity>): String {
        if (subjectPronoun == SubjectPronoun.NOSOTROS || subjectPronoun == SubjectPronoun.VOSOTROS) {
            return root
        } else {
            return when {
                irregularities.contains(Irregularity.STEM_CHANGE_E_to_I) -> replaceLastOccurrence(root, 'e', "i")
                irregularities.contains(Irregularity.STEM_CHANGE_E_to_IE) -> replaceLastOccurrence(root, 'e', "ie")
                irregularities.contains(Irregularity.STEM_CHANGE_O_to_UE) -> replaceLastOccurrence(root, 'o', "ue")
                irregularities.contains(Irregularity.STEM_CHANGE_U_to_UE) -> replaceLastOccurrence(root, 'u', "ue")
                else -> root
            }
        }
    }

    private fun getSpecialYoRootIfAny(verb: Verb, subjectPronoun: SubjectPronoun): String? {
        if (subjectPronoun == SubjectPronoun.YO) {
            if (verb.irregularities.contains(Irregularity.SPELLING_CHANGE_YO_GO)) {
                verb.root.apply {
                    return when {
                        endsWith("l") || endsWith("n") || endsWith("s") -> this + "g" // e.g. salgo, tengo, asgo
                        endsWith("a") || endsWith("o") -> this + "ig"   // e.g. caer -> caigo, oír -> oigo
                        endsWith("c") -> this.substring(0, length - 1) + "g"  // e.g. hacer -> hago
                        else -> null
                    }
                }
            } else if (verb.irregularities.contains(Irregularity.SPELLING_CHANGE_YO_ZC)) {
                verb.root.apply {
                    return this.substring(0, length - 1) + "zc"  // e.g. conocer -> conozco
                }
            }
        }
        return null
    }
}
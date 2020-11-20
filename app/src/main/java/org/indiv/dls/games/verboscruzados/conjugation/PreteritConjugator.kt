package org.indiv.dls.games.verboscruzados.conjugation

import org.indiv.dls.games.verboscruzados.model.ConjugationType
import org.indiv.dls.games.verboscruzados.model.InfinitiveEnding
import org.indiv.dls.games.verboscruzados.model.Irregularity
import org.indiv.dls.games.verboscruzados.model.SubjectPronoun
import org.indiv.dls.games.verboscruzados.model.Verb
import org.indiv.dls.games.verboscruzados.model.strongVowels

/**
 * Preterit tense conjugator
 */
class PreteritConjugator : Conjugator {
    private val arSubjectSuffixMap = mapOf(
            SubjectPronoun.YO to "é",
            SubjectPronoun.TU to "aste",
            SubjectPronoun.EL_ELLA_USTED to "ó",
            SubjectPronoun.ELLOS_ELLAS_USTEDES to "aron",
            SubjectPronoun.NOSOTROS to "amos",
            SubjectPronoun.VOSOTROS to "asteis")
    private val irErSubjectSuffixMap = mapOf(
            SubjectPronoun.YO to "í",
            SubjectPronoun.TU to "iste",
            SubjectPronoun.EL_ELLA_USTED to "ió",
            SubjectPronoun.ELLOS_ELLAS_USTEDES to "ieron",
            SubjectPronoun.NOSOTROS to "imos",
            SubjectPronoun.VOSOTROS to "isteis")
    private val mapOfSuffixMaps = mapOf(
            InfinitiveEnding.AR to arSubjectSuffixMap,
            InfinitiveEnding.IR to irErSubjectSuffixMap,
            InfinitiveEnding.ER to irErSubjectSuffixMap)

    override fun conjugate(verb: Verb, subjectPronoun: SubjectPronoun): String {
        return verb.customConjugation?.invoke(subjectPronoun, ConjugationType.PRETERIT) ?: run {
            val defaultSuffix = mapOfSuffixMaps[verb.infinitiveEnding]!![subjectPronoun]!!
            val suffixWithCorrectedAccent = replaceSuffixAccentIfAppropriate(defaultSuffix, verb.irregularities, subjectPronoun)
            val initialRoot = verb.altPreteritRoot
                    ?: getRootCorrectionForIrThirdPersonStemChange(verb, subjectPronoun)
                    ?: verb.root
            val suffix = getSuffixWithSpellingChanges(initialRoot, suffixWithCorrectedAccent, subjectPronoun)
            val root = getRootWithSpellingChange(initialRoot, verb.infinitiveEnding.ending, suffix)
            return root + suffix
        }
    }

    private fun replaceSuffixAccentIfAppropriate(suffix: String, irregularities: List<Irregularity>,
                                                 subjectPronoun: SubjectPronoun): String {
        if (irregularities.contains(Irregularity.NO_ACCENT_ON_PRETERIT)) {
            return when (subjectPronoun) {
                SubjectPronoun.YO -> "e"
                SubjectPronoun.EL_ELLA_USTED -> "o"
                else -> irErSubjectSuffixMap[subjectPronoun]!! // even the AR verbs use the ir suffix for this case
            }
        }
        return suffix
    }

    private fun getRootCorrectionForIrThirdPersonStemChange(verb: Verb, subjectPronoun: SubjectPronoun): String? {
        return if (verb.infinitiveEnding == InfinitiveEnding.IR && subjectPronoun.isThirdPerson) {
            getIrAlteredRoot(verb.root, verb.irregularities)
        } else {
            null
        }
    }

    private fun getSuffixWithSpellingChanges(root: String, suffix: String, subjectPronoun: SubjectPronoun): String {
        root.apply {
            if (suffix.startsWith("i")) {
                if (takeLast(1) in strongVowels) {
                    // E.g. caer -> caímos, cayeron
                    val replacement = if (subjectPronoun.isThirdPerson) "y" else "í"
                    return replacement + suffix.drop(1)
                } else if (subjectPronoun.isThirdPerson &&
                        (endsWith("ñ") || endsWith("ll") || endsWith("j") || endsWith("i"))) {
                    // E.g. tañer -> tañó, bullir -> bulló, producir -> produjeron, reír -> rieron
                    return suffix.drop(1)
                } else if (subjectPronoun.isThirdPerson && takeLast(1) in listOf("a", "e", "o", "u")
                        && takeLast(2) !in listOf("qu", "gu")) {
                    // e.g. caer -> cayeron, creer -> creyeron, oír -> oyeron, construir -> construyeron
                    return "y" + suffix.drop(1)
                }
            } else if (suffix.startsWith("í") && takeLast(1) == "u" && !anyVowels(dropLast(1))) {
                return "i" + suffix.drop(1) // no accent because single syllable, e.g. fluir -> flui, huir -> hui
            }
        }
        return suffix
    }
}
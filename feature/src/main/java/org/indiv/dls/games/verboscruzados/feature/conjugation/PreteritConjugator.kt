package org.indiv.dls.games.verboscruzados.feature.conjugation

import org.indiv.dls.games.verboscruzados.feature.model.ConjugationType
import org.indiv.dls.games.verboscruzados.feature.model.InfinitiveEnding
import org.indiv.dls.games.verboscruzados.feature.model.Irregularity
import org.indiv.dls.games.verboscruzados.feature.model.SubjectPronoun
import org.indiv.dls.games.verboscruzados.feature.model.Verb
import org.indiv.dls.games.verboscruzados.feature.model.strongVowels

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
            val suffix = getSuffixWithSpellingChanges(verb, suffixWithCorrectedAccent, subjectPronoun)
            val initialRoot = verb.altPreteritRoot
                    ?: getRootCorrectionForIrThirdPersonStemChange(verb, subjectPronoun)
                    ?: verb.root
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

    private fun getSuffixWithSpellingChanges(verb: Verb, suffix: String, subjectPronoun: SubjectPronoun): String {
        if (suffix.startsWith("i")) {
            (verb.altPreteritRoot ?: verb.root).apply {
                val lastLetter = if (isNotEmpty()) last() else ""
                if (lastLetter in strongVowels) {
                    // E.g. caer -> caímos, cayeron
                    val replacement = if (subjectPronoun.isThirdPerson) "y" else "í"
                    return replacement + suffix.substring(1)
                } else if (subjectPronoun.isThirdPerson &&
                        (endsWith("ñ") || endsWith("ll") || endsWith("j"))) {
                    // E.g. tañer -> tañó, bullir -> bulló, producir -> produjeron
                    return suffix.drop(1)
                } else if (subjectPronoun.isThirdPerson && (takeLast(1) in listOf("a", "e", "o", "u"))) {
                    // e.g. caer -> cayeron, creer -> creyeron, oír -> oyeron, construir -> construyeron
                    return "y" + suffix.drop(1)
                }
            }
        }
        return suffix
    }
}
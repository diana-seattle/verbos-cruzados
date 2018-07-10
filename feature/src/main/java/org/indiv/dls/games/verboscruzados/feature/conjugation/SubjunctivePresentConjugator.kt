package org.indiv.dls.games.verboscruzados.feature.conjugation

import org.indiv.dls.games.verboscruzados.feature.model.ConjugationType
import org.indiv.dls.games.verboscruzados.feature.model.InfinitiveEnding
import org.indiv.dls.games.verboscruzados.feature.model.Irregularity
import org.indiv.dls.games.verboscruzados.feature.model.SubjectPronoun
import org.indiv.dls.games.verboscruzados.feature.model.Verb

/**
 * Subjunctive Present tense conjugator
 */
open class SubjunctivePresentConjugator : Conjugator {
    private val presentConjugator = PresentConjugator()
    private val arSubjectSuffixMap = mapOf(
            SubjectPronoun.YO to "e",
            SubjectPronoun.TU to "es",
            SubjectPronoun.EL_ELLA_USTED to "e",
            SubjectPronoun.ELLOS_ELLAS_USTEDES to "en",
            SubjectPronoun.NOSOTROS to "emos",
            SubjectPronoun.VOSOTROS to "éis")
    private val irErSubjectSuffixMap = mapOf(
            SubjectPronoun.YO to "a",
            SubjectPronoun.TU to "as",
            SubjectPronoun.EL_ELLA_USTED to "a",
            SubjectPronoun.ELLOS_ELLAS_USTEDES to "an",
            SubjectPronoun.NOSOTROS to "amos",
            SubjectPronoun.VOSOTROS to "áis")
    private val mapOfSuffixMaps = mapOf(
            InfinitiveEnding.AR to arSubjectSuffixMap,
            InfinitiveEnding.IR to irErSubjectSuffixMap,
            InfinitiveEnding.ER to irErSubjectSuffixMap)

    override fun conjugate(verb: Verb, subjectPronoun: SubjectPronoun): String {
        return verb.customConjugation?.invoke(subjectPronoun, ConjugationType.SUBJUNCTIVE_PRESENT)
                ?: run {
                    val defaultSuffix = mapOfSuffixMaps[verb.infinitiveEnding]!![subjectPronoun]!!
                    val yoRoot = getYoRoot(verb)
                    val subjunctiveRoot = if (subjectPronoun.isNosotrosOrVosotros)
                        reverseAnyStemChanges(yoRoot, verb.infinitiveEnding == InfinitiveEnding.IR, verb.irregularities)
                    else
                        yoRoot
                    val root = getRootWithSpellingChange(subjunctiveRoot, "o", defaultSuffix)
                    // Examples: guiar, fluir, huir, criar, dar, ver, reír, freír
                    val suffix = if (subjectPronoun == SubjectPronoun.VOSOTROS && isWeakOneSyllableRoot(root)) {
                            removeStartingAccent(defaultSuffix) // no accent because single syllable with week vowel
                    } else defaultSuffix
                    return root + suffix
                }
    }

    private fun getYoRoot(verb: Verb): String {
        val presentYoForm = presentConjugator.conjugate(verb, SubjectPronoun.YO)
        return presentYoForm.dropLast(if (presentYoForm.endsWith("oy")) 2 else 1)
    }

    private fun reverseAnyStemChanges(yoRoot: String, forIrVerb: Boolean, irregularities: List<Irregularity>): String {
        return when {
            irregularities.contains(Irregularity.STEM_CHANGE_U_to_UE) -> replaceInLastSyllable(yoRoot, "ue", "u")
            irregularities.contains(Irregularity.STEM_CHANGE_O_to_UE) -> {
                val changedStem = when {
                    yoRoot.contains("güe") -> "üe"
                    yoRoot.length <= 5 && yoRoot.startsWith("hue") -> "hue"
                    else -> "ue"
                }
                replaceInLastSyllable(yoRoot, changedStem, if (forIrVerb) "u" else "o")
            }
            irregularities.contains(Irregularity.STEM_CHANGE_E_to_IE) -> {
                val changedStem = if (yoRoot.length <= 5 && yoRoot.startsWith("ye")) "ye" else "ie"
                replaceInLastSyllable(yoRoot, changedStem, if (forIrVerb) "i" else "e")
            }
            irregularities.contains(Irregularity.STEM_CHANGE_E_to_ACCENTED_I) -> {
                replaceInLastSyllable(yoRoot, "í", "i")
            }
            irregularities.contains(Irregularity.SPELLING_CHANGE_I_to_ACCENTED_I) -> {
                replaceInLastSyllable(yoRoot, "í", "i")
            }
            irregularities.contains(Irregularity.SPELLING_CHANGE_U_to_ACCENTED_U) -> {
                replaceInLastSyllable(yoRoot, "ú", "u")
            }
            else -> yoRoot
        }
    }

}
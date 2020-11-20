package org.indiv.dls.games.verboscruzados.conjugation

import org.indiv.dls.games.verboscruzados.conjugation.conjugatorMap
import org.indiv.dls.games.verboscruzados.model.ConjugationType
import org.indiv.dls.games.verboscruzados.model.Irregularity
import org.indiv.dls.games.verboscruzados.model.SubjectPronoun
import org.indiv.dls.games.verboscruzados.model.Verb
import org.indiv.dls.games.verboscruzados.model.irregularArVerbs
import org.indiv.dls.games.verboscruzados.model.irregularErVerbs
import org.indiv.dls.games.verboscruzados.model.irregularIrVerbs
import org.indiv.dls.games.verboscruzados.model.regularArVerbs
import org.indiv.dls.games.verboscruzados.model.regularErVerbs
import org.indiv.dls.games.verboscruzados.model.regularIrVerbs
import org.indiv.dls.games.verboscruzados.model.spellingChangeArVerbs
import org.indiv.dls.games.verboscruzados.model.spellingChangeErVerbs
import org.indiv.dls.games.verboscruzados.model.spellingChangeIrVerbs
import org.indiv.dls.games.verboscruzados.model.stemChangeArVerbs
import org.indiv.dls.games.verboscruzados.model.stemChangeErVerbs
import org.indiv.dls.games.verboscruzados.model.stemChangeIrVerbs
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.max

class ConjugatorTest {

    //region CLASSES UNDER TEST --------------------------------------------------------------------
    //endregion

    //region PROPERTIES ----------------------------------------------------------------------------

    private val firstColumnWidth = SubjectPronoun.ELLOS_ELLAS_USTEDES.text.length + 2

    //endregion

    //region SETUP ---------------------------------------------------------------------------------
    //endregion

    //region TESTS ---------------------------------------------------------------------------------

    @Test fun ensureNoDuplicates() {
        val allVerbs = mutableListOf<Verb>()
        allVerbs.addAll(regularArVerbs)
        allVerbs.addAll(regularIrVerbs)
        allVerbs.addAll(regularErVerbs)
        allVerbs.addAll(spellingChangeArVerbs)
        allVerbs.addAll(spellingChangeIrVerbs)
        allVerbs.addAll(spellingChangeErVerbs)
        allVerbs.addAll(stemChangeArVerbs)
        allVerbs.addAll(stemChangeIrVerbs)
        allVerbs.addAll(stemChangeErVerbs)
        allVerbs.addAll(irregularArVerbs)
        allVerbs.addAll(irregularIrVerbs)
        allVerbs.addAll(irregularErVerbs)

        val duplicates = allVerbs.groupBy { it.infinitive }
                .filter { it.value.size > 1 }
        assertEquals(0, duplicates.size)

        println("Total verbs: ${allVerbs.size}")
    }

    @Test fun testOneVerb() {
        val verb = Verb("lucir", "shine, wear", irregularities = listOf(Irregularity.SPELLING_CHANGE_YO_ZC))
        printResult(verb)
    }

    @Test fun testListOfVerbs() {
        val verbs = listOf(
                Verb("absolver", "absolve, acquit", irregularPastParticiple = "absuelto", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE)),
                Verb("conmover", "move emotionally, shake", irregularities = listOf(Irregularity.STEM_CHANGE_O_to_UE))
        )

        printAllResults(verbs)
    }

    @Test fun showVerbCollectionSizes() {
        println("regularArVerbs: " + regularArVerbs.size)
        println("regularIrVerbs: " + regularIrVerbs.size)
        println("regularErVerbs: " + regularErVerbs.size)
        val totalRegular = regularArVerbs.size + regularIrVerbs.size + regularErVerbs.size
        println("Total regular: $totalRegular\n")

        println("spellingChangeArVerbs: " + spellingChangeArVerbs.size)
        println("spellingChangeIrVerbs: " + spellingChangeIrVerbs.size)
        println("spellingChangeErVerbs: " + spellingChangeErVerbs.size)
        val totalSpellingChange = spellingChangeArVerbs.size + spellingChangeIrVerbs.size + spellingChangeErVerbs.size
        println("Total spelling change: $totalSpellingChange\n")

        println("stemChangeArVerbs: " + stemChangeArVerbs.size)
        println("stemChangeIrVerbs: " + stemChangeIrVerbs.size)
        println("stemChangeErVerbs: " + stemChangeErVerbs.size)
        val totalStemChange = stemChangeArVerbs.size + stemChangeIrVerbs.size + stemChangeErVerbs.size
        println("Total stem change: $totalStemChange\n")

        println("irregularArVerbs: " + irregularArVerbs.size)
        println("irregularIrVerbs: " + irregularIrVerbs.size)
        println("irregularErVerbs: " + irregularErVerbs.size)
        val totalIrregular = irregularArVerbs.size + irregularIrVerbs.size + irregularErVerbs.size
        println("Total irregular: $totalIrregular\n")

        println("Total: ${totalRegular + totalSpellingChange + totalStemChange + totalIrregular}")
    }

    @Test fun testRegular() {
        printResult(regularArVerbs[0])
        printResult(regularIrVerbs[0])
        printResult(regularErVerbs[0])
    }

    @Test fun testSpellingChange() {
        printAllResults(spellingChangeArVerbs)
        printAllResults(spellingChangeIrVerbs)
        printAllResults(spellingChangeErVerbs)
    }

    @Test fun testStemChange() {
        printAllResults(stemChangeArVerbs)
        printAllResults(stemChangeIrVerbs)
        printAllResults(stemChangeErVerbs)
    }

    @Test fun testIrregular() {
        printAllResults(irregularArVerbs)
        printAllResults(irregularIrVerbs)
        printAllResults(irregularErVerbs)
    }

    //endregion

    //region PRIVATE METHODS -----------------------------------------------------------------------

    private fun printAllResults(verbs: List<Verb>) {
        for (verb in verbs) {
            printResult(verb)
        }
    }

    private fun printResult(verb: Verb) {
        val mapOfBuilders = mapOf(
                createPronounLineBuilder(SubjectPronoun.YO),
                createPronounLineBuilder(SubjectPronoun.TU),
                createPronounLineBuilder(SubjectPronoun.EL_ELLA_USTED),
                createPronounLineBuilder(SubjectPronoun.NOSOTROS),
                createPronounLineBuilder(SubjectPronoun.VOSOTROS),
                createPronounLineBuilder(SubjectPronoun.ELLOS_ELLAS_USTEDES))
        val labelBuilder = StringBuilder()
                .append("\n${"".padEnd(firstColumnWidth)}")
        for ((conjugationType, conjugator) in conjugatorMap.entries) {
            val conjugationTypeHeading = when(conjugationType) {
                ConjugationType.PRESENT -> "Present"
                ConjugationType.PRETERIT -> "Preterite"
                ConjugationType.IMPERFECT -> "Imperfect"
                ConjugationType.CONDITIONAL -> "Conditional"
                ConjugationType.FUTURE -> "Future"
                ConjugationType.IMPERATIVE -> "Imperative"
                ConjugationType.SUBJUNCTIVE_PRESENT -> "Subjunctive Present"
                ConjugationType.SUBJUNCTIVE_IMPERFECT -> "Subjunctive Imperfect"
                ConjugationType.PAST_PARTICIPLE -> "Past Participle"
                ConjugationType.GERUND -> "Gerund"
            }
            val columnWidth = max(conjugationTypeHeading.length + 2, verb.infinitive.length + 7)
            labelBuilder.append(conjugationTypeHeading.toUpperCase().padEnd(columnWidth))
            for (subjectPronoun in SubjectPronoun.values()) {
                val result = conjugator.conjugate(verb, subjectPronoun)
                mapOfBuilders[subjectPronoun]!!.append(result.padEnd(columnWidth))
            }
        }

        println("\n\n${verb.infinitive.toUpperCase()}: ${verb.pastParticiple}, ${verb.gerund} ")
        println(labelBuilder.toString())
        mapOfBuilders.values.forEach {
            println(it)
        }
    }

    private fun createPronounLineBuilder(subjectPronoun: SubjectPronoun): Pair<SubjectPronoun, StringBuilder> {
        return subjectPronoun to StringBuilder().append(subjectPronoun.text.padEnd(firstColumnWidth))
    }

    //endregion

}
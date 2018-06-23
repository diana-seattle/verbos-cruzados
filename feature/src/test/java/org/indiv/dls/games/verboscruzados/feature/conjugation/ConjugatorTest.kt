package org.indiv.dls.games.verboscruzados.feature.conjugation

import org.indiv.dls.games.verboscruzados.feature.model.SubjectPronoun
import org.indiv.dls.games.verboscruzados.feature.model.Verb
import org.indiv.dls.games.verboscruzados.feature.model.irregularArVerbs
import org.indiv.dls.games.verboscruzados.feature.model.irregularErVerbs
import org.indiv.dls.games.verboscruzados.feature.model.irregularIrVerbs
import org.indiv.dls.games.verboscruzados.feature.model.regularArVerbs
import org.indiv.dls.games.verboscruzados.feature.model.regularErVerbs
import org.indiv.dls.games.verboscruzados.feature.model.regularIrVerbs
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

    @Test fun testOneVerb() {
        printResult(irregularArVerbs[10])
    }

    @Test fun testRegular() {
        printResult(regularArVerbs[0])
        printResult(regularIrVerbs[0])
        printResult(regularErVerbs[0])
    }

    @Test fun testIrregular_ar() {
        printAllResults(irregularArVerbs)
    }

    @Test fun testIrregular_ir() {
        printAllResults(irregularIrVerbs)
    }

    @Test fun testIrregular_er() {
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
            val columnWidth = max(conjugationType.text.length + 2, verb.infinitive.length + 7)
            labelBuilder.append("${conjugationType.text.toUpperCase().padEnd(columnWidth)}")
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
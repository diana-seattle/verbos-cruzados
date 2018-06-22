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

class ConjugatorTest {

    //region CLASSES UNDER TEST --------------------------------------------------------------------
    //endregion

    //region PROPERTIES ----------------------------------------------------------------------------

    val maxPronounLength = SubjectPronoun.ELLOS_ELLAS_USTEDES.text.length

    //endregion

    //region SETUP ---------------------------------------------------------------------------------
    //endregion

    //region TESTS ---------------------------------------------------------------------------------

    @Test
    fun testOneVerb() {
        printResult(irregularArVerbs[5])
    }

    @Test
    fun testRegular_ar() {
        printResult(regularArVerbs[0])
    }

    @Test
    fun testRegular_ir() {
        printResult(regularIrVerbs[0])
    }

    @Test
    fun testRegular_er() {
        printResult(regularErVerbs[0])
    }

    @Test
    fun testIrregular_ar() {
        printAllResults(irregularArVerbs)
    }

    @Test
    fun testIrregular_ir() {
        printAllResults(irregularIrVerbs)
    }

    @Test
    fun testIrregular_er() {
        printAllResults(irregularErVerbs)
    }

    // Other tenses...

    //endregion

    //region PRIVATE METHODS -----------------------------------------------------------------------

    private fun printAllResults(verbs: List<Verb>) {
        for (verb in verbs) {
            printResult(verb)
        }
    }

    private fun printResult(verb: Verb) {
        for ((conjugationType, conjugator) in conjugatorMap.entries) {
            println("\n${verb.infinitive.toUpperCase()} - ${conjugationType.text}")
            for (subjectPronoun in SubjectPronoun.values()) {
                val result = conjugator.conjugate(verb, subjectPronoun)
                println("${subjectPronoun.text.padEnd(maxPronounLength)}  $result")
            }
        }
    }

    //endregion

}
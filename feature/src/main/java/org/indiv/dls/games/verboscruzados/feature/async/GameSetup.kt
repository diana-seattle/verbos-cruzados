package org.indiv.dls.games.verboscruzados.feature.async

import io.reactivex.Single
import org.indiv.dls.games.verboscruzados.feature.GridCell
import org.indiv.dls.games.verboscruzados.feature.conjugation.conjugatorMap
import org.indiv.dls.games.verboscruzados.feature.dialog.StatsDialogFragment
import org.indiv.dls.games.verboscruzados.feature.game.GameWord
import org.indiv.dls.games.verboscruzados.feature.model.ConjugationType
import org.indiv.dls.games.verboscruzados.feature.model.InfinitiveEnding
import org.indiv.dls.games.verboscruzados.feature.model.IrregularityCategory
import org.indiv.dls.games.verboscruzados.feature.model.SubjectPronoun
import org.indiv.dls.games.verboscruzados.feature.model.Verb
import org.indiv.dls.games.verboscruzados.feature.model.irregularArVerbs
import org.indiv.dls.games.verboscruzados.feature.model.irregularErVerbs
import org.indiv.dls.games.verboscruzados.feature.model.irregularIrVerbs
import org.indiv.dls.games.verboscruzados.feature.model.regularArVerbs
import org.indiv.dls.games.verboscruzados.feature.model.regularErVerbs
import org.indiv.dls.games.verboscruzados.feature.model.regularIrVerbs
import org.indiv.dls.games.verboscruzados.feature.model.spellingChangeArVerbs
import org.indiv.dls.games.verboscruzados.feature.model.spellingChangeErVerbs
import org.indiv.dls.games.verboscruzados.feature.model.spellingChangeIrVerbs
import org.indiv.dls.games.verboscruzados.feature.model.stemChangeArVerbs
import org.indiv.dls.games.verboscruzados.feature.model.stemChangeErVerbs
import org.indiv.dls.games.verboscruzados.feature.model.stemChangeIrVerbs
import java.util.*

/**
 * Handles process of setting up a game.
 */
class GameSetup {

    companion object {
        private val TAG = GameSetup::class.java.simpleName

        private val EL_ELLA_USTED_PRONOUNS = listOf("Él", "Ella", "Usted")
        private val ELLOS_ELLAS_USTEDES_PRONOUNS = listOf("Ellos", "Ellas", "Ustedes")
    }

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    /**
     * Creates a new game.
     */
    fun newGame(cellGrid: Array<Array<GridCell?>>, gameOptions: Map<String, Boolean>): Single<List<GameWord>> {
        return Single.fromCallable {

            // retrieve random list of words
            val gridHeight = cellGrid.size
            val gridWidth = cellGrid[0].size

            val numWords = Math.round(((gridWidth * gridHeight) / 10 + 20).toFloat())  // get more than we need to maximize density of layout
            val wordCandidates = getWordCandidates(numWords, gameOptions).toMutableList()

            // determine layout
            val gameWords = layoutWords(cellGrid, wordCandidates)
            if (gameWords.isEmpty()) {
                throw Exception("Failure during words layout")
            }

            // return game
            gameWords
        }
    }

    /**
     * Adds existing game word to layout.
     */
    fun addToGrid(gameWord: GameWord, cellGrid: Array<Array<GridCell?>>) {
        var row = gameWord.row
        var col = gameWord.col
        val word = gameWord.word
        val userText = gameWord.userText
        val wordLength = word.length
        var charIndex = 0
        while (charIndex < wordLength) {
            if (cellGrid[row][col] == null) { // might exist already due to word in other direction
                cellGrid[row][col] = GridCell(word[charIndex])
            }
            cellGrid[row][col]!!.apply {
                val userChar = if (userText != null && userText.length > charIndex) userText[charIndex] else null
                if (gameWord.isAcross) {
                    gameWordAcross = gameWord
                    userCharAcross = userChar
                    col++
                } else {
                    gameWordDown = gameWord
                    userCharDown = userChar
                    row++
                }
                charIndex++
            }
        }
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    /**
     * Gets list of words that are candidates for the next puzzle.
     */
    private fun getWordCandidates(numWords: Int, gameOptions: Map<String, Boolean>): List<WordCandidate> {
        val verbMap = getQualifyingVerbs(gameOptions, numWords)
        val candidates = mutableListOf<WordCandidate>()

        getQualifyingConjugationTypes(gameOptions).forEach {
            val conjugationType = it
            verbMap.keys.forEach {
                val irregularityCategory = it
                val verbs = verbMap[irregularityCategory]!!
                when (conjugationType) {
                    ConjugationType.GERUND -> {
                        candidates.addAll(verbs.map {
                            createWordCandidate(it, it.gerund, conjugationType, irregularityCategory, null)
                        })
                    }
                    ConjugationType.PAST_PARTICIPLE -> {
                        candidates.addAll(verbs.map {
                            createWordCandidate(it, it.pastParticiple, conjugationType, irregularityCategory, null)
                        })
                    }
                    else -> {
                        val conjugator = conjugatorMap[conjugationType]!!
                        getQualifyingSubjectPronouns(gameOptions).forEach {
                            val subjectPronoun = it
                            // Avoid the Yo - Imperative form
                            if (conjugationType != ConjugationType.IMPERATIVE || subjectPronoun != SubjectPronoun.YO) {
                                candidates.addAll(verbs.map {
                                    createWordCandidate(it, conjugator.conjugate(it, subjectPronoun),
                                            conjugationType, irregularityCategory, subjectPronoun)
                                })
                            }
                        }
                    }
                }
            }
        }
        // Take less than 100% of candidates to ensure variability
        return randomSelection(candidates, minOf(numWords, (.8f * candidates.size).toInt()))
    }

    private fun createWordCandidate(verb: Verb, word: String,
                                    conjugationType: ConjugationType,
                                    irregularityCategory: IrregularityCategory,
                                    subjectPronoun: SubjectPronoun?): WordCandidate {
        // Conjugated verb can be duplicate between imperative and subjunctive or between sentar and sentir.
        val uniqueKey = "${verb.infinitive}|${conjugationType.name}|${subjectPronoun?.name ?: "na"}"
        val conjugationLabel = subjectPronoun?.let {
            "${getPronounText(it)} - ${conjugationType.text}"
        } ?: conjugationType.text
        val statsIndex = StatsDialogFragment.createStatsIndex(conjugationType, verb.infinitiveEnding, irregularityCategory)
        return WordCandidate(uniqueKey, word, conjugationLabel, verb.infinitive, verb.translation, statsIndex)
    }

    private fun getPronounText(subjectPronoun: SubjectPronoun): String {
        return when (subjectPronoun) {
            SubjectPronoun.EL_ELLA_USTED -> randomSelection(EL_ELLA_USTED_PRONOUNS, 1)[0]
            SubjectPronoun.ELLOS_ELLAS_USTEDES -> randomSelection(ELLOS_ELLAS_USTEDES_PRONOUNS, 1)[0]
            else -> subjectPronoun.text
        }
    }

    private fun getQualifyingVerbs(gameOptions: Map<String, Boolean>, minTargetQty: Int): Map<IrregularityCategory, List<Verb>> {
        val verbMap = mutableMapOf<IrregularityCategory, List<Verb>>()

        val infinitiveEndings = getQualifyingInfinitiveEndings(gameOptions)
        val irregularityCategories = getQualifyingIrregularityCategories(gameOptions)

        irregularityCategories.forEach {
            val verbs = mutableListOf<Verb>()
            when (it) {
                IrregularityCategory.REGULAR -> {
                    if (infinitiveEndings.contains(InfinitiveEnding.AR))
                        verbs.addAll(randomSelection(regularArVerbs, minTargetQty)) // cut this one down a little in size
                    if (infinitiveEndings.contains(InfinitiveEnding.IR)) verbs.addAll(regularIrVerbs)
                    if (infinitiveEndings.contains(InfinitiveEnding.ER)) verbs.addAll(regularErVerbs)
                }
                IrregularityCategory.SPELLING_CHANGE -> {
                    if (infinitiveEndings.contains(InfinitiveEnding.AR))
                        verbs.addAll(randomSelection(spellingChangeArVerbs, minTargetQty)) // cut this one down a little in size
                    if (infinitiveEndings.contains(InfinitiveEnding.IR)) verbs.addAll(spellingChangeIrVerbs)
                    if (infinitiveEndings.contains(InfinitiveEnding.ER)) verbs.addAll(spellingChangeErVerbs)
                }
                IrregularityCategory.STEM_CHANGE -> {
                    if (infinitiveEndings.contains(InfinitiveEnding.AR)) verbs.addAll(stemChangeArVerbs)
                    if (infinitiveEndings.contains(InfinitiveEnding.IR)) verbs.addAll(stemChangeIrVerbs)
                    if (infinitiveEndings.contains(InfinitiveEnding.ER)) verbs.addAll(stemChangeErVerbs)
                }
                IrregularityCategory.IRREGULAR -> {
                    if (infinitiveEndings.contains(InfinitiveEnding.AR)) verbs.addAll(irregularArVerbs)
                    if (infinitiveEndings.contains(InfinitiveEnding.IR)) verbs.addAll(irregularIrVerbs)
                    if (infinitiveEndings.contains(InfinitiveEnding.ER)) verbs.addAll(irregularErVerbs)
                }
            }
            verbMap[it] = verbs.toList()
        }
        return verbMap
    }

    private fun getQualifyingConjugationTypes(gameOptions: Map<String, Boolean>): List<ConjugationType> {
        var allValues = ConjugationType.values()
        var filteredValues = allValues.filter { gameOptions[it.name] ?: false }
        return if (filteredValues.isEmpty()) allValues.toList() else filteredValues
    }

    private fun getQualifyingInfinitiveEndings(gameOptions: Map<String, Boolean>): List<InfinitiveEnding> {
        var allValues = InfinitiveEnding.values()
        var filteredValues = allValues.filter { gameOptions[it.name] ?: false }
        return if (filteredValues.isEmpty()) allValues.toList() else filteredValues
    }

    private fun getQualifyingIrregularityCategories(gameOptions: Map<String, Boolean>): List<IrregularityCategory> {
        var allValues = IrregularityCategory.values()
        var filteredValues = allValues.filter { gameOptions[it.name] ?: false }
        return if (filteredValues.isEmpty()) allValues.toList() else filteredValues
    }

    private fun getQualifyingSubjectPronouns(gameOptions: Map<String, Boolean>): List<SubjectPronoun> {
        var allValues = SubjectPronoun.values()
        var filteredValues = allValues.filter { gameOptions[it.name] ?: false }
        return if (filteredValues.isEmpty()) allValues.toList() else filteredValues
    }

    private fun <T> randomSelection(items: List<T>, numItems: Int): List<T> {
        val sourceList = items.toMutableList()
        val destinationList = mutableListOf<T>()
        do {
            // return whichever list reaches the correct size first
            if (destinationList.size >= numItems) {
                return destinationList
            } else if (sourceList.size <= numItems) {
                return sourceList
            }

            // randomly move an item from one list to the other
            val randomIndex = getRandomIndex(sourceList.size)
            destinationList.add(sourceList.removeAt(randomIndex))

        } while(true)
    }

    private fun getRandomIndex(size: Int): Int {
        return Math.round(Math.random() * (size - 1)).toInt()
    }

    private fun layoutWords(cellGrid: Array<Array<GridCell?>>, wordCandidates: MutableList<WordCandidate>): List<GameWord> {

        // sort so that longest words are first
        wordCandidates.sortWith(Comparator { lhs, rhs -> rhs.word.length - lhs.word.length })

        // place each word into character grid
        val gameWords = ArrayList<GameWord>()
        var across = true
        var firstWord = true
        var lastGaspEffortTaken = false
        var i = 0
        while (i < wordCandidates.size) {
            val word = wordCandidates[i]
            val gameWord = placeInGrid(cellGrid, gameWords, word, across, firstWord)

            if (gameWord != null) {
                gameWords.add(gameWord)
                wordCandidates.removeAt(i) // remove used word from list of available words
                i = -1 // start over at beginning of list for next word
                across = !across
                firstWord = false
                lastGaspEffortTaken = false // since we found a word, continue on
            } else {
                // if we're about to give up, make one last effort with opposite direction
                if (!lastGaspEffortTaken && i == wordCandidates.size - 1) {
                    lastGaspEffortTaken = true
                    across = !across
                    i = -1
                }
            }
            i++
        }
        return gameWords
    }

    private fun placeInGrid(cellGrid: Array<Array<GridCell?>>,
                            gameWords: List<GameWord>,
                            wordCandidate: WordCandidate,
                            across: Boolean,
                            firstWord: Boolean): GameWord? {
        var gameWord: GameWord? = null
        val word = wordCandidate.word
        val wordLength = word.length
        var locationFound = false
        var row = 0
        var col = 0
        val lastPositionTried = wordCandidate.getLastPositionTried(across)

        if (firstWord) {
            if (isLocationValid(cellGrid, word, row, col, across, firstWord)) {
                locationFound = true
            }
            wordCandidate.setLastPositionTried(across, 0)
        } else {
            // traverse list of lain down words backwards
            var iGW = gameWords.size - 1
            while (iGW > lastPositionTried && !locationFound) {
                val gw = gameWords[iGW]
                if (gw.isAcross != across) {
                    // traverse letters of lain down word backwards
                    var iChar = gw.word.length - 1
                    while (iChar >= 0 && !locationFound) {
                        val c = gw.word[iChar]
                        // find first instance of character in word we're trying to place
                        var indexOfChar = word.indexOf(c)
                        while (indexOfChar != -1) {
                            if (gw.isAcross) {
                                col = gw.col + iChar // determine column purely from already lain down word
                                row = gw.row - indexOfChar // offset row by position in word we're laying down
                            } else {
                                row = gw.row + iChar // determine row purely from already lain down word
                                col = gw.col - indexOfChar // offset col by position in word we're laying down
                            }
                            if (isLocationValid(cellGrid, word, row, col, across, firstWord)) {
                                locationFound = true
                                break
                            }
                            if (indexOfChar < wordLength - 1) {
                                indexOfChar = word.indexOf(c, indexOfChar + 1)
                            } else {
                                break
                            }
                        }
                        iChar--
                    }
                }
                iGW--
            }
            wordCandidate.setLastPositionTried(across, gameWords.size - 1)
        }

        if (locationFound) {
            gameWord = GameWord(wordCandidate.uniqueKey, word, wordCandidate.conjugationLabel,
                    wordCandidate.infinitive, wordCandidate.translation, wordCandidate.statsIndex,
                    row, col, across)
            addToGrid(gameWord, cellGrid)
        }

        return gameWord
    }

    private fun isLocationValid(cellGrid: Array<Array<GridCell?>>,
                                word: String,
                                startingRow: Int,
                                startingCol: Int,
                                across: Boolean,
                                firstWord: Boolean): Boolean {
        var row = startingRow
        var col = startingCol

        val gridHeight = cellGrid.size
        val gridWidth = cellGrid[0].size

        // if word won't fit physically, return false
        val wordLength = word.length
        if (row < 0 || col < 0 ||
                across && col + wordLength > gridWidth ||
                !across && row + wordLength > gridHeight) {
            return false
        }

        // if first word not too long, then location is valid
        if (firstWord) {
            return true
        }

        // see if word crosses at least one other word successfully, return false if any conflicts
        var crossingFound = false
        var charIndex = 0
        while (charIndex < wordLength) {
            cellGrid[row][col]?.let {
                // if characters match
                if (it.char == word[charIndex]) {
                    // if other word is in the same direction, return false
                    if (across && it.gameWordAcross != null || !across && it.gameWordDown != null) {
                        return false
                    }
                    crossingFound = true // at least one crossing with another word has been found
                } else {
                    return false // since character conflicts, this is not a valid location for the word
                }
            } ?: run {
                // else cell is empty
                // make sure adjacent positions also open
                if (across) {
                    if (row > 0 && cellGrid[row - 1][col] != null ||                        // disallow any above
                            row < gridHeight - 1 && cellGrid[row + 1][col] != null ||          // disallow any below
                            col > 0 && charIndex == 0 && cellGrid[row][col - 1] != null ||    // disallow any to the left if on first char
                            col < gridWidth - 1 && charIndex == wordLength - 1 && cellGrid[row][col + 1] != null) {  // disallow any to the right if on last char
                        return false
                    }
                } else {
                    if (col > 0 && cellGrid[row][col - 1] != null ||                        // disallow any to the left
                            col < gridWidth - 1 && cellGrid[row][col + 1] != null ||           // disallow any to the right
                            row > 0 && charIndex == 0 && cellGrid[row - 1][col] != null ||    // disallow any above if on first char
                            row < gridHeight - 1 && charIndex == wordLength - 1 && cellGrid[row + 1][col] != null) { // disallow any below if on last char
                        return false
                    }
                }
            }
            charIndex++
            if (across) {
                col++
            } else {
                row++
            }
        }
        return crossingFound
    }

    //endregion

    //region INNER CLASSES -------------------------------------------------------------------------

    private class WordCandidate(val uniqueKey: String,
                                val word: String,
                                val conjugationLabel: String,
                                val infinitive: String,
                                val translation: String,
                                val statsIndex: Int) {
        // variables used by word placement algorithm to place word in puzzle
        private var lastAcrossPositionTried = -1
        private var lastDownPositionTried = -1


        fun getLastPositionTried(across: Boolean): Int {
            return if (across) this.lastAcrossPositionTried else this.lastDownPositionTried
        }

        fun setLastPositionTried(across: Boolean, position: Int) {
            if (across) {
                this.lastAcrossPositionTried = position
            } else {
                this.lastDownPositionTried = position
            }
        }
    }

    //endregion

}
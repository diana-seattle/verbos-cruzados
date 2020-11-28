package org.indiv.dls.games.verboscruzados.async

import android.content.res.Resources
import io.reactivex.Single
import org.indiv.dls.games.verboscruzados.GridCell
import org.indiv.dls.games.verboscruzados.conjugation.conjugatorMap
import org.indiv.dls.games.verboscruzados.dialog.StatsDialogFragment
import org.indiv.dls.games.verboscruzados.game.GameWord
import org.indiv.dls.games.verboscruzados.model.ConjugationType
import org.indiv.dls.games.verboscruzados.model.InfinitiveEnding
import org.indiv.dls.games.verboscruzados.model.IrregularityCategory
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
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Handles process of setting up a game.
 */
class GameSetup {

    companion object {
        private val USTED_PRONOUN = "Usted"
        private val USTEDES_PRONOUN = "Ustedes"
        private val EL_ELLA_USTED_PRONOUNS = listOf("Él", "Ella", USTED_PRONOUN)
        private val ELLOS_ELLAS_USTEDES_PRONOUNS = listOf("Ellos", "Ellas", USTEDES_PRONOUN)
    }

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    /**
     * Creates a new game.
     */
    fun newGame(resources: Resources, cellGrid: Array<Array<GridCell?>>, gameOptions: Map<String, Boolean>): Single<List<GameWord>> {
        return Single.fromCallable {

            val gridHeight = cellGrid.size
            val gridWidth = cellGrid[0].size

            // retrieve random list of words, getting more than we need to maximize density of layout
            val numWords = Math.round(((gridWidth * gridHeight) / 5 + 20).toFloat())
            val wordCandidates = getWordCandidates(numWords, gameOptions).toMutableList()

            // determine layout
            val gameWords = layoutWords(resources, cellGrid, wordCandidates)
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
        val userEntry = gameWord.userEntry
        val wordLength = word.length
        var charIndex = 0
        while (charIndex < wordLength) {
            if (cellGrid[row][col] == null) { // might exist already due to word in other direction
                cellGrid[row][col] = GridCell(word[charIndex])
            }
            cellGrid[row][col]!!.apply {
                val userEntryChar = userEntry[charIndex]
                if (gameWord.isAcross) {
                    gameWordAcross = gameWord
                    userCharAcross = userEntryChar
                    acrossIndex = charIndex
                    col++
                } else {
                    gameWordDown = gameWord
                    userCharDown = userEntryChar
                    downIndex = charIndex
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
        val finalRandomPercentageTaken = .8f
        val verbMap = getQualifyingVerbs(gameOptions, (numWords / finalRandomPercentageTaken).roundToInt())
        val candidates = mutableListOf<WordCandidate>()

        val conjugationTypes = getQualifyingConjugationTypes(gameOptions)
        val subjectPronouns = getQualifyingSubjectPronouns(gameOptions)
        val subjectPronounsForImperative = if (subjectPronouns.size == 1 && subjectPronouns[0] == SubjectPronoun.YO) {
            listOf(SubjectPronoun.TU) // Use Tú for commands to self
        } else {
            subjectPronouns.filter { it != SubjectPronoun.YO }
        }

        // For each irregularity category and its list of verbs
        verbMap.forEach { (irregularityCategory, verbs) ->
            val idealNumberPerIrregularityCategory = (numWords / finalRandomPercentageTaken) / verbMap.size
            val verbMultiplier = (idealNumberPerIrregularityCategory / verbs.size.toFloat()).coerceAtLeast(1f)
            val subjectPronounsPerVerb = (verbMultiplier / conjugationTypes.size).roundToInt().coerceIn(1..subjectPronouns.size)
            val conjugationTypesPerVerb = (verbMultiplier / subjectPronounsPerVerb).roundToInt().coerceIn(1..conjugationTypes.size)

            // For each verb in irregularity category
            verbs.forEach { verb ->
                val bonus = if (verb.frequency > 2) 1 else 0

                // For each conjugation type
                randomSelection(conjugationTypes, conjugationTypesPerVerb + bonus).forEach { conjugationType ->
                    when (conjugationType) {
                        ConjugationType.GERUND ->
                            candidates.add(createWordCandidate(verb, verb.gerund, conjugationType, irregularityCategory, null))
                        ConjugationType.PAST_PARTICIPLE ->
                            candidates.add(createWordCandidate(verb, verb.pastParticiple, conjugationType, irregularityCategory, null))
                        else -> {
                            val conjugator = conjugatorMap[conjugationType]!!
                            val relevantSubjectPronouns = if (conjugationType == ConjugationType.IMPERATIVE)
                                subjectPronounsForImperative else subjectPronouns
                            randomSelection(relevantSubjectPronouns, subjectPronounsPerVerb + bonus).forEach {
                                candidates.add(createWordCandidate(verb, conjugator.conjugate(verb, it), conjugationType, irregularityCategory, it))
                            }
                        }
                    }
                }
            }
        }

        // Take less than 100% of candidates to ensure variability
        return randomSelection(candidates, minOf(numWords, (finalRandomPercentageTaken * candidates.size).toInt()))
    }

    private fun createWordCandidate(verb: Verb,
                                    word: String,
                                    conjugationType: ConjugationType,
                                    irregularityCategory: IrregularityCategory,
                                    subjectPronoun: SubjectPronoun?
    ): WordCandidate {
        return WordCandidate(word, verb.infinitive, verb.infinitiveEnding, verb.translation, irregularityCategory,
                conjugationType, subjectPronoun)
    }

    private fun createGameWord(resources: Resources, wordCandidate: WordCandidate, row: Int, col: Int, isAcross: Boolean): GameWord {
        // Conjugated verb can be duplicate between imperative and subjunctive or between sentar and sentir.
        val uniqueKey = "${wordCandidate.infinitive}|${wordCandidate.conjugationType.name}|${wordCandidate.subjectPronoun?.name
                ?: "na"}"
        val conjugationTypeLabel = resources.getString(wordCandidate.conjugationType.textResId)
        val isImperative = wordCandidate.conjugationType == ConjugationType.IMPERATIVE
        val subjectPronounLabel = wordCandidate.subjectPronoun?.let { getPronounText(it, isImperative) }.orEmpty()
        val statsIndex = StatsDialogFragment.createStatsIndex(wordCandidate.conjugationType,
                wordCandidate.infinitiveEnding, wordCandidate.irregularityCategory)
        return GameWord(uniqueKey, wordCandidate.word, conjugationTypeLabel, subjectPronounLabel,
                wordCandidate.infinitive, wordCandidate.translation, statsIndex, row, col, isAcross)
    }

    private fun getPronounText(subjectPronoun: SubjectPronoun, isImperative: Boolean): String {
        return when (subjectPronoun) {
            SubjectPronoun.EL_ELLA_USTED -> if (isImperative) USTED_PRONOUN else randomSelection(EL_ELLA_USTED_PRONOUNS, 1)[0]
            SubjectPronoun.ELLOS_ELLAS_USTEDES -> if (isImperative) USTEDES_PRONOUN else randomSelection(ELLOS_ELLAS_USTEDES_PRONOUNS, 1)[0]
            else -> subjectPronoun.text
        }
    }

    /**
     * Returns a map of verbs per irregularity category, matching the configured game options, and shooting for approximately
     * the specified target quantity for the total number of verbs in the map.
     */
    private fun getQualifyingVerbs(gameOptions: Map<String, Boolean>, targetQty: Int): Map<IrregularityCategory, List<Verb>> {
        val verbMap = mutableMapOf<IrregularityCategory, List<Verb>>()

        val infinitiveEndings = getQualifyingInfinitiveEndings(gameOptions)
        val irregularityCategories = getQualifyingIrregularityCategories(gameOptions)

        // Count the combinations of infinitive endings and irregularity categories, and calculate the estimated
        // target number of verbs per combo. But don't count the irregular AR combo since it only has 3 verbs.
        val includesIrregularAr = infinitiveEndings.contains(InfinitiveEnding.AR) && irregularityCategories.contains(IrregularityCategory.IRREGULAR)
        val combinations = (infinitiveEndings.size * irregularityCategories.size - (if (includesIrregularAr) 1 else 0)).coerceAtLeast(1)
        val targetQtyPerCombo = (1.2 * targetQty / combinations).toInt() // go 20% over to make it more likely total target is met

        // For each combo, create a random list of verbs
        irregularityCategories.forEach { irregularityCategory ->
            val verbsOfIrregularityCategory = mutableListOf<Verb>()
            when (irregularityCategory) {
                IrregularityCategory.REGULAR -> {
                    if (infinitiveEndings.contains(InfinitiveEnding.AR)) verbsOfIrregularityCategory.addAll(randomVerbSelection(regularArVerbs, targetQtyPerCombo))
                    if (infinitiveEndings.contains(InfinitiveEnding.IR)) verbsOfIrregularityCategory.addAll(randomVerbSelection(regularIrVerbs, targetQtyPerCombo))
                    if (infinitiveEndings.contains(InfinitiveEnding.ER)) verbsOfIrregularityCategory.addAll(randomVerbSelection(regularErVerbs, targetQtyPerCombo))
                }
                IrregularityCategory.SPELLING_CHANGE -> {
                    if (infinitiveEndings.contains(InfinitiveEnding.AR)) verbsOfIrregularityCategory.addAll(randomVerbSelection(spellingChangeArVerbs, targetQtyPerCombo))
                    if (infinitiveEndings.contains(InfinitiveEnding.IR)) verbsOfIrregularityCategory.addAll(randomVerbSelection(spellingChangeIrVerbs, targetQtyPerCombo))
                    if (infinitiveEndings.contains(InfinitiveEnding.ER)) verbsOfIrregularityCategory.addAll(randomVerbSelection(spellingChangeErVerbs, targetQtyPerCombo))
                }
                IrregularityCategory.STEM_CHANGE -> {
                    if (infinitiveEndings.contains(InfinitiveEnding.AR)) verbsOfIrregularityCategory.addAll(randomVerbSelection(stemChangeArVerbs, targetQtyPerCombo))
                    if (infinitiveEndings.contains(InfinitiveEnding.IR)) verbsOfIrregularityCategory.addAll(randomVerbSelection(stemChangeIrVerbs, targetQtyPerCombo))
                    if (infinitiveEndings.contains(InfinitiveEnding.ER)) verbsOfIrregularityCategory.addAll(randomVerbSelection(stemChangeErVerbs, targetQtyPerCombo))
                }
                IrregularityCategory.IRREGULAR -> {
                    if (infinitiveEndings.contains(InfinitiveEnding.AR)) verbsOfIrregularityCategory.addAll(irregularArVerbs) // this category only has 3
                    if (infinitiveEndings.contains(InfinitiveEnding.IR)) verbsOfIrregularityCategory.addAll(randomVerbSelection(irregularIrVerbs, targetQtyPerCombo))
                    if (infinitiveEndings.contains(InfinitiveEnding.ER)) verbsOfIrregularityCategory.addAll(randomVerbSelection(irregularErVerbs, targetQtyPerCombo))
                }
            }
            verbMap[irregularityCategory] = verbsOfIrregularityCategory.toList()
        }
        return verbMap
    }

    /**
     * Gets conjugation types currently selected in game options.
     */
    private fun getQualifyingConjugationTypes(gameOptions: Map<String, Boolean>): List<ConjugationType> {
        val allValues = ConjugationType.values()
        val filteredValues = allValues.filter { gameOptions[it.name] ?: false }
        return if (filteredValues.isEmpty()) allValues.toList() else filteredValues
    }

    /**
     * Gets infinitive endings currently selected in game options.
     */
    private fun getQualifyingInfinitiveEndings(gameOptions: Map<String, Boolean>): List<InfinitiveEnding> {
        val allValues = InfinitiveEnding.values()
        val filteredValues = allValues.filter { gameOptions[it.name] ?: false }
        return if (filteredValues.isEmpty()) allValues.toList() else filteredValues
    }

    /**
     * Gets irregularity categories currently selected in game options.
     */
    private fun getQualifyingIrregularityCategories(gameOptions: Map<String, Boolean>): List<IrregularityCategory> {
        val allValues = IrregularityCategory.values()
        val filteredValues = allValues.filter { gameOptions[it.name] ?: false }
        return if (filteredValues.isEmpty()) allValues.toList() else filteredValues
    }

    /**
     * Gets subject pronouns currently selected in game options.
     */
    private fun getQualifyingSubjectPronouns(gameOptions: Map<String, Boolean>): List<SubjectPronoun> {
        val allValues = SubjectPronoun.values()
        val filteredValues = allValues.filter { gameOptions[it.name] ?: false }
        return if (filteredValues.isEmpty()) allValues.toList() else filteredValues
    }

    /**
     * Selects a target quentity of verbs from the specified list in a pseudo-random way, weighting verbs used more
     * frequently in conversation a little higher in the selection.
     */
    private fun randomVerbSelection(verbs: List<Verb>, targetQty: Int): List<Verb> {
        val selectedVerbs = mutableListOf<Verb>()

        // Group the verbs from the list into frequency (popularity) categories.
        val verbsByFrequency = verbs.groupBy { it.frequency }

        // Weight the more frequently used verbs a little higher in the random selection.
        verbsByFrequency.forEach { (frequency, verbsOfFrequency) ->
            val factor = when {
                frequency > 2 -> 1.5
                frequency > 1 -> 1.2
                else -> 1.0
            }
            val targetQtyOfFrequency = (factor * targetQty * verbsOfFrequency.size / verbs.size.coerceAtLeast(1)).roundToInt()
            selectedVerbs.addAll(randomSelection(verbsOfFrequency, targetQtyOfFrequency))
        }
        return selectedVerbs.toList()
    }

    /**
     * Returns a list of randomly selected verbs.
     */
    private fun <T> randomSelection(verbs: List<T>, quantity: Int): List<T> {
        // Create mutable copy of source list and move verbs from source to destination list.
        val mutableSourceList = verbs.toMutableList()
        val destinationList = mutableListOf<T>()
        do {
            // As an optimization, return whichever list reaches the target size first
            if (destinationList.size >= quantity) {
                return destinationList
            } else if (mutableSourceList.size <= quantity) {
                return mutableSourceList
            }

            // Randomly move an item from the source list to the destination list.
            val randomIndex = Random.Default.nextInt(mutableSourceList.size)
            destinationList.add(mutableSourceList.removeAt(randomIndex))

        } while (true)
    }

    /**
     * Sorts the candidates for the puzzle roughly by descending length, but without causing an imbalance in word selection
     * due to some pronouns and conjugation types having longer conjugations than others.
     */
    private fun sortCandidates(wordCandidates: MutableList<WordCandidate>): MutableList<WordCandidate> {
        // Sort entire list by descending length
        wordCandidates.sortByDescending { it.word.length }

        // Since some pronouns and conjugation types produce longer results, break into sub-lists and sort each individually.
        val candidatesByType = wordCandidates.groupBy {
            // Concatenate the pronoun with the conjugation type to form the key.
            it.subjectPronoun.toString() + "_" + it.conjugationType.toString()
        }.map { it.key to it.value.toMutableList() }

        // Merge lists such that longest of each type listed first (interweave).
        val result = mutableListOf<WordCandidate>()
        while (result.size < wordCandidates.size) {
            candidatesByType.forEach { entry ->
                if (entry.second.isNotEmpty()) {
                    result.add(entry.second.removeAt(0))
                }
            }
        }
        return result
    }

    /**
     * Chooses words from the list of candidates that fit into the puzzle, returning a list of game words that contain
     * positional info.
     */
    private fun layoutWords(resources: Resources, cellGrid: Array<Array<GridCell?>>, wordCandidates: MutableList<WordCandidate>): List<GameWord> {
        // Sort so that longest words are first (roughly).
        val sortedCandidates = sortCandidates(wordCandidates)

        // Place each word into character grid.
        val gameWords = ArrayList<GameWord>()
        var across = true
        var firstWord = true
        var lastGaspEffortTaken = false
        var i = 0
        while (i < sortedCandidates.size) {
            val word = sortedCandidates[i]
            val gameWord = placeInGrid(resources, cellGrid, gameWords, word, across, firstWord)

            if (gameWord != null) {
                gameWords.add(gameWord)
                sortedCandidates.removeAt(i) // remove used word from list of available words
                i = -1 // start over at beginning of list for next word
                across = !across
                firstWord = false
                lastGaspEffortTaken = false // since we found a word, continue on
            } else {
                // if we're about to give up, make one last effort with opposite direction
                if (!lastGaspEffortTaken && i == sortedCandidates.size - 1) {
                    lastGaspEffortTaken = true
                    across = !across
                    i = -1
                }
            }
            i++
        }
        return gameWords
    }

    private fun placeInGrid(resources: Resources,
                            cellGrid: Array<Array<GridCell?>>,
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
            gameWord = createGameWord(resources, wordCandidate, row, col, across)
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

    private class WordCandidate(val word: String,
                                val infinitive: String,
                                val infinitiveEnding: InfinitiveEnding,
                                val translation: String,
                                val irregularityCategory: IrregularityCategory,
                                val conjugationType: ConjugationType,
                                val subjectPronoun: SubjectPronoun?) {
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

        override fun toString() = "$infinitive -> $word"
    }

    //endregion

}
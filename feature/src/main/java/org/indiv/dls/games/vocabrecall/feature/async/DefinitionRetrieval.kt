package org.indiv.dls.games.vocabrecall.feature.async

import io.reactivex.Completable
import org.indiv.dls.games.vocabrecall.feature.async.api.WordnikDefinition
import org.indiv.dls.games.vocabrecall.feature.async.api.WordnikService
import org.indiv.dls.games.vocabrecall.feature.db.ContentHelper
import org.indiv.dls.games.vocabrecall.feature.db.Definition
import org.indiv.dls.games.vocabrecall.feature.db.Word
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class DefinitionRetrieval {

    companion object {
        private val TAG = DefinitionRetrieval::class.java.simpleName

        private val WORDNIK_BASE_URL = "http://api.wordnik.com:80/";
        private val MIN_WORDS_WITH_DEFS = 4000
        private val MIN_DEFS_PER_WORD = 10
    }

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    fun retrieveDefinitions(dbHelper: ContentHelper, quantity: Int): Completable {
        return Completable.fromRunnable {
            // query for words needing definitions
            val words = dbHelper.getWordsNeedingDefinitions(quantity)
            if (words.isEmpty()) {
                // When all words have definitions, release some of the older ones so they can be
                // refreshed with more current definitions next time.
                if (dbHelper.getNumWordsWithDefinitions() > MIN_WORDS_WITH_DEFS) {
                    val wordsNotNeedingDefinitions = dbHelper.getWordsNotNeedingDefinitions(quantity)  // keep the number low since we're not pausing between words
                    if (wordsNotNeedingDefinitions.isNotEmpty()) {
                        dbHelper.releaseDefinitions(wordsNotNeedingDefinitions)
                    }
                }
            } else {
                retrieveDefinitions(dbHelper, words)
            }
        }
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    // perform work
    private fun retrieveDefinitions(dbHelper: ContentHelper, words: List<Word>) {
        // Retrieve definitions for each word in the list
        val wordsWithDefs = ArrayList<Word>()
        for (word in words) {
            try {
                // retrieve definitions from web service
                retrieveDefinitions(word)
                wordsWithDefs.add(word)
            } catch (e: Exception) {
                // this probably occurred because we exceeded the API limit so abort for now but save the defs we've successfully loaded
                break
            }
        }

        // persist to db
        if (wordsWithDefs.isNotEmpty()) {
            dbHelper.saveWordDefinitions(wordsWithDefs, true)
        }
    }

    private fun retrieveDefinitions(word: Word) {
        // retrieve from web service
        val wordnikService = Retrofit.Builder().baseUrl(WORDNIK_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WordnikService::class.java)
        val wordnikDefinitions = wordnikService.getDefinitions(word.word.toLowerCase())
                .execute()
                .body()

        wordnikDefinitions?.let {
            val definitions: MutableList<Definition> = it
                    .map { def ->
                        convertToDefinition(def, word.word)
                    }
                    .filterNotNull()
                    .toMutableList()

            // sort definitions in order of dictionary preference so we can eliminate extra definitions
            definitions.sortWith(Comparator { lhs, rhs -> getDictionaryRanking(lhs) - getDictionaryRanking(rhs) })

            // remove duplicates
            val distinctDefinitions = definitions.distinctBy { it.definition }
                    .toMutableList()

            // only use as many dictionaries as necessary to reach minimum
            var defIndex = MIN_DEFS_PER_WORD
            while (defIndex < distinctDefinitions.size) {
                // if starting on a new dictionary, stop here
                if (distinctDefinitions[defIndex].source != distinctDefinitions[defIndex - 1].source) {
                    break
                }
                defIndex++
            }
            while (defIndex < distinctDefinitions.size) {
                distinctDefinitions.removeAt(defIndex)
            }

            // set order value of definitions
            distinctDefinitions.forEachIndexed { index, definition -> definition.order = index + 1 }

            word.definitions = distinctDefinitions
        }
    }

    private fun getDictionaryRanking(d: Definition): Int {
        if (d.isSourceAhd) {
            return 1
        } else if (d.isSourceWiktionary) {
            return 2
        } else if (d.isSourceCentury) {
            return 3
        } else if (d.isSourceWebster) {
            return 4
        } else if (d.isSourceWordnet) {
            return 5
        }
        return 99
    }

    private fun readResponse(`in`: InputStream): String {
        val buf = StringBuffer()
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(InputStreamReader(`in`))
            var line = reader.readLine()
            while (line != null) {
                buf.append(line)
                line = reader.readLine()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
        return buf.toString()
    }

    private fun convertToDefinition(w: WordnikDefinition, word: String): Definition? {
        val definition = Definition()
        definition.word = word

        // require source
        definition.source = w.sourceDictionary
        if (!definition.isSourceAhd && !definition.isSourceWebster && !definition.isSourceCentury && !definition.isSourceWiktionary) {
            return null
        }

        // require definition text
        var defText: String = w.text ?: ""
        if (defText.isBlank()) {
            return null
        }

        // clean up definition text
        defText = defText.replace("(?i)See the extract\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)See the quotation\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)Usage Problem ".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)See [\\w]*\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)Compare [\\w]*\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)See under [\\w]*\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)See Regional Note at [\\w]*\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)See [\\w]* at [^.]+\\.".toRegex(), "") // case-insensitive replacement of irrelevant note in AHD dictionary (e.g. "See Synonyms at brave." on word "bold")
        defText = defText.replace("(?i)Also spelled [\\w]*\\.".toRegex(), "") // case-insensitive replacement of alt spelling in century dictionary (e.g. "Also spelled stabilise." on word "stabilize")
        defText = defText.replace("(?i)Common misspelling of [\\w]*\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)See Regional Note at [\\w]*\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)Also called [\\w]*\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)Also [\\w]*\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)Obsolete spelling of [\\w]*\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)Obsolete form of [\\w]*\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)Alternate spelling of [\\w]*\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)Alternate form of [\\w]*\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)Alternative form of [\\w]*\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)Alternative spelling of [\\w]*\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)See Usage Note at [\\w]*\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)See Usage Note below.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)Same as [\\w]*\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)Related to [\\w]*\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)A dialectal variant of [\\w]*\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)An obsolete variant of [\\w]*\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)A dialectal or obsolete variant of [\\w]*\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)Variant of [\\w]*\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("(?i)A variant of [\\w]*\\.".toRegex(), "") // case-insensitive replacement
        defText = defText.replace("\\<[^\\>]*\\>".toRegex(), "") // removal of html tags
        defText = defText.replace("&amp;".toRegex(), "&") // removal of ampersand html tag
        defText = defText.trim { it <= ' ' }
        if (defText.isBlank()) {
            return null
        }
        // sometimes above doesn't match due to extra punctuation or words before the period, so eliminate some more
        if (defText.startsWith("Variant of ") || defText.startsWith("See ") || defText.startsWith("Alternative ") ||
                defText.startsWith("An obsolete ") || defText.startsWith("A dialectal ") || defText.startsWith("A Scotch form ") ||
                defText.startsWith("Also spelled ") || defText.startsWith("Obsolete spelling ") ||
                defText.startsWith("Obsolete form of ") || defText.startsWith("Same as ") ||
                defText.startsWith("Simple past tense")) {
            return null
        }

        // do the redaction
        defText = redactText(defText, word)

        // if after redaction, removing redaction and some meaningless leading words results in only about a suffix' worth, then discard the definition
        val strippedText = defText.replace("\\*".toRegex(), "").replace("Also ".toRegex(), "").replace("One that ".toRegex(), "").replace("One who ".toRegex(), "")
                .replace("Archaic".toRegex(), "").replace("Synonyms".toRegex(), "").replace("Music".toRegex(), "").replace("Something ".toRegex(), "").replace("The act of ".toRegex(), "").replace("To become ".toRegex(), "")
                .replace("To be ".toRegex(), "").replace("To form ".toRegex(), "").replace("To have ".toRegex(), "").replace("To make ".toRegex(), "")
                .replace("To use ".toRegex(), "").replace("To ".toRegex(), "")
                .replace("Having ".toRegex(), "").replace("An ".toRegex(), "").replace("A ".toRegex(), "")
                .replace("[^\\w]".toRegex(), "")
        if (strippedText.length < 4) {
            return null
        }


        // if removing non-alpha characters (e.g. hyphen) from already redacted text causes a match to the word, discard the definition
        if (word.length > 4 && defText.toLowerCase().replace("[^\\w]".toRegex(), "").indexOf(word.toLowerCase()) != -1) {
            return null
        }

        // if definition is too long, discard it
        if (defText.length > 500) {
            return null
        }

        // set the definition
        definition.definition = defText

        // get part of speech if present
        w.partOfSpeech?.let {
            definition.partOfSpeech = when {
                it.contains("noun") -> "noun"
                it.contains("verb") -> "verb"
                it.contains("adjective") -> "adj"
                it.contains("adverb") -> "adv"
                it.contains("article") -> "art"
                else -> null
            }
        }

        return definition
    }

    private fun redactText(text: String, word: String): String {
        val wordLength = word.length
        val wordLC = word.toLowerCase()
        var redactedText = text

        val wordRoots = getWordRoots(wordLC)

        var minRootLength = Math.min(wordLength, 4)
        //		if (wordLength<5 && wordLC.endsWith("e")) {   // e frequently dropped and replaced with ing, so handle this special case
        //			wordRoots.add(wordLC.substring(0, wordLength-1) + "ing");
        //		}
        if (wordLength == 6 && wordLC.endsWith("de")) {  // de more common as a non-suffix so require longer word (e.g. deride/derision)
            minRootLength = 5 // protect other words where "de" isn't actually a suffix
            wordRoots.add(wordLC.substring(0, wordLength - 2) + "sion") // handle deride/derision
        }

        // for each root, redact it out if appropriate
        for (root in wordRoots) {
            val rootLength = root.length
            if (rootLength >= minRootLength) {
                redactedText = redactedText.replace("(?i)$root".toRegex(), getBlackout(rootLength))
            } else if (rootLength >= 3) {
                redactedText = redactedText.replace(("(?i)" + root + "ing").toRegex(), getBlackout(rootLength + 3)) // try again with the "ing" suffix since most common
            }
        }

        return redactedText
    }


    private fun getWordRoots(word: String): MutableList<String> {
        val wordRoots = ArrayList<String>()
        val wordLength = word.length

        // start with whole word
        wordRoots.add(word)

        // get roots by removing suffixes
        val startingWordRoots = getStartingWordRoots(word)
        wordRoots.addAll(startingWordRoots)

        // get roots by removing prefixes
        wordRoots.addAll(getEndingWordRoots(word))

        // get additional roots by removing prefixes from roots having suffixes removed
        for (startingRoot in startingWordRoots) {
            wordRoots.addAll(getEndingWordRoots(startingRoot))
        }

        return wordRoots
    }


    // get roots after removing suffix
    private fun getStartingWordRoots(word: String): List<String> {
        val wordRoots = ArrayList<String>()
        val wordLength = word.length

        // strip suffixes
        if (word.endsWith("a") || word.endsWith("c") || word.endsWith("d") || word.endsWith("e") ||
                word.endsWith("l") || word.endsWith("m") || word.endsWith("n") || word.endsWith("r") ||
                word.endsWith("s") || word.endsWith("t") || word.endsWith("y")) {
            wordRoots.add(word.substring(0, wordLength - 1))
        }
        if (word.endsWith("ac") || word.endsWith("al") || word.endsWith("ar") ||
                word.endsWith("aw") || word.endsWith("cy") ||
                word.endsWith("de") || word.endsWith("ed") || word.endsWith("fy") ||
                word.endsWith("gy") || word.endsWith("ia") ||
                word.endsWith("er") || word.endsWith("en") || word.endsWith("es") ||
                word.endsWith("ic") || word.endsWith("le") || word.endsWith("ly") ||
                word.endsWith("my") || word.endsWith("on") || word.endsWith("pt") ||
                word.endsWith("or") || word.endsWith("ry") || word.endsWith("se") || word.endsWith("sy") ||
                word.endsWith("ty") || word.endsWith("um") || word.endsWith("us") || word.endsWith("ve")) {
            wordRoots.add(word.substring(0, wordLength - 2))
        }
        if (word.endsWith("acy") || word.endsWith("ade") || word.endsWith("age") ||
                word.endsWith("ake") || word.endsWith("ant") ||
                word.endsWith("ary") || word.endsWith("ate") || word.endsWith("cal") ||
                word.endsWith("eal") || word.endsWith("ect") || word.endsWith("efy") ||
                word.endsWith("ent") || word.endsWith("ery") || word.endsWith("ful") ||
                word.endsWith("ial") || word.endsWith("ian") || word.endsWith("ied") ||
                word.endsWith("ify") || word.endsWith("ile") || word.endsWith("ily") ||
                word.endsWith("ine") || word.endsWith("ing") || word.endsWith("ion") ||
                word.endsWith("ish") || word.endsWith("ism") || word.endsWith("ist") ||
                word.endsWith("ity") || word.endsWith("ive") || word.endsWith("ize") ||
                word.endsWith("nal") || word.endsWith("ner") ||
                word.endsWith("oir") || word.endsWith("ory") || word.endsWith("ous") ||
                word.endsWith("sis") || word.endsWith("tan") || word.endsWith("ter") || word.endsWith("tic") ||
                word.endsWith("ual") || word.endsWith("ure") || word.endsWith("yze")) {
            wordRoots.add(word.substring(0, wordLength - 3))
        }
        if (word.endsWith("able") || word.endsWith("ance") || word.endsWith("cent") ||
                word.endsWith("ence") || word.endsWith("eous") ||
                word.endsWith("iage") || word.endsWith("iate") || word.endsWith("ible") ||
                word.endsWith("ical") || word.endsWith("ient") || word.endsWith("iful") ||
                word.endsWith("ious") || word.endsWith("less") || word.endsWith("ment") ||
                word.endsWith("sion") || word.endsWith("sive") || word.endsWith("tial") ||
                word.endsWith("tion") || word.endsWith("tive") || word.endsWith("ture") ||
                word.endsWith("uate") || word.endsWith("uous")) {
            wordRoots.add(word.substring(0, wordLength - 4))
        }
        if (word.endsWith("ament") || word.endsWith("ation") || word.endsWith("ative") || word.endsWith("ature") ||
                word.endsWith("neous") || word.endsWith("imate") || word.endsWith("ition") ||
                word.endsWith("itive") || word.endsWith("iture") || word.endsWith("tious") || word.endsWith("ulous")) {
            wordRoots.add(word.substring(0, wordLength - 5))
        }

        return wordRoots
    }


    // get roots after removing prefix
    private fun getEndingWordRoots(word: String): List<String> {
        val wordRoots = ArrayList<String>()
        val wordLength = word.length

        // strip prefixes
        if (word.startsWith("a") || word.startsWith("e")) {
            wordRoots.add(word.substring(1))
        }
        if (word.startsWith("be") || word.startsWith("de") || word.startsWith("em") ||
                word.startsWith("en") || word.startsWith("ex") || word.startsWith("im") ||
                word.startsWith("in") || word.startsWith("ir") ||
                word.startsWith("re") || word.startsWith("un")) {
            wordRoots.add(word.substring(2))
        }
        if (word.startsWith("ant") || word.startsWith("dis") || word.startsWith("mis") || word.startsWith("non") ||
                word.startsWith("out") || word.startsWith("pre") || word.startsWith("sub")) {
            wordRoots.add(word.substring(3))
        }
        if (word.startsWith("anti") || word.startsWith("back") || word.startsWith("fore") || word.startsWith("over")) {
            wordRoots.add(word.substring(4))
        }
        if (word.startsWith("after") || word.startsWith("extra") || word.startsWith("inter") || word.startsWith("super") || word.startsWith("under")) {
            wordRoots.add(word.substring(5))
        }

        if (word.startsWith("counter")) {
            wordRoots.add(word.substring(7))
        }
        return wordRoots
    }


    private fun getBlackout(length: Int): String {
        val b = StringBuffer()
        for (i in 0 until length) {
            b.append("*")
        }
        return b.toString()
    }

    //endregion
}
package org.indiv.dls.games.verboscruzados.feature.async

import android.content.Context
import android.util.Log
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import org.indiv.dls.games.verboscruzados.feature.db.ContentHelper
import org.indiv.dls.games.verboscruzados.feature.db.Definition
import org.indiv.dls.games.verboscruzados.feature.db.Word
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class DbSetup {

    companion object {
        private val TAG = DbSetup::class.java.simpleName

        const val PROGRESS_RANGE = 100
        private val RANDOMIZER_RANGE = 10000
        private val FILENAME_WORDS = "words.txt"
        private val FILENAME_DEFINITIONS = "definitions.txt"
    }

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    fun ensureDbLoaded(context: Context, dbHelper: ContentHelper): Observable<Int> {
        return Observable.create { emitter ->
            // if no words loaded in db, load them
            if (!dbHelper.isDbLoaded) {
                try {
                    loadDbFromFile(emitter, context, dbHelper)
                    dbHelper.setDbLoaded()
                    emitter.onComplete()
                } catch (e: IOException) {
                    Log.e(TAG, "error loading words from file")
                    emitter.onError(e)
                }
            } else {
                emitter.onComplete()
            }
        }
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    @Throws(IOException::class)
    private fun loadDbFromFile(emitter: ObservableEmitter<Int>, context: Context, dbHelper: ContentHelper) {
        Log.d(TAG, "Loading words from file...")
        val assetManager = context.getAssets()

        // we can't get file lengths using assetManager.openFd due to file compression, so just guess-timate

        val defFactor = 12 // set of definitions for word takes longer to process than single word
        val totalWordsToProcess = 5000 + 930 * defFactor // about 5000 words, definitions for about 900 words
        var numWordsProcessed = 0


        // load all words from file, then persist them in db (faster than read one, persist one)
        var reader = BufferedReader(InputStreamReader(assetManager.open(FILENAME_WORDS)))
        val randomGenerater = Random(Date().time)
        val words = ArrayList<Word>()
        try {
            var line = reader.readLine()
            while (line != null) {
                line = line.trim()
                if (line.isNotEmpty()) {
                    words.add(Word(line.toUpperCase(), Math.round(randomGenerater.nextDouble() * RANDOMIZER_RANGE).toInt()))
                    numWordsProcessed++
                }
                // persist a chunk of words at a time to cut down on # of transactions
                if (words.size > 400) {
                    saveWordsToDb(dbHelper, words)
                    emitter.onNext(PROGRESS_RANGE * numWordsProcessed / totalWordsToProcess)
                }
                line = reader.readLine()
            }
            if (words.isNotEmpty()) {
                saveWordsToDb(dbHelper, words)
                emitter.onNext(PROGRESS_RANGE * numWordsProcessed / totalWordsToProcess)
            }
        } catch (e: Exception) {
            Log.e(TAG, "problem while loading words into database: " + e.message)
            throw e
        } finally {
            reader.close()
        }
        Log.i(TAG, "DONE loading words from file")

        // game will exist if we are doing an upgrade
        //        boolean gameExists = mDbHelper.getTotalNumGameWords() > 0;
        val gameExists = false

        // load definitions from file
        reader = BufferedReader(InputStreamReader(assetManager.open(FILENAME_DEFINITIONS)))
        var w: String?
        try {
            var word: Word? = null
            var line = reader.readLine()
            while (line != null) {
                line = line.trim()
                if (line.isNotEmpty()) {
                    // parse line into columns
                    val defColumns = line.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    w = defColumns[0]
                    val order = Integer.parseInt(defColumns[1])
                    val source = defColumns[2]
                    val partOfSpeech = defColumns[3]
                    val def = defColumns[4]

                    // if on first definition of a word
                    if (order == 1) {
                        // persist definitions for a chunk of words at a time to cut down on # of transactions
                        if (words.size > 50) {
                            saveWordDefinitionsToDb(dbHelper, words, gameExists)
                            emitter.onNext((PROGRESS_RANGE * numWordsProcessed / totalWordsToProcess))
                        }
                        word = Word(w)
                        word.definitions = ArrayList()
                        words.add(word)
                        numWordsProcessed += defFactor
                    }
                    word!!.definitions.add(Definition(w, order, def, source, partOfSpeech))
                }
                line = reader.readLine()
            }
            if (words.isNotEmpty()) {
                saveWordDefinitionsToDb(dbHelper, words, gameExists)
                emitter.onNext(PROGRESS_RANGE)
            }
        } catch (e: Exception) {
            Log.e(TAG, "problem while loading definitions into database: " + e.message)
            throw e
        } finally {
            reader.close()
        }
        Log.d(TAG, "DONE loading definition cache from file")
    }

    private fun saveWordsToDb(dbHelper: ContentHelper, words: MutableList<Word>) {
        try {
            dbHelper.addWords(words)
        } catch (e: Exception) {
            Log.e(TAG, "unable to load words: " + words[0].word + " thru " + words[words.size - 1].word + ", " + e.message)
        }

        words.clear()
    }

    private fun saveWordDefinitionsToDb(dbHelper: ContentHelper, words: MutableList<Word>, gameExists: Boolean) {
        try {
            dbHelper.saveWordDefinitions(words, gameExists)  // if game exists, then we'll need to delete existing defs as we load new ones
        } catch (e: Exception) {
            Log.e(TAG, "unable to load definitions for words: " + words[0].word + " thru " + words[words.size - 1].word + ", " + e.message)
        }

        words.clear()
    }

    //endregion

}
package org.indiv.dls.games.verboscruzados.feature.game

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import org.indiv.dls.games.verboscruzados.feature.model.ConjugationType
import org.indiv.dls.games.verboscruzados.feature.model.InfinitiveEnding
import org.indiv.dls.games.verboscruzados.feature.model.IrregularityCategory
import org.indiv.dls.games.verboscruzados.feature.model.SubjectPronoun
import java.lang.Integer.parseInt
import java.util.ArrayList

/**
 * Manages reading and writing from persisted storage.
 */
class PersistenceHelper constructor(private val mContext: Context) {

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        private val PREFS_GAME_WORDS = "game words"
        private val PREFS_GAME_WORD_OPTIONS = "game options"
        private val PREFS_GAME_STATS = "game stats"
        private val PREFS_GAME = "game"
        private val KEY_IMAGE_INDEX = "KEY_IMAGE_INDEX"
        private val gson = Gson()
    }

    //endregion

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------

    /**
     * @return list of persisted game words
     */
    val currentGameWords: List<GameWord>
        get() {
            val gameWords = ArrayList<GameWord>()
            val map: Map<String, *> = gameWordPrefs.all
            for (key in map.keys) {
                gameWords.add(gson.fromJson(map[key] as String, GameWord::class.java))
            }
            return gameWords
        }

    /**
     * @return current index of image for the current game.
     */
    val currentImageIndex: Int
        get() = gamePrefs.getInt(KEY_IMAGE_INDEX, 0)

    /**
     * @return map of persisted game options where the keys are of the following:
     * InfinitiveEnding.name, IrregularityCategory.name, SubjectPronoun.name, ConjugationType.name
     */
    val currentGameOptions: Map<String, Boolean>
        get() {
            var map: Map<String, *> = gameOptionPrefs.all
            if (map.isEmpty()) {
                map = setDefaults()
            }
            val optionMap = mutableMapOf<String, Boolean>()
            for (key in map.keys) {
                optionMap[key] = map[key] as? Boolean ?: false
            }
            return optionMap.toMap()
        }

    /**
     * @return map of stats index to total count.
     */
    val allGameStats: Map<Int, Int>
        get() {
            val statsMap = mutableMapOf<Int, Int>()
            gameStatsPrefs.all.mapKeys {
                val key = parseInt(it.key)
                statsMap[key] = it.value as Int
            }
            return statsMap
        }

    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private val gamePrefs: SharedPreferences
        get() = mContext.getSharedPreferences(PREFS_GAME, Context.MODE_PRIVATE)

    private val gameWordPrefs: SharedPreferences
        get() = mContext.getSharedPreferences(PREFS_GAME_WORDS, Context.MODE_PRIVATE)

    private val gameOptionPrefs: SharedPreferences
        get() = mContext.getSharedPreferences(PREFS_GAME_WORD_OPTIONS, Context.MODE_PRIVATE)

    private val gameStatsPrefs: SharedPreferences
        get() = mContext.getSharedPreferences(PREFS_GAME_STATS, Context.MODE_PRIVATE)

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    /**
     * Persists the entire game.
     */
    fun persistGame(gameWords: List<GameWord>) {
        var prefsEditor: SharedPreferences.Editor = gameWordPrefs
                .edit()
                .clear()
        for (gameWord in gameWords) {
            prefsEditor = prefsEditor.putString(gameWord.uniqueKey, gson.toJson(gameWord, GameWord::class.java))
        }
        prefsEditor.apply()
    }

    /**
     * Persists the image index.
     */
    fun persistImageIndex(imageIndex: Int) {
        gamePrefs.edit()
                .putInt(KEY_IMAGE_INDEX, imageIndex)
                .apply()
    }

    /**
     * Updates the persisted game with the user's entry for a word.
     *
     * @return number of rows updated
     */
    fun persistUserEntry(gameWord: GameWord) {
        gameWordPrefs
                .edit()
                .putString(gameWord.uniqueKey, gson.toJson(gameWord, GameWord::class.java))
                .apply()
    }

    /**
     * Persists the game options. Keys should be of of the following:
     * InfinitiveEnding.name, IrregularityCategory.name, SubjectPronoun.name, ConjugationType.name
     */
    fun persistGameOptions(optionsMap: Map<String, Boolean>) {
        val editor = gameOptionPrefs.edit()
        for (key in optionsMap.keys) {
            editor.putBoolean(key, optionsMap[key] ?: false)
        }
        editor.apply()
    }

    /**
     * Persists game stats for the specified set of game words.
     */
    fun persistGameStats(gameWords: List<GameWord>) {
        // Get counts per index for the game
        val gameWordsByStatsIndex = gameWords.groupBy {
            it.statsIndex
        }

        // Add game counts to totals
        val editor = gameStatsPrefs.edit()
        gameWordsByStatsIndex.keys.forEach {
            val countForGame = gameWordsByStatsIndex[it]?.size ?: 0
            val key = it.toString()
            val totalCount = gameStatsPrefs.getInt(key, 0)
            editor.putInt(key, totalCount + countForGame)
        }
        editor.apply()
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    private fun setDefaults(): Map<String, *> {
        val editor = gameOptionPrefs.edit()
        InfinitiveEnding.values().forEach { editor.putBoolean(it.name, true) }
        SubjectPronoun.values().forEach { editor.putBoolean(it.name, true) }
        editor.putBoolean(SubjectPronoun.VOSOTROS.name, false) // all but vosotros
        editor.putBoolean(ConjugationType.PRESENT.name, true)
        editor.putBoolean(IrregularityCategory.REGULAR.name, true)
        editor.apply()
        return gameOptionPrefs.all.toMap()
    }

    //endregion

}

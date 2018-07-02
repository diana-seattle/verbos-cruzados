package org.indiv.dls.games.verboscruzados.feature.game

import java.util.ArrayList

import android.content.Context
import android.content.SharedPreferences

import com.google.gson.Gson
import org.indiv.dls.games.verboscruzados.feature.model.ConjugationType
import org.indiv.dls.games.verboscruzados.feature.model.InfinitiveEnding
import org.indiv.dls.games.verboscruzados.feature.model.IrregularityCategory
import org.indiv.dls.games.verboscruzados.feature.model.SubjectPronoun

/**
 * Manages reading and writing from persisted storage.
 */
class PersistenceHelper constructor(private val mContext: Context) {

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        private val PREFS_NAME_GAME_WORDS = "game words"
        private val PREFS_GAME_WORD_OPTIONS = "game options"
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
            val map = gameWordPrefs.all
            for (key in map.keys) {
                gameWords.add(gson.fromJson(map[key] as String, GameWord::class.java))
            }
            return gameWords
        }

    /**
     * @return map of persisted game options where the keys are of the following:
     * InfinitiveEnding.name, IrregularityCategory.name, SubjectPronoun.name, ConjugationType.name
     */
    val currentGameOptions: Map<String, Boolean>
        get() {
            var map = gameOptionPrefs.all
            if (map.isEmpty()) {
                map = setDefaults()
            }
            val optionMap = mutableMapOf<String, Boolean>()
            for (key in map.keys) {
                optionMap[key] = map[key] as? Boolean ?: false
            }
            return optionMap.toMap()
        }

    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private val gameWordPrefs: SharedPreferences
        get() = mContext.getSharedPreferences(PREFS_NAME_GAME_WORDS, Context.MODE_PRIVATE)

    private val gameOptionPrefs: SharedPreferences
        get() = mContext.getSharedPreferences(PREFS_GAME_WORD_OPTIONS, Context.MODE_PRIVATE)

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
            prefsEditor = prefsEditor.putString(gameWord.word, gson.toJson(gameWord, GameWord::class.java))
        }
        prefsEditor.apply()
    }

    /**
     * Updates the persisted game with the user's entry for a word.
     *
     * @return number of rows updated
     */
    fun persistUserEntry(gameWord: GameWord) {
        gameWordPrefs
                .edit()
                .putString(gameWord.word, gson.toJson(gameWord, GameWord::class.java))
                .apply()
    }

    /**
     * Persists the game word selection option. Key should be one of the following:
     * InfinitiveEnding.name, IrregularityCategory.name, SubjectPronoun.name, ConjugationType.name
     */
    fun persistGameOption(optionKey: String, enabled: Boolean) {
        gameOptionPrefs
                .edit()
                .putBoolean(optionKey, enabled)
                .apply()
    }

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

    private fun setDefaults(): Map<String, *> {
        val editor = gameOptionPrefs.edit()
        InfinitiveEnding.values().forEach { editor.putBoolean(it.name, true) }
        IrregularityCategory.values().forEach { editor.putBoolean(it.name, true) }
        SubjectPronoun.values().forEach { editor.putBoolean(it.name, true) }
        editor.putBoolean(ConjugationType.PRESENT.name, true)
        editor.putBoolean(ConjugationType.PRETERIT.name, true)
        editor.putBoolean(ConjugationType.FUTURE.name, true)
        editor.apply()
        return gameOptionPrefs.all
    }

    //endregion

}

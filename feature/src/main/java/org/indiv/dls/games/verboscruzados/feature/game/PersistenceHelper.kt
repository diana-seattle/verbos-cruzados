package org.indiv.dls.games.verboscruzados.feature.game

import java.util.ArrayList

import android.content.Context
import android.content.SharedPreferences

import com.google.gson.Gson

/**
 * Manages reading and writing from persisted storage.
 */
class PersistenceHelper constructor(private val mContext: Context) {

    //region COMPANION OBJECT ----------------------------------------------------------------------

    companion object {
        private val PREFS_NAME_GAME_WORDS = "game words"
        private val gson = Gson()
    }

    //endregion

    //region PUBLIC PROPERTIES ---------------------------------------------------------------------
    //endregion

    //region PRIVATE PROPERTIES --------------------------------------------------------------------

    private val gameWordPrefs: SharedPreferences
        get() = mContext.getSharedPreferences(PREFS_NAME_GAME_WORDS, Context.MODE_PRIVATE)

    //endregion

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    /**
     * get list of game words from the database
     *
     * @return list
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

    //endregion

    //region PRIVATE FUNCTIONS ---------------------------------------------------------------------

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

    //endregion

}

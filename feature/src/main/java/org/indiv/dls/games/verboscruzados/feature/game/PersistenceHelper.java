package org.indiv.dls.games.verboscruzados.feature.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class PersistenceHelper {

    private static final String PREFS_NAME_GAME_WORDS = "game words";

    private static final Gson gson = new Gson();

    private final Context mContext;

    // constructor
    public PersistenceHelper(Context context) {
        mContext = context;
    }


    /**
     * Persists the entire game.
     */
    public void persistGame(List<GameWord> gameWords) {
        SharedPreferences.Editor prefsEditor = getGameWordPrefs()
                .edit()
                .clear();
        for (GameWord gameWord : gameWords) {
            prefsEditor = prefsEditor.putString(gameWord.getWord(), gson.toJson(gameWord, GameWord.class));
        }
        prefsEditor.apply();
    }

    /**
     * Updates the persisted game with the user's entry for a word.
     *
     * @return number of rows updated
     */
    public void persistUserEntry(GameWord gameWord) {
        getGameWordPrefs()
                .edit()
                .putString(gameWord.getWord(), gson.toJson(gameWord, GameWord.class))
                .apply();
    }

    /**
     * get list of game words from the database
     *
     * @return list
     */
    public List<GameWord> getCurrentGameWords() {
        List<GameWord> gameWords = new ArrayList<>();
        Map<String, ?> map = getGameWordPrefs().getAll();
        for (String key : map.keySet()) {
            gameWords.add(gson.fromJson((String)map.get(key), GameWord.class));
        }
        return gameWords;
    }

    //---------------------------------------------------//
    //------------ private methods ------------//
    //---------------------------------------------------//

    private SharedPreferences getGameWordPrefs() {
        return mContext.getSharedPreferences(PREFS_NAME_GAME_WORDS, Context.MODE_PRIVATE);
    }

}

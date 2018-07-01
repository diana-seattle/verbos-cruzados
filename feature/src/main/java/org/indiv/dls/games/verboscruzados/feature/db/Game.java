package org.indiv.dls.games.verboscruzados.feature.db;

import java.util.List;

public class Game {

    private int gameNo; // PK
    private List<GameWord> gameWords;
    

    // constructor
	public Game() {}
	
    // constructor
	public Game(int gameNo) {
		this.gameNo = gameNo;
	}


	public List<GameWord> getGameWords() {
		return gameWords;
	}
	public void setGameWords(List<GameWord> gameWords) {
		this.gameWords = gameWords;
	}

	// game no
	public int getGameNo() {
		return gameNo;
	}

}

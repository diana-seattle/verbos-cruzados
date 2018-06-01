package org.indiv.dls.games.vocabrecall.feature;

import org.indiv.dls.games.vocabrecall.feature.db.GameWord;

import android.view.View;
import android.widget.TextView;


public class GridCell {
	private GameWord gameWordAcross;
	private GameWord gameWordDown;
	private char correctChar;
	private char userCharAcross;
	private char userCharDown;
	private TextView view;
	
	// constructor
	public GridCell() {	}


	public GameWord getGameWordAcross() {
		return gameWordAcross;
	}
	public void setGameWordAcross(GameWord gameWordAcross) {
		this.gameWordAcross = gameWordAcross;
	}
	public GameWord getGameWordDown() {
		return gameWordDown;
	}
	public void setGameWordDown(GameWord gameWordDown) {
		this.gameWordDown = gameWordDown;
	}
	public char getChar() {
		return correctChar;
	}
	public void setChar(char correctChar) {
		this.correctChar = correctChar;
	}
	public char getUserCharAcross() {
		return userCharAcross;
	}
	public void setUserCharAcross(char userCharAcross) {
		this.userCharAcross = userCharAcross;
	}
	public char getUserCharDown() {
		return userCharDown;
	}
	public void setUserCharDown(char userCharDown) {
		this.userCharDown = userCharDown;
	}

	public TextView getView() {
		return view;
	}
	public void setView(TextView view) {
		this.view = view;
	}


	public boolean hasUserError() {
		char dominantUserChar = getDominantUserChar();
		return (dominantUserChar == 0  ||  dominantUserChar != correctChar);  // returning error if wrong or empty
//		return (dominantUserChar != 0  &&  dominantUserChar != correctChar);  // returning error if wrong 
	}
	
	public char getDominantUserChar() {
		if (userCharAcross != 0 && userCharDown == 0) {
			return userCharAcross;
		} else if (userCharDown != 0 && userCharAcross == 0) {
			return userCharDown;
		} else if (userCharAcross == 0 && userCharDown == 0) {
			return 0;
		} else {
			if (gameWordAcross.isConfident() && !gameWordDown.isConfident()) {
				return userCharAcross;
			} else {
				return userCharDown;
			}
		}
	}

	public boolean isDominantCharConfident() {
		return ((userCharAcross != 0 && gameWordAcross.isConfident()) || 
				(userCharDown != 0 && gameWordDown.isConfident()));
	}
}

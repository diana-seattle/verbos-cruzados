package org.indiv.dls.games.vocabrecall.feature.async

import android.util.Log
import io.reactivex.Single
import org.indiv.dls.games.vocabrecall.feature.GridCell
import org.indiv.dls.games.vocabrecall.feature.db.ContentHelper
import org.indiv.dls.games.vocabrecall.feature.db.Game
import org.indiv.dls.games.vocabrecall.feature.db.GameWord
import org.indiv.dls.games.vocabrecall.feature.db.Word
import java.util.*

/**
 * Handles process of setting up a game.
 */
class GameSetup {

    companion object {
        private val TAG = GameSetup::class.java.simpleName
    }

    //region PUBLIC FUNCTIONS ----------------------------------------------------------------------

    /**
     * Creates a new game.
     */
    fun newGame(dbHelper: ContentHelper, cellGrid: Array<Array<GridCell?>>, gameNo: Int): Single<Game> {
        return Single.fromCallable {

            // delete previous game
            if (gameNo > 1) {
                try {
                    dbHelper.deleteGame(gameNo - 1)
                } catch (e: Exception) {
                    Log.e(TAG, "error deleting previous game: " + e.message)
                }

            }

            // retrieve random list of words
            val gridHeight = cellGrid.size
            val gridWidth = cellGrid[0].size

            val numWords = Math.round((gridWidth * gridHeight / 5 + 20).toFloat())  // get more than we need to maximize density of layout
            val words = dbHelper.getNextWordsList(numWords)
            if (words == null || words.size == 0) {
                throw Exception("Unable to get next words")
            }

            // determine layout
            val game = Game(gameNo)
            game.gameWords = layoutWords(cellGrid, words, gameNo)
            if (game.getGameWords() == null || game.gameWords.size == 0) {
                throw Exception("Failure during words layout")
            }

            // load word definitions from db
            for (gameWord in game.gameWords) {
                dbHelper.loadWordDefinitions(gameWord.getWordInfo())
            }

            // persist game words with layout
            try {
                dbHelper.saveGame(game)
            } catch (e: Exception) {
                throw Exception("Unable to save game to database")
            }

            // return game
            game
        }
    }


    /**
     * Adds existing game word to layout.
     */
    fun addToGrid(gameWord: GameWord, cellGrid: Array<Array<GridCell?>>) {
        var row = gameWord.row
        var col = gameWord.col
        val w = gameWord.word
        val userText = gameWord.userText
        val wordLength = w.length
        var charIndex = 0
        while (charIndex < wordLength) {
            if (cellGrid[row][col] == null) { // might exist already due to word in other direction
                cellGrid[row][col] = GridCell()
            }
            cellGrid[row][col]?.apply {
                char = w[charIndex]
                val userChar = if (userText != null && userText.length > charIndex) userText[charIndex] else 0.toChar()
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

    private fun layoutWords(cellGrid: Array<Array<GridCell?>>, words: MutableList<Word>, gameNo: Int): List<GameWord> {

        // sort so that longest words are first
        Collections.sort(words) { lhs, rhs -> rhs.word.length - lhs.word.length }

        // place each word into character grid
        val gameWords = ArrayList<GameWord>()
        var across = true
        var firstWord = true
        var lastGaspEffortTaken = false
        //		int giveUpCount = 0;
        var i = 0
        while (i < words.size) {
            val word = words[i]
            val gameWord = placeInGrid(cellGrid, gameWords, word, gameNo, across, firstWord)

            if (gameWord != null) {
                gameWords.add(gameWord)
                words.removeAt(i) // remove used word from list of available words
                i = -1 // start over at beginning of list for next word
                across = !across
                firstWord = false
                lastGaspEffortTaken = false // since we found a word, continue on
            } else {
                // a small optimization to speed things up - if first (and longest) word in list has been tried multiple times, give up on it
                //				if (i == 0) {
                //					giveUpCount++;
                //					if (giveUpCount > 2  &&  word.getWord().length() > 6) {  // keep trying with the smaller words for the sake of density
                //						words.remove(0);
                //						giveUpCount = 0;
                //						i = -1;
                //					}
                //				} else

                // if we're about to give up, make one last effort with opposite direction
                if (!lastGaspEffortTaken && i == words.size - 1) {
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
                            word: Word,
                            gameNo: Int,
                            across: Boolean,
                            firstWord: Boolean): GameWord? {
        var gameWord: GameWord? = null
        val w = word.word
        val wordLength = w.length
        var locationFound = false
        var row = 0
        var col = 0
        val lastPositionTried = word.getLastPositionTried(across)

        if (firstWord) {
            if (isLocationValid(cellGrid, w, row, col, across, firstWord)) {
                locationFound = true
            }
            word.setLastPositionTried(across, 0)
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
                        var indexOfChar = w.indexOf(c)
                        while (indexOfChar != -1) {
                            if (gw.isAcross) {
                                col = gw.col + iChar // determine column purely from already lain down word
                                row = gw.row - indexOfChar // offset row by position in word we're laying down
                            } else {
                                row = gw.row + iChar // determine row purely from already lain down word
                                col = gw.col - indexOfChar // offset col by position in word we're laying down
                            }
                            if (isLocationValid(cellGrid, w, row, col, across, firstWord)) {
                                locationFound = true
                                break
                            }
                            if (indexOfChar < wordLength - 1) {
                                indexOfChar = w.indexOf(c, indexOfChar + 1)
                            } else {
                                break
                            }
                        }
                        iChar--
                    }
                }
                iGW--
            }
            word.setLastPositionTried(across, gameWords.size - 1)
        }

        if (locationFound) {
            gameWord = GameWord(word, gameNo, row, col, across)
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

//	private GameWord placeInGridAlt(Word word, int gameNo, boolean across, int distanceFromEdge, boolean firstWord) {
//		int row = 0, col = 0;
//		String w = word.getWord();
//		int wordLength = w.length();
//		boolean locationFound = false;
//		if (across) {
//			col = distanceFromEdge;
//			while (row < mGridHeight) {
//				// check left edge
//				if (isLocationValid(w, row, col, across, firstWord)) {
//					locationFound = true;
//					break;
//				// else check right edge
//				} else if (isLocationValid(w, row, mGridWidth - wordLength - distanceFromEdge, across, firstWord)) {
//					col = mGridWidth - wordLength - distanceFromEdge;
//					locationFound = true;
//					break;
//				}
//				row++;
//			}
//		} else {
//			row = distanceFromEdge;
//			while (col < mGridWidth) {
//				// check top edge
//				if (isLocationValid(w, row, col, across, firstWord)) {
//					locationFound = true;
//					break;
//				// check bottom edge
//				} else if (isLocationValid(w, mGridHeight - wordLength - distanceFromEdge, col, across, firstWord)) {
//					row = mGridHeight - wordLength - distanceFromEdge;
//					locationFound = true;
//					break;
//				}
//				col++;
//			}
//		}
//		if (locationFound) {
//			GameWord gameWord = new GameWord(word, gameNo, row, col, across);
//			addToGrid(gameWord, cellGrid);
//			return gameWord;
//		} else {
//			if ((across && 2*distanceFromEdge + wordLength + 1 >= mGridWidth) ||
//				(!across && 2*distanceFromEdge + wordLength + 1 >= mGridHeight)) {
//				return null;
//			} else {
//				// make recursive call
//				return placeInGridAlt(word, gameNo, across, distanceFromEdge+1, firstWord);
//			}
//		}
//	}

    //endregion

}
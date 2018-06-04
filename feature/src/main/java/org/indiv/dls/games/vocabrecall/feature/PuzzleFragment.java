package org.indiv.dls.games.vocabrecall.feature;

import java.util.ArrayList;
import java.util.List;

import org.indiv.dls.games.vocabrecall.feature.db.GameWord;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.widget.Space;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class PuzzleFragment extends Fragment {

    // interface for activity to implement to receive touch event
    public interface PuzzleListener {
        void onPuzzleClick(GameWord gameWord);
    }

    private static final String TAG = PuzzleFragment.class.getSimpleName();
    private static final int TENTATIVE_COLOR = 0xFFAAAAAA; // Color.LTGRAY is 0xFFCCCCCC, Color.GRAY is 0xFF888888
    private static final int CONFIDENT_COLOR = Color.BLACK; // Color.LTGRAY is 0xFFCCCCCC, Color.GRAY is 0xFF888888
    private static final int CELL_BKGD_LEVEL_NORMAL = 1;
    private static final int CELL_BKGD_LEVEL_ERRORED = 2;
    private static final int CELL_BKGD_LEVEL_SELECTED = 3;
    private static final int CELL_BKGD_LEVEL_ERRORED_SELECTED = 4;

    private TableLayout mTableLayout;
    private GridCell[][] mCellGrid;
    private int mGridWidth;
    private int mGridHeight;
    private int mPixelsPerCell;
    private GameWord mCurrentGameWord;
    private Vibrator mVibrator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // inflate the view
        View view = inflater.inflate(R.layout.fragment_puzzle, container);

        // Get instance of Vibrator from current Context
        mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);


//		Resources r = getResources();
//		DisplayMetrics displayMetrics = r.getDisplayMetrics();
//		
////		Configuration config = r.getConfiguration();
//		
////		int viewWidthPixels = displayMetrics.widthPixels;
////		int viewHeightPixels = displayMetrics.heightPixels - actionBarHeightInPixels;
//		int viewWidthPixels = view.getWidth();
//		int viewHeightPixels = view.getHeight();
//		
//
//		
//		mTableLayout = (TableLayout)view.findViewById(R.id.cellTable);
//		
//		
//		// calculate number of pixels equivalent to 24dp (24dp allows 13 cells on smallest screen supported by Android (320dp width, 426dp height))
//	    mPixelsPerCell = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, displayMetrics));
//		
//		
//		// set up grid
//		mGridHeight = (int)(viewHeightPixels / mPixelsPerCell) - 1;  // subtract 1 because subtracting action bar height doesn't seem to be enough
//		mGridWidth = viewWidthPixels / mPixelsPerCell - 1;  // subtract 1 to give margin for consistency with height
//		mCellGrid = new GridCell[mGridHeight][mGridWidth];
//
//		// create table rows 
//		for (int row = 0;  row < mGridHeight;  row++) {
//			TableRow tableRow = new TableRow(getActivity());
//			tableRow.setGravity(Gravity.CENTER);
//			mTableLayout.addView(tableRow);
//		}


        // Note that database not set up yet at this point (happening in other thread).
        // When it completes, it will call onFinishDbSetup().


        return view;
    }


    //---------------------------------------------------//
    //------------ end of overridden methods ------------//
    //---------------------------------------------------//

    public void initialize(int viewWidthPixels, int viewHeightPixels) {
        Resources r = getResources();
//		DisplayMetrics displayMetrics = r.getDisplayMetrics();

//		View view = getView();
//		Configuration config = r.getConfiguration();
//		int viewWidthPixels = displayMetrics.widthPixels;
//		int viewHeightPixels = displayMetrics.heightPixels - actionBarHeightInPixels;
//		int viewWidthPixels = view.getWidth();
//		int viewHeightPixels = view.getHeight();


        // calculate number of pixels equivalent to 24dp (24dp allows 13 cells on smallest screen supported by Android (320dp width, 426dp height))
//	    mPixelsPerCell = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, r.getDimension(R.dimen.cell_width), displayMetrics));
        mPixelsPerCell = Math.round(r.getDimension(R.dimen.cell_width));

        // set up grid
        mGridHeight = (int) (viewHeightPixels / mPixelsPerCell) - 2;  // subtract 2 because subtracting action bar height doesn't seem to be enough
        mGridWidth = viewWidthPixels / mPixelsPerCell - 1;  // subtract 1 to give margin for consistency with height
        mCellGrid = new GridCell[mGridHeight][mGridWidth];

        // create table rows
        mTableLayout = (TableLayout) getView().findViewById(R.id.cellTable);
        for (int row = 0; row < mGridHeight; row++) {
            TableRow tableRow = new TableRow(getActivity());
            tableRow.setGravity(Gravity.CENTER);
            mTableLayout.addView(tableRow);
        }
    }

    public boolean doWordsFitInGrid(List<GameWord> gameWords) {
        for (GameWord gw : gameWords) {
            if (gw.getRow() >= mGridHeight || gw.getCol() >= mGridWidth) {
                return false;
            }
            if (gw.isAcross() && gw.getCol() + gw.getWord().length() > mGridWidth) {
                return false;
            }
            if (!gw.isAcross() && gw.getRow() + gw.getWord().length() > mGridHeight) {
                return false;
            }
        }
        return true;
    }

    public GridCell[][] getCellGrid() {
        return mCellGrid;
    }


    /**
     * create new game (called first time app run, or when user starts new game)
     * implements interface for receiving callback from ConfirmStartNewGameDialogFragment
     */
    public void clearExistingGame() {
        // clear out any existing data
        mCurrentGameWord = null;
        for (int row = 0; row < mGridHeight; row++) {
            TableRow tableRow = (TableRow) mTableLayout.getChildAt(row);
            tableRow.removeAllViews();
            for (int col = 0; col < mGridWidth; col++) {
                GridCell gridCell = mCellGrid[row][col];
                if (gridCell != null) {
                    mCellGrid[row][col] = null;  // remove cell data
                }
            }
        }
    }


    /*
     */
    public GameWord getCurrentGameWord() {
        return mCurrentGameWord;
    }


    /*
     * implements interface for receiving callback from AnswerFragment
     */
    public List<TextView> getPuzzleRepresentation() {
        // show puzzle representation in layout
        Resources r = getResources();
        int wordLength = mCurrentGameWord.getWord().length();
        int row = mCurrentGameWord.getRow();
        int col = mCurrentGameWord.getCol();
//        int size = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, r.getDimension(R.dimen.cell_representation_width), r.getDisplayMetrics()));
        int size = Math.round(r.getDimension(R.dimen.cell_representation_width));
        float fontHeight = size * .75f;
        List<TextView> views = new ArrayList<TextView>();
        for (int charIndex = 0; charIndex < wordLength; charIndex++) {
            TextView textView = createCellTextView(r, fontHeight, size);
            views.add(textView);
            GridCell gridCell = mCellGrid[row][col];
            if (mCurrentGameWord.isAcross()) {
                if (gridCell.getGameWordDown() != null) {
                    fillTextView(textView, gridCell.getUserCharDown(), gridCell.getGameWordDown().isConfident());
                }
                col++;
            } else {
                if (gridCell.getGameWordAcross() != null) {
                    fillTextView(textView, gridCell.getUserCharAcross(), gridCell.getGameWordAcross().isConfident());
                }
                row++;
            }
        }
        return views;
    }


    public void createGrid() {
        Resources r = getResources();
        float fontHeight = mPixelsPerCell * .75f;

        // create click listener for puzzle clicks
        OnClickListener onPuzzleClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                GridCell gridCell = getCellForView(v);
                if (gridCell != null) {
                    if (mVibrator != null) {
                        mVibrator.vibrate(25); // vibrate for 300 milliseconds
                    }
                    setCurrentGameWord((gridCell.getGameWordDown() != null) ? gridCell.getGameWordDown() : gridCell.getGameWordAcross());
                    ((PuzzleListener) getActivity()).onPuzzleClick(mCurrentGameWord);
                }
            }
        };

        FragmentActivity activity = getActivity();

        // add views into table rows and columns
        GameWord firstGameWord = null;
        for (int row = 0; row < mGridHeight; row++) {
            TableRow tableRow = (TableRow) mTableLayout.getChildAt(row);
            tableRow.removeAllViews();
            for (int col = 0; col < mGridWidth; col++) {
                GridCell gridCell = mCellGrid[row][col];
                if (gridCell != null) {

                    // create text view for this row and column
                    TextView textView = createCellTextView(r, fontHeight, mPixelsPerCell);
                    textView.setOnClickListener(onPuzzleClickListener);
                    tableRow.addView(textView, col);
                    gridCell.setView(textView);

                    fillTextView(gridCell);

                    // set current game word to the first across word found for sake of dual pane mode
                    if (firstGameWord == null) {
                        firstGameWord = gridCell.getGameWordAcross();
                        if (firstGameWord == null) {
                            firstGameWord = gridCell.getGameWordDown();
                        }
                    }
                } else {
                    tableRow.addView(new Space(activity), col);
                }
            }
        }

        // do this after grid completely created
        if (firstGameWord != null) {
            setCurrentGameWord(firstGameWord);
        }
    }

    private void setCurrentGameWord(GameWord gameWord) {
        if (mCurrentGameWord != null) {
            showAsSelected(mCurrentGameWord, false);
        }
        mCurrentGameWord = gameWord;
        showAsSelected(mCurrentGameWord, true);
    }

    public boolean selectNextErroredGameWord() {
        for (int row = 0; row < mGridHeight; row++) {
            for (int col = 0; col < mGridWidth; col++) {
                GridCell gridCell = mCellGrid[row][col];
                if (gridCell != null && gridCell.hasUserError()) {
                    setCurrentGameWord(gridCell.getGameWordAcross() != null ? gridCell.getGameWordAcross() : gridCell.getGameWordDown());
                    return true;
                }
            }
        }
        return false;
    }

    public boolean showErrors(boolean showErrors) {
        int numErrors = 0;

        // update background of cells based on whether text is correct or not
        for (int row = 0; row < mGridHeight; row++) {
            for (int col = 0; col < mGridWidth; col++) {
                GridCell gridCell = mCellGrid[row][col];
                // if cell is part of currently selected game word, adjust the level for the background
                if (gridCell != null) {
                    boolean isSelected = (mCurrentGameWord.equals(gridCell.getGameWordAcross()) || mCurrentGameWord.equals(gridCell.getGameWordDown()));
                    TextView textView = gridCell.getView();
                    if (showErrors && gridCell.hasUserError()) {
                        numErrors++;
                        textView.getBackground().setLevel(isSelected ? CELL_BKGD_LEVEL_ERRORED_SELECTED : CELL_BKGD_LEVEL_ERRORED); // set error background
                        textView.setTextColor(Color.RED);
                    } else {
                        textView.getBackground().setLevel(isSelected ? CELL_BKGD_LEVEL_SELECTED : CELL_BKGD_LEVEL_NORMAL); // set normal text cell background
                        textView.setTextColor(gridCell.isDominantCharConfident() ? CONFIDENT_COLOR : TENTATIVE_COLOR);
                    }
                }
            }
        }
        return (numErrors > 0);
    }

    //
    // returns true if puzzle is completely filled in
    // (if correctly=true, then only returns true if all entries are filled in and correct)
    //
    public boolean isPuzzleComplete(boolean correctly) {
        for (int row = 0; row < mGridHeight; row++) {
            for (int col = 0; col < mGridWidth; col++) {
                GridCell gridCell = mCellGrid[row][col];
                if (gridCell != null) {
                    // if cell is empty, then not complete
                    if (gridCell.getDominantUserChar() == 0 || (correctly && gridCell.hasUserError())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    /*
     *
     */
    public void updateUserTextInPuzzle(GameWord gameWord) {
        // show answer in puzzle
        String userText = gameWord.getUserText();
        int userTextLength = userText.length();
        int wordLength = mCurrentGameWord.getWord().length();
        int row = gameWord.getRow();
        int col = gameWord.getCol();
        for (int charIndex = 0; charIndex < wordLength; charIndex++) {
            GridCell gridCell = mCellGrid[row][col];
            char userChar = (charIndex < userTextLength) ? userText.charAt(charIndex) : 0;
            if (gameWord.isAcross()) {
                gridCell.setUserCharAcross(userChar);
                fillTextView(gridCell);
                col++;
            } else {
                gridCell.setUserCharDown(userChar);
                fillTextView(gridCell);
                row++;
            }
        }
    }

    //-----------------------------------------//
    //------------ private methods ------------//
    //-----------------------------------------//

    private void showAsSelected(GameWord gameWord, boolean asSelected) {
        if (gameWord != null) {

//			int animationId = gameWord.isAcross()? R.anim.word_selection_horiz : R.anim.word_selection_vert;
//			Animation selectionAnimation = asSelected? AnimationUtils.loadAnimation(getActivity(), animationId) : null;

            int row = gameWord.getRow();
            int col = gameWord.getCol();
            for (int i = 0; i < gameWord.getWord().length(); i++) {
                GridCell gridCell = mCellGrid[row][col];
                TextView textView = gridCell.getView();
                int newLevel = CELL_BKGD_LEVEL_NORMAL;
                switch (textView.getBackground().getLevel()) {
                    case CELL_BKGD_LEVEL_NORMAL:
                    case CELL_BKGD_LEVEL_SELECTED:
                        newLevel = asSelected ? CELL_BKGD_LEVEL_SELECTED : CELL_BKGD_LEVEL_NORMAL;
                        break;
                    case CELL_BKGD_LEVEL_ERRORED:
                    case CELL_BKGD_LEVEL_ERRORED_SELECTED:
                        newLevel = asSelected ? CELL_BKGD_LEVEL_ERRORED_SELECTED : CELL_BKGD_LEVEL_ERRORED;
                        break;
                }
                textView.getBackground().setLevel(newLevel);

//				if (selectionAnimation != null) {
//					textView.startAnimation(selectionAnimation);
//				}

                if (gameWord.isAcross()) {
                    col++;
                } else {
                    row++;
                }
            }
        }
    }

    private void fillTextView(GridCell gridCell) {
        fillTextView(gridCell.getView(), gridCell.getDominantUserChar(), gridCell.isDominantCharConfident());
    }

    private void fillTextView(TextView textView, char dominantUserChar, boolean confident) {
        if (dominantUserChar != 0) {
            textView.setTextColor(confident ? CONFIDENT_COLOR : TENTATIVE_COLOR);
            textView.setText(String.valueOf(dominantUserChar));
        } else {
            textView.setText(null);
        }
    }

    private TextView createCellTextView(Resources r, float fontHeight, int size) {
        TextView textView = new TextView(getActivity());
        textView.setGravity(Gravity.CENTER);
        // need to create Drawable object for each TextView
        textView.setBackgroundDrawable(r.getDrawable(R.drawable.cell_drawable));  // using deprecated method since setBackground() not supported until API level 16
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontHeight);
        textView.setWidth(size);
        textView.setHeight(size);
        textView.getBackground().setLevel(1); // set normal text cell background (i.e. no error indication)
//		textView.setSoundEffectsEnabled(false); // true by default, consider disabling since we're providing our own vibration (except not all devices have vibration)
        return textView;
    }


    private GridCell getCellForView(View v) {
        for (int row = 0; row < mGridHeight; row++) {
            for (int col = 0; col < mGridWidth; col++) {
                GridCell gridCell = mCellGrid[row][col];
                if (gridCell != null && v == gridCell.getView()) {
                    return gridCell;
                }
            }
        }
        return null;
    }

}

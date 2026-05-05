package sudoku.model;

import java.util.Set;

public interface SudokuModel {
    int SIZE = 9;

    int getCellValue(int row, int col);

    boolean isCellFixed(int row, int col);

    boolean isCellEditable(int row, int col);

    boolean setCellValue(int row, int col, int value);

    boolean clearCell(int row, int col);

    boolean undoLastAction();

    boolean canUndo();

    boolean revealHint();

    void resetPuzzle();

    void newGame();

    boolean isComplete();

    Set<CellPosition> getInvalidCells();

    int[][] getBoardSnapshot();

    boolean isValidationFeedbackEnabled();

    void setValidationFeedbackEnabled(boolean enabled);

    boolean isHintEnabled();

    void setHintEnabled(boolean enabled);

    boolean isRandomPuzzleSelectionEnabled();

    void setRandomPuzzleSelectionEnabled(boolean enabled);
}

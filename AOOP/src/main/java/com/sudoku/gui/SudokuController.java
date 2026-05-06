package sudoku.gui;

import sudoku.model.CellPosition;
import sudoku.model.SudokuModel;

public class SudokuController {
    private final SudokuModel model;
    private final SudokuGuiViewPort view;

    private int selectedRow = -1;
    private int selectedCol = -1;

    public SudokuController(SudokuModel model, SudokuGuiViewPort view) {
        this.model = model;
        this.view = view;
        refreshControls();
    }

    public void selectCell(int row, int col) {
        if (!isInBounds(row, col)) {
            return;
        }
        selectedRow = row;
        selectedCol = col;
        refreshControls();
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public int getSelectedCol() {
        return selectedCol;
    }

    public void moveSelection(int deltaRow, int deltaCol) {
        int nextRow = selectedRow < 0 ? 0 : Math.max(0, Math.min(SudokuModel.SIZE - 1, selectedRow + deltaRow));
        int nextCol = selectedCol < 0 ? 0 : Math.max(0, Math.min(SudokuModel.SIZE - 1, selectedCol + deltaCol));
        selectCell(nextRow, nextCol);
    }

    public void inputDigit(int value) {
        if (!isSelected() || value < 1 || value > SudokuModel.SIZE) {
            return;
        }
        if (!model.isCellEditable(selectedRow, selectedCol)) {
            view.showStatusMessage("This is a pre-filled cell and cannot be modified.");
            return;
        }

        if (model.setCellValue(selectedRow, selectedCol, value)) {
            reportInvalidIfNeeded(selectedRow, selectedCol);
        }

        refreshControls();
    }

    public void eraseSelectedCell() {
        if (!isSelected()) {
            return;
        }
        if (!model.isCellEditable(selectedRow, selectedCol)) {
            view.showStatusMessage("This is a pre-filled cell and cannot be erased.");
            return;
        }

        if (!model.clearCell(selectedRow, selectedCol)) {
            view.showStatusMessage("Selected cell is already empty.");
        }

        refreshControls();
    }

    public void undo() {
        if (!model.undoLastAction()) {
            view.showStatusMessage("No move available for undo.");
        }
        refreshControls();
    }

    public void hint() {
        if (!model.isHintEnabled()) {
            view.showStatusMessage("Hint is currently disabled.");
            return;
        }
        if (!model.revealHint()) {
            view.showStatusMessage("No empty editable cell available for hint.");
        }
        refreshControls();
    }

    public void reset() {
        model.resetPuzzle();
        refreshControls();
        view.showStatusMessage("Puzzle reset to the initial state.");
    }

    public void newGame() {
        model.newGame();
        selectedRow = -1;
        selectedCol = -1;
        refreshControls();
        view.showStatusMessage("Loaded a new puzzle.");
    }

    public void setValidationFeedbackEnabled(boolean enabled) {
        model.setValidationFeedbackEnabled(enabled);
        refreshControls();
    }

    public void setHintEnabled(boolean enabled) {
        model.setHintEnabled(enabled);
        refreshControls();
    }

    public void setRandomPuzzleSelectionEnabled(boolean enabled) {
        model.setRandomPuzzleSelectionEnabled(enabled);
        refreshControls();
    }

    public void refreshControls() {
        boolean eraseEnabled = isSelected() && model.isCellEditable(selectedRow, selectedCol)
                && model.getCellValue(selectedRow, selectedCol) != 0;
        view.setEraseEnabled(eraseEnabled);
        view.setUndoEnabled(model.canUndo());
        view.setHintEnabled(model.isHintEnabled());
    }

    private void reportInvalidIfNeeded(int row, int col) {
        if (!model.isValidationFeedbackEnabled()) {
            return;
        }
        if (model.getInvalidCells().contains(new CellPosition(row, col))) {
            view.showStatusMessage("Invalid move: duplicate detected in row, column, or 3x3 block.");
        }
    }

    private boolean isSelected() {
        return isInBounds(selectedRow, selectedCol);
    }

    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < SudokuModel.SIZE && col >= 0 && col < SudokuModel.SIZE;
    }
}

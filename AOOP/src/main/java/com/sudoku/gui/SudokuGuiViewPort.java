package sudoku.gui;

public interface SudokuGuiViewPort {
    void setEraseEnabled(boolean enabled);

    void setUndoEnabled(boolean enabled);

    void setHintEnabled(boolean enabled);

    void showStatusMessage(String message);
}

package sudoku.gui;

import sudoku.model.SudokuGameModel;

import javax.swing.SwingUtilities;

public final class SudokuGuiMain {
    private SudokuGuiMain() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SudokuSwingView view = new SudokuSwingView(new SudokuGameModel());
            view.setVisible(true);
        });
    }
}

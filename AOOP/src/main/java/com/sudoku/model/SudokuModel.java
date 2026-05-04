package com.sudoku.model;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Observable;

public class SudokuModel extends Observable {
    private Board currentBoard;
    private Board initialBoard;
    private List<int[][]> puzzleList;
    private int lastMoveRow = -1;
    private int lastMoveCol = -1;
    private int lastMoveValue = 0;
    private final Random random = new Random();

    private boolean validationFeedbackEnabled = true;
    private boolean hintEnabled = true;
    private boolean randomPuzzleEnabled = true;

    public SudokuModel(String puzzleFilePath) throws IOException {
        this.puzzleList = PuzzleLoader.loadPuzzles(puzzleFilePath);
        loadNewPuzzle();
    }

    public boolean setCellValue(int row, int col, int value) {
        if (validationFeedbackEnabled && !currentBoard.isValidMove(row, col, value)) {
            return false;
        }
        Cell cell = currentBoard.getCell(row, col);
        if (cell == null || !cell.isEditable()) {
            return false;
        }
        this.lastMoveRow = row;
        this.lastMoveCol = col;
        this.lastMoveValue = cell.getValue();
        boolean success = cell.setValue(value);
        if (success) {
            notifyModelChanged();
        }
        return success;
    }

    public boolean clearCell(int row, int col) {
        Cell cell = currentBoard.getCell(row, col);
        if (cell == null || !cell.isEditable()) {
            return false;
        }
        this.lastMoveRow = row;
        this.lastMoveCol = col;
        this.lastMoveValue = cell.getValue();
        cell.clear();
        notifyModelChanged();
        return true;
    }

    public void loadNewPuzzle() {
        int[][] selectedPuzzle;
        if (randomPuzzleEnabled) {
            int randomIndex = random.nextInt(puzzleList.size());
            selectedPuzzle = puzzleList.get(randomIndex);
        } else {
            selectedPuzzle = puzzleList.get(0);
        }
        this.initialBoard = new Board(selectedPuzzle);
        this.currentBoard = initialBoard.deepCopy();
        resetLastMove();
        notifyModelChanged();
    }

    public void resetPuzzle() {
        this.currentBoard = initialBoard.deepCopy();
        resetLastMove();
        notifyModelChanged();
    }

    public boolean isGameWon() {
        return currentBoard.isBoardComplete() && currentBoard.isBoardValid();
    }

    public boolean isBoardValid() {
        return currentBoard.isBoardValid();
    }

    public boolean isCellValid(int row, int col) {
        return currentBoard.isCellValid(row, col);
    }

    public boolean isValidationFeedbackEnabled() {
        return validationFeedbackEnabled;
    }

    public void setValidationFeedbackEnabled(boolean validationFeedbackEnabled) {
        this.validationFeedbackEnabled = validationFeedbackEnabled;
        notifyModelChanged();
    }

    public boolean isHintEnabled() {
        return hintEnabled;
    }

    public void setHintEnabled(boolean hintEnabled) {
        this.hintEnabled = hintEnabled;
    }

    public boolean isRandomPuzzleEnabled() {
        return randomPuzzleEnabled;
    }

    public void setRandomPuzzleEnabled(boolean randomPuzzleEnabled) {
        this.randomPuzzleEnabled = randomPuzzleEnabled;
    }

    public Board getCurrentBoard() {
        return currentBoard;
    }

    public Board getInitialBoard() {
        return initialBoard;
    }

    private void notifyModelChanged() {
        setChanged();
        notifyObservers();
    }

    private void resetLastMove() {
        this.lastMoveRow = -1;
        this.lastMoveCol = -1;
        this.lastMoveValue = 0;
    }

    public boolean undoLastMove() {
        return false;
    }

    public boolean giveHint() {
        return false;
    }
}
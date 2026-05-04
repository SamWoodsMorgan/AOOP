package com.sudoku.model;

public class Board {
    private static final int GRID_SIZE = 9;
    private static final int SUBGRID_SIZE = 3;
    private final Cell[][] grid;
    private final int[][] initialValues;

    public Board(int[][] initialValues) {
        this.initialValues = deepCopyArray(initialValues);
        this.grid = new Cell[GRID_SIZE][GRID_SIZE];
        initGrid(initialValues);
    }

    private void initGrid(int[][] initialValues) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int value = initialValues[row][col];
                boolean preFilled = value != 0;
                grid[row][col] = new Cell(row, col, value, preFilled);
            }
        }
    }

    public Cell getCell(int row, int col) {
        if (isIndexOutOfBounds(row, col)) {
            return null;
        }
        return grid[row][col];
    }

    public boolean isValidMove(int row, int col, int value) {
        if (isIndexOutOfBounds(row, col) || value < 1 || value > 9) {
            return false;
        }
        if (grid[row][col].isPreFilled()) {
            return false;
        }
        for (int i = 0; i < GRID_SIZE; i++) {
            if (grid[row][i].getValue() == value) {
                return false;
            }
        }
        for (int i = 0; i < GRID_SIZE; i++) {
            if (grid[i][col].getValue() == value) {
                return false;
            }
        }
        int subGridRowStart = (row / SUBGRID_SIZE) * SUBGRID_SIZE;
        int subGridColStart = (col / SUBGRID_SIZE) * SUBGRID_SIZE;
        for (int i = subGridRowStart; i < subGridRowStart + SUBGRID_SIZE; i++) {
            for (int j = subGridColStart; j < subGridColStart + SUBGRID_SIZE; j++) {
                if (grid[i][j].getValue() == value) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isBoardComplete() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (grid[row][col].getValue() == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isBoardValid() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int value = grid[row][col].getValue();
                if (value != 0 && !isCellValid(row, col)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isCellValid(int row, int col) {
        if (isIndexOutOfBounds(row, col)) {
            return false;
        }
        int value = grid[row][col].getValue();
        if (value == 0) {
            return true;
        }
        grid[row][col].clear();
        boolean isValid = isValidMove(row, col, value);
        grid[row][col].setValue(value);
        return isValid;
    }

    public void resetToInitial() {
        initGrid(initialValues);
    }

    public Board deepCopy() {
        int[][] copyValues = deepCopyArray(initialValues);
        Board copyBoard = new Board(copyValues);
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                copyBoard.getCell(row, col).setValue(this.grid[row][col].getValue());
            }
        }
        return copyBoard;
    }

    private int[][] deepCopyArray(int[][] source) {
        int[][] copy = new int[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(source[i], 0, copy[i], 0, GRID_SIZE);
        }
        return copy;
    }

    private boolean isIndexOutOfBounds(int row, int col) {
        return row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE;
    }
}
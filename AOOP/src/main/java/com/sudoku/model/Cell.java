package com.sudoku.model;

public class Cell {
    private final int row;
    private final int col;
    private int value;
    private final boolean preFilled;

    public Cell(int row, int col, int value, boolean preFilled) {
        this.row = row;
        this.col = col;
        this.value = value;
        this.preFilled = preFilled;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getValue() {
        return value;
    }

    public boolean setValue(int value) {
        if (preFilled) {
            return false;
        }
        if (value < 0 || value > 9) {
            return false;
        }
        this.value = value;
        return true;
    }

    public boolean isPreFilled() {
        return preFilled;
    }

    public boolean isEditable() {
        return !preFilled;
    }

    public void clear() {
        if (isEditable()) {
            this.value = 0;
        }
    }
}
package sudoku.model;

import java.util.Objects;

public final class CellPosition {
    private final int row;
    private final int col;

    public CellPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int row() {
        return row;
    }

    public int col() {
        return col;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CellPosition that)) {
            return false;
        }
        return row == that.row && col == that.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}

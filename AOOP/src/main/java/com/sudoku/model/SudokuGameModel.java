package sudoku.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Random;
import java.util.Set;

@SuppressWarnings("deprecation")
public class SudokuGameModel extends Observable implements SudokuModel {
    private static final String PUZZLE_RESOURCE = "/puzzles.txt";

    private final List<int[][]> puzzleBank = new ArrayList<>();
    private final Random random = new Random();

    private int[][] initialBoard = new int[SIZE][SIZE];
    private int[][] board = new int[SIZE][SIZE];
    private boolean[][] fixedCells = new boolean[SIZE][SIZE];
    private int[][] solutionBoard = new int[SIZE][SIZE];

    private boolean validationFeedbackEnabled = true;
    private boolean hintEnabled = true;
    private boolean randomPuzzleSelectionEnabled = true;
    private int fixedPuzzleIndex = 0;
    private int currentPuzzleIndex = 0;

    private Move lastMove;

    private enum ActionType {
        SET,
        CLEAR,
        HINT
    }

    private static final class Move {
        private final int row;
        private final int col;
        private final int previousValue;
        private final int newValue;
        private final ActionType actionType;

        private Move(int row, int col, int previousValue, int newValue, ActionType actionType) {
            this.row = row;
            this.col = col;
            this.previousValue = previousValue;
            this.newValue = newValue;
            this.actionType = actionType;
        }
    }

    public SudokuGameModel() {
        loadPuzzles();
        assert !puzzleBank.isEmpty();
        newGame();
    }

    /**
     * @invariant board != null && initialBoard != null && fixedCells != null && solutionBoard != null
     * @invariant board.length == 9 && initialBoard.length == 9 && fixedCells.length == 9 && solutionBoard.length == 9
     * @invariant (forall r,c : 0<=r,c<9 ==> 0 <= board[r][c] <= 9)
     * @invariant (forall r,c : 0<=r,c<9 ==> fixedCells[r][c] <==> initialBoard[r][c] != 0)
     */
    private void assertInvariant() {
        assert board != null && initialBoard != null && fixedCells != null && solutionBoard != null;
        assert board.length == SIZE;
        assert initialBoard.length == SIZE;
        assert fixedCells.length == SIZE;
        assert solutionBoard.length == SIZE;

        for (int r = 0; r < SIZE; r++) {
            assert board[r] != null && board[r].length == SIZE;
            assert initialBoard[r] != null && initialBoard[r].length == SIZE;
            assert fixedCells[r] != null && fixedCells[r].length == SIZE;
            assert solutionBoard[r] != null && solutionBoard[r].length == SIZE;
            for (int c = 0; c < SIZE; c++) {
                assert board[r][c] >= 0 && board[r][c] <= SIZE;
                assert initialBoard[r][c] >= 0 && initialBoard[r][c] <= SIZE;
                assert solutionBoard[r][c] >= 1 && solutionBoard[r][c] <= SIZE;
                assert fixedCells[r][c] == (initialBoard[r][c] != 0);
            }
        }
    }

    /**
     * @requires 0 <= row,col < 9
     * @ensures \result == board[row][col]
     */
    @Override
    public int getCellValue(int row, int col) {
        assertInvariant();
        assert isInBounds(row, col);
        return board[row][col];
    }

    /**
     * @requires 0 <= row,col < 9
     * @ensures \result == (initialBoard[row][col] != 0)
     */
    @Override
    public boolean isCellFixed(int row, int col) {
        assertInvariant();
        assert isInBounds(row, col);
        return fixedCells[row][col];
    }

    /**
     * @requires 0 <= row,col < 9
     * @ensures \result == !isCellFixed(row,col)
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        assertInvariant();
        assert isInBounds(row, col);
        return !fixedCells[row][col];
    }

    /**
     * @requires 0 <= row,col < 9 && 1 <= value <= 9
     * @ensures isCellFixed(row,col) ==> board[row][col] unchanged
     * @ensures !isCellFixed(row,col) ==> board[row][col] == value
     */
    @Override
    public boolean setCellValue(int row, int col, int value) {
        assertInvariant();
        assert isInBounds(row, col);
        assert value >= 1 && value <= SIZE;

        if (!isCellEditable(row, col)) {
            return false;
        }

        int previousValue = board[row][col];
        if (previousValue == value) {
            return false;
        }

        board[row][col] = value;
        lastMove = new Move(row, col, previousValue, value, ActionType.SET);
        fireModelChanged();

        assert board[row][col] == value;
        assertInvariant();
        return true;
    }

    /**
     * @requires 0 <= row,col < 9
     * @ensures isCellFixed(row,col) ==> board[row][col] unchanged
     * @ensures !isCellFixed(row,col) ==> board[row][col] == 0
     */
    @Override
    public boolean clearCell(int row, int col) {
        assertInvariant();
        assert isInBounds(row, col);

        if (!isCellEditable(row, col)) {
            return false;
        }

        int previousValue = board[row][col];
        if (previousValue == 0) {
            return false;
        }

        board[row][col] = 0;
        lastMove = new Move(row, col, previousValue, 0, ActionType.CLEAR);
        fireModelChanged();

        assert board[row][col] == 0;
        assertInvariant();
        return true;
    }

    /**
     * @ensures \result ==> board restored to previous state for one latest user action
     * @ensures !\result ==> board unchanged
     */
    @Override
    public boolean undoLastAction() {
        assertInvariant();

        if (lastMove == null) {
            return false;
        }

        board[lastMove.row][lastMove.col] = lastMove.previousValue;
        lastMove = null;
        fireModelChanged();

        assertInvariant();
        return true;
    }

    @Override
    public boolean canUndo() {
        return lastMove != null;
    }

    /**
     * @requires hintEnabled
     * @ensures \result ==> one editable empty cell becomes correct value
     * @ensures !\result ==> board unchanged
     */
    @Override
    public boolean revealHint() {
        assertInvariant();

        if (!hintEnabled) {
            return false;
        }

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (isCellEditable(r, c) && board[r][c] == 0) {
                    int solvedValue = solutionBoard[r][c];
                    board[r][c] = solvedValue;
                    lastMove = new Move(r, c, 0, solvedValue, ActionType.HINT);
                    fireModelChanged();
                    assertInvariant();
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @ensures board == initialBoard
     */
    @Override
    public void resetPuzzle() {
        assertInvariant();
        board = deepCopy(initialBoard);
        lastMove = null;
        fireModelChanged();
        assertInvariant();
    }

    /**
     * @ensures board initialized from one puzzle in puzzleBank
     * @ensures all fixed cells come from non-zero clues in initial puzzle
     */
    @Override
    public void newGame() {
        int puzzleIndex = choosePuzzleIndex();
        int[][] chosenPuzzle = deepCopy(puzzleBank.get(puzzleIndex));
        currentPuzzleIndex = puzzleIndex;

        initialBoard = deepCopy(chosenPuzzle);
        board = deepCopy(chosenPuzzle);
        fixedCells = createFixedCells(initialBoard);
        solutionBoard = solveOrThrow(initialBoard);
        lastMove = null;
        fireModelChanged();
        assertInvariant();
    }

    /**
     * @ensures \result == true <==> board is full and satisfies Sudoku rules
     */
    @Override
    public boolean isComplete() {
        assertInvariant();
        if (hasAnyEmptyCell(board)) {
            return false;
        }
        return getInvalidCells().isEmpty();
    }

    /**
     * @ensures \result contains exactly all non-empty cells violating row/column/sub-grid uniqueness
     */
    @Override
    public Set<CellPosition> getInvalidCells() {
        assertInvariant();
        Set<CellPosition> invalid = new HashSet<>();

        for (int r = 0; r < SIZE; r++) {
            markDuplicatePositions(getRowPositions(r), invalid);
        }
        for (int c = 0; c < SIZE; c++) {
            markDuplicatePositions(getColumnPositions(c), invalid);
        }
        for (int blockRow = 0; blockRow < SIZE; blockRow += 3) {
            for (int blockCol = 0; blockCol < SIZE; blockCol += 3) {
                markDuplicatePositions(getBlockPositions(blockRow, blockCol), invalid);
            }
        }

        return invalid;
    }

    @Override
    public int[][] getBoardSnapshot() {
        assertInvariant();
        return deepCopy(board);
    }

    @Override
    public boolean isValidationFeedbackEnabled() {
        return validationFeedbackEnabled;
    }

    @Override
    public void setValidationFeedbackEnabled(boolean enabled) {
        validationFeedbackEnabled = enabled;
        fireModelChanged();
    }

    @Override
    public boolean isHintEnabled() {
        return hintEnabled;
    }

    @Override
    public void setHintEnabled(boolean enabled) {
        hintEnabled = enabled;
        fireModelChanged();
    }

    @Override
    public boolean isRandomPuzzleSelectionEnabled() {
        return randomPuzzleSelectionEnabled;
    }

    @Override
    public void setRandomPuzzleSelectionEnabled(boolean enabled) {
        if (randomPuzzleSelectionEnabled && !enabled) {
            fixedPuzzleIndex = currentPuzzleIndex;
        }
        randomPuzzleSelectionEnabled = enabled;
        fireModelChanged();
    }

    private int choosePuzzleIndex() {
        if (puzzleBank.isEmpty()) {
            throw new IllegalStateException("No puzzles are available");
        }
        if (!randomPuzzleSelectionEnabled) {
            return fixedPuzzleIndex;
        }
        return random.nextInt(puzzleBank.size());
    }

    private void fireModelChanged() {
        setChanged();
        notifyObservers();
    }

    private void markDuplicatePositions(List<CellPosition> positions, Set<CellPosition> invalid) {
        int[] counts = new int[SIZE + 1];
        for (CellPosition position : positions) {
            int value = board[position.row()][position.col()];
            if (value != 0) {
                counts[value]++;
            }
        }
        for (CellPosition position : positions) {
            int value = board[position.row()][position.col()];
            if (value != 0 && counts[value] > 1) {
                invalid.add(position);
            }
        }
    }

    private List<CellPosition> getRowPositions(int row) {
        List<CellPosition> positions = new ArrayList<>(SIZE);
        for (int c = 0; c < SIZE; c++) {
            positions.add(new CellPosition(row, c));
        }
        return positions;
    }

    private List<CellPosition> getColumnPositions(int col) {
        List<CellPosition> positions = new ArrayList<>(SIZE);
        for (int r = 0; r < SIZE; r++) {
            positions.add(new CellPosition(r, col));
        }
        return positions;
    }

    private List<CellPosition> getBlockPositions(int blockRow, int blockCol) {
        List<CellPosition> positions = new ArrayList<>(SIZE);
        for (int r = blockRow; r < blockRow + 3; r++) {
            for (int c = blockCol; c < blockCol + 3; c++) {
                positions.add(new CellPosition(r, c));
            }
        }
        return positions;
    }

    private static boolean hasAnyEmptyCell(int[][] matrix) {
        for (int[] row : matrix) {
            for (int value : row) {
                if (value == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean[][] createFixedCells(int[][] puzzle) {
        boolean[][] fixed = new boolean[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                fixed[r][c] = puzzle[r][c] != 0;
            }
        }
        return fixed;
    }

    private static int[][] deepCopy(int[][] source) {
        int[][] copy = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = Arrays.copyOf(source[i], source[i].length);
        }
        return copy;
    }

    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    private int[][] solveOrThrow(int[][] puzzle) {
        int[][] working = deepCopy(puzzle);
        if (!solveBoard(working)) {
            throw new IllegalStateException("Puzzle has no solution");
        }
        return working;
    }

    private boolean solveBoard(int[][] working) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (working[r][c] == 0) {
                    for (int candidate = 1; candidate <= SIZE; candidate++) {
                        if (isValidPlacement(working, r, c, candidate)) {
                            working[r][c] = candidate;
                            if (solveBoard(working)) {
                                return true;
                            }
                            working[r][c] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isValidPlacement(int[][] grid, int row, int col, int value) {
        for (int i = 0; i < SIZE; i++) {
            if (grid[row][i] == value || grid[i][col] == value) {
                return false;
            }
        }
        int startRow = (row / 3) * 3;
        int startCol = (col / 3) * 3;
        for (int r = startRow; r < startRow + 3; r++) {
            for (int c = startCol; c < startCol + 3; c++) {
                if (grid[r][c] == value) {
                    return false;
                }
            }
        }
        return true;
    }

    private void loadPuzzles() {
        try {
            List<String> lines = readPuzzleLines();
            parsePuzzles(lines);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load puzzles", e);
        }

        if (puzzleBank.isEmpty()) {
            throw new IllegalStateException("Puzzle file contains no valid puzzles");
        }
    }

    private List<String> readPuzzleLines() throws IOException {
        InputStream in = SudokuGameModel.class.getResourceAsStream(PUZZLE_RESOURCE);
        if (in != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return reader.lines().toList();
            }
        }

        Path fallback = Path.of("src", "main", "resources", "puzzles.txt");
        if (Files.exists(fallback)) {
            return Files.readAllLines(fallback, StandardCharsets.UTF_8);
        }

        throw new IOException("puzzles.txt not found in classpath or project resources");
    }

    private void parsePuzzles(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String normalized = line.replaceAll("\\s+", "");
            if (normalized.length() != SIZE * SIZE) {
                throw new IllegalStateException("Invalid puzzle length at line " + (i + 1)
                        + ": expected 81 characters, got " + normalized.length());
            }

            puzzleBank.add(parsePuzzle(normalized, i + 1));
        }
    }

    private int[][] parsePuzzle(String encoded, int lineNumber) {
        if (encoded.length() != SIZE * SIZE) {
            throw new IllegalArgumentException("Encoded puzzle must contain 81 cells");
        }
        int[][] puzzle = new int[SIZE][SIZE];
        for (int i = 0; i < encoded.length(); i++) {
            char ch = encoded.charAt(i);
            int value;
            if (ch == '.') {
                value = 0;
            } else if (Character.isDigit(ch)) {
                value = ch - '0';
            } else {
                throw new IllegalStateException("Unsupported puzzle character '" + ch
                        + "' at line " + lineNumber + ", index " + i);
            }
            int row = i / SIZE;
            int col = i % SIZE;
            puzzle[row][col] = value;
        }
        return puzzle;
    }
}

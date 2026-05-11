package sudoku.cli;

import sudoku.model.CellPosition;
import sudoku.model.SudokuGameModel;
import sudoku.model.SudokuModel;

import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;

public class SudokuCliMain {
    private final SudokuModel model;

    public SudokuCliMain(SudokuModel model) {
        this.model = model;
    }

    public static void main(String[] args) {
        new SudokuCliMain(new SudokuGameModel()).run();
    }

    public void run() {
        System.out.println("Sudoku CLI - type 'help' for commands.");
        printBoard();

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) {
                    break;
                }

                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }

                if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye.");
                    break;
                }

                handleCommand(line);

                if (model.isComplete()) {
                    System.out.println("Congratulations! Puzzle completed correctly.");
                }

                printBoard();
            }
        }
    }

    private void handleCommand(String line) {
        String[] parts = line.split("\\s+");
        String command = parts[0].toLowerCase();

        switch (command) {
            case "help" -> printHelp();
            case "set" -> handleSet(parts);
            case "clear" -> handleClear(parts);
            case "undo" -> handleUndo();
            case "hint" -> handleHint();
            case "reset" -> {
                model.resetPuzzle();
                System.out.println("Puzzle reset.");
            }
            case "new" -> {
                model.newGame();
                System.out.println("New puzzle loaded.");
            }
            default -> System.out.println("Unknown command. Type 'help' for valid commands.");
        }
    }

    private void handleSet(String[] parts) {
        if (parts.length != 4) {
            System.out.println("Usage: set <row 1-9> <col 1-9> <value 1-9>");
            return;
        }

        int[] values = parseThreeInts(parts, "Usage: set <row 1-9> <col 1-9> <value 1-9>");
        if (values == null) {
            return;
        }

        int row = values[0] - 1;
        int col = values[1] - 1;
        int value = values[2];

        if (!isBoardPosition(row, col) || value < 1 || value > SudokuModel.SIZE) {
            System.out.println("Only row/col/value in range 1-9 are allowed.");
            return;
        }

        if (!model.setCellValue(row, col, value)) {
            System.out.println("Move rejected. The target cell may be pre-filled or unchanged.");
            return;
        }

        printInvalidFeedbackIfNeeded(row, col);
    }

    private void handleClear(String[] parts) {
        if (parts.length != 3) {
            System.out.println("Usage: clear <row 1-9> <col 1-9>");
            return;
        }

        int[] values = parseTwoInts(parts, "Usage: clear <row 1-9> <col 1-9>");
        if (values == null) {
            return;
        }

        int row = values[0] - 1;
        int col = values[1] - 1;

        if (!isBoardPosition(row, col)) {
            System.out.println("Only row and col in range 1-9 are allowed.");
            return;
        }

        if (!model.clearCell(row, col)) {
            System.out.println("Clear rejected. The target cell may be pre-filled or already empty.");
            return;
        }

        printInvalidFeedbackIfNeeded(row, col);
    }

    private void handleUndo() {
        if (!model.undoLastAction()) {
            System.out.println("No move to undo.");
        }
    }

    private void handleHint() {
        if (!model.revealHint()) {
            if (!model.isHintEnabled()) {
                System.out.println("Hint is disabled by flag.");
            } else {
                System.out.println("No available cell for hint.");
            }
        }
    }

    private void printHelp() {
        System.out.println("Commands:");
        System.out.println("  set <row> <col> <value>   Put value 1-9 in editable cell");
        System.out.println("  clear <row> <col>         Clear an editable cell");
        System.out.println("  undo                      Undo the most recent action");
        System.out.println("  hint                      Fill one empty editable cell with correct value");
        System.out.println("  reset                     Restore puzzle to initial state");
        System.out.println("  new                       Load a new puzzle");
        System.out.println("  help                      Show this help");
        System.out.println("  quit                      Exit CLI");
    }

    private void printInvalidFeedbackIfNeeded(int row, int col) {
        if (!model.isValidationFeedbackEnabled()) {
            return;
        }

        Set<CellPosition> invalidCells = model.getInvalidCells();
        if (invalidCells.contains(new CellPosition(row, col))) {
            System.out.println("Invalid move: duplicate number in row/column/sub-grid.");
        }
    }

    private void printBoard() {
        int[][] board = model.getBoardSnapshot();

        System.out.println("+-------+-------+-------+");
        for (int r = 0; r < SudokuModel.SIZE; r++) {
            StringBuilder line = new StringBuilder("| ");
            for (int c = 0; c < SudokuModel.SIZE; c++) {
                int value = board[r][c];
                line.append(value == 0 ? "." : Integer.toString(value));
                line.append(' ');

                if ((c + 1) % 3 == 0) {
                    line.append("| ");
                }
            }
            System.out.println(line);
            if ((r + 1) % 3 == 0) {
                System.out.println("+-------+-------+-------+");
            }
        }
    }

    private int[] parseTwoInts(String[] parts, String usage) {
        try {
            return new int[]{Integer.parseInt(parts[1]), Integer.parseInt(parts[2])};
        } catch (NumberFormatException ex) {
            System.out.println(usage);
            return null;
        }
    }

    private int[] parseThreeInts(String[] parts, String usage) {
        try {
            return new int[]{Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3])};
        } catch (NumberFormatException ex) {
            System.out.println(usage);
            return null;
        }
    }

    private boolean isBoardPosition(int row, int col) {
        return row >= 0 && row < SudokuModel.SIZE && col >= 0 && col < SudokuModel.SIZE;
    }
}

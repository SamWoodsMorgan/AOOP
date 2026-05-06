package sudoku.gui;

import sudoku.model.CellPosition;
import sudoku.model.SudokuModel;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

@SuppressWarnings("deprecation")
public class SudokuSwingView extends JFrame implements Observer, SudokuGuiViewPort {
    private static final Color COLOR_FIXED = new Color(232, 232, 232);
    private static final Color COLOR_EDITABLE = Color.WHITE;
    private static final Color COLOR_SELECTED = new Color(195, 223, 255);
    private static final Color COLOR_INVALID = new Color(255, 205, 205);

    private final SudokuModel model;
    private final SudokuController controller;

    private final JButton[][] cellButtons = new JButton[SudokuModel.SIZE][SudokuModel.SIZE];
    private final JButton eraseButton = new JButton("Erase");
    private final JButton undoButton = new JButton("Undo");
    private final JButton hintButton = new JButton("Hint");
    private final JButton resetButton = new JButton("Reset");
    private final JButton newGameButton = new JButton("New Game");

    private final JCheckBox validationCheck = new JCheckBox("Validation Feedback", true);
    private final JCheckBox hintCheck = new JCheckBox("Hint Enabled", true);
    private final JCheckBox randomPuzzleCheck = new JCheckBox("Random Puzzle", true);

    private final JLabel statusLabel = new JLabel("Select a cell and start playing.");

    private boolean completionMessageShown;

    public SudokuSwingView(SudokuModel model) {
        super("Sudoku - AOOP Coursework");
        this.model = model;
        this.controller = new SudokuController(model, this);
        ((Observable) this.model).addObserver(this);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        add(buildBoardPanel(), BorderLayout.CENTER);
        add(buildRightPanel(), BorderLayout.EAST);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        installKeyboardBindings();

        pack();
        setMinimumSize(new Dimension(980, 700));
        setLocationRelativeTo(null);
        refreshBoard();
    }

    private JPanel buildBoardPanel() {
        JPanel panel = new JPanel(new GridLayout(SudokuModel.SIZE, SudokuModel.SIZE));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int r = 0; r < SudokuModel.SIZE; r++) {
            for (int c = 0; c < SudokuModel.SIZE; c++) {
                JButton button = new JButton("");
                button.setFocusPainted(false);
                button.setMargin(new Insets(0, 0, 0, 0));
                button.setFont(new Font("SansSerif", Font.BOLD, 24));
                button.setPreferredSize(new Dimension(62, 62));

                int top = (r % 3 == 0) ? 3 : 1;
                int left = (c % 3 == 0) ? 3 : 1;
                int bottom = (r == SudokuModel.SIZE - 1) ? 3 : 1;
                int right = (c == SudokuModel.SIZE - 1) ? 3 : 1;
                button.setBorder(BorderFactory.createMatteBorder(top, left, bottom, right, Color.DARK_GRAY));

                final int row = r;
                final int col = c;
                button.addActionListener(event -> {
                    controller.selectCell(row, col);
                    refreshBoard();
                });

                cellButtons[r][c] = button;
                panel.add(button);
            }
        }

        return panel;
    }

    private JPanel buildRightPanel() {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 10));

        eraseButton.addActionListener(event -> controller.eraseSelectedCell());
        undoButton.addActionListener(event -> controller.undo());
        hintButton.addActionListener(event -> controller.hint());
        resetButton.addActionListener(event -> controller.reset());
        newGameButton.addActionListener(event -> controller.newGame());

        validationCheck.addActionListener(event -> controller.setValidationFeedbackEnabled(validationCheck.isSelected()));
        hintCheck.addActionListener(event -> controller.setHintEnabled(hintCheck.isSelected()));
        randomPuzzleCheck.addActionListener(event -> controller.setRandomPuzzleSelectionEnabled(randomPuzzleCheck.isSelected()));

        rightPanel.add(eraseButton);
        rightPanel.add(undoButton);
        rightPanel.add(hintButton);
        rightPanel.add(resetButton);
        rightPanel.add(newGameButton);
        rightPanel.add(new JLabel(" "));
        rightPanel.add(validationCheck);
        rightPanel.add(hintCheck);
        rightPanel.add(randomPuzzleCheck);
        rightPanel.add(new JLabel(" "));
        rightPanel.add(new JLabel("Virtual Keyboard", SwingConstants.CENTER));
        rightPanel.add(buildVirtualKeyboard());

        return rightPanel;
    }

    private JPanel buildVirtualKeyboard() {
        JPanel keyboard = new JPanel(new GridLayout(3, 3, 4, 4));
        keyboard.setMaximumSize(new Dimension(240, 180));
        keyboard.setPreferredSize(new Dimension(240, 180));

        for (int value = 1; value <= SudokuModel.SIZE; value++) {
            JButton keyButton = new JButton(String.valueOf(value));
            int digit = value;
            keyButton.addActionListener(event -> controller.inputDigit(digit));
            keyboard.add(keyButton);
        }

        return keyboard;
    }

    private JPanel buildBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(new Color(248, 248, 248));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        return bottomPanel;
    }

    private void installKeyboardBindings() {
        Map<KeyStroke, Runnable> mappings = new HashMap<>();
        for (int value = 1; value <= SudokuModel.SIZE; value++) {
            int digit = value;
            mappings.put(KeyStroke.getKeyStroke((char) ('0' + value)), () -> controller.inputDigit(digit));
            mappings.put(KeyStroke.getKeyStroke("NUMPAD" + value), () -> controller.inputDigit(digit));
        }

        mappings.put(KeyStroke.getKeyStroke("BACK_SPACE"), controller::eraseSelectedCell);
        mappings.put(KeyStroke.getKeyStroke("DELETE"), controller::eraseSelectedCell);
        mappings.put(KeyStroke.getKeyStroke("UP"), () -> controller.moveSelection(-1, 0));
        mappings.put(KeyStroke.getKeyStroke("DOWN"), () -> controller.moveSelection(1, 0));
        mappings.put(KeyStroke.getKeyStroke("LEFT"), () -> controller.moveSelection(0, -1));
        mappings.put(KeyStroke.getKeyStroke("RIGHT"), () -> controller.moveSelection(0, 1));

        for (Map.Entry<KeyStroke, Runnable> entry : mappings.entrySet()) {
            String key = "KEY_" + entry.getKey().toString();
            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(entry.getKey(), key);
            getRootPane().getActionMap().put(key, new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    entry.getValue().run();
                    refreshBoard();
                }
            });
        }
    }

    private void refreshBoard() {
        int[][] board = model.getBoardSnapshot();
        Set<CellPosition> invalidCells = model.getInvalidCells();
        boolean showInvalid = model.isValidationFeedbackEnabled();

        validationCheck.setSelected(model.isValidationFeedbackEnabled());
        hintCheck.setSelected(model.isHintEnabled());
        randomPuzzleCheck.setSelected(model.isRandomPuzzleSelectionEnabled());

        int selectedRow = controller.getSelectedRow();
        int selectedCol = controller.getSelectedCol();

        for (int r = 0; r < SudokuModel.SIZE; r++) {
            for (int c = 0; c < SudokuModel.SIZE; c++) {
                JButton button = cellButtons[r][c];
                int value = board[r][c];
                button.setText(value == 0 ? "" : String.valueOf(value));

                if (model.isCellFixed(r, c)) {
                    button.setFont(new Font("SansSerif", Font.BOLD, 24));
                    button.setForeground(new Color(40, 40, 40));
                    button.setBackground(COLOR_FIXED);
                } else {
                    button.setFont(new Font("SansSerif", Font.PLAIN, 24));
                    button.setForeground(new Color(30, 60, 130));
                    button.setBackground(COLOR_EDITABLE);
                }

                if (showInvalid && invalidCells.contains(new CellPosition(r, c))) {
                    button.setBackground(COLOR_INVALID);
                }

                if (r == selectedRow && c == selectedCol) {
                    button.setBackground(COLOR_SELECTED);
                }
            }
        }

        controller.refreshControls();

        if (model.isComplete() && !completionMessageShown) {
            completionMessageShown = true;
            JOptionPane.showMessageDialog(this, "Congratulations! You have completed the puzzle.",
                    "Puzzle Completed", JOptionPane.INFORMATION_MESSAGE);
        } else if (!model.isComplete()) {
            completionMessageShown = false;
        }
    }

    @Override
    public void update(Observable observable, Object argument) {
        SwingUtilities.invokeLater(this::refreshBoard);
    }

    @Override
    public void setEraseEnabled(boolean enabled) {
        eraseButton.setEnabled(enabled);
    }

    @Override
    public void setUndoEnabled(boolean enabled) {
        undoButton.setEnabled(enabled);
    }

    @Override
    public void setHintEnabled(boolean enabled) {
        hintButton.setEnabled(enabled);
    }

    @Override
    public void showStatusMessage(String message) {
        statusLabel.setText(message);
    }
}

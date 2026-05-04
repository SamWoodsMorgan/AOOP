package com.sudoku.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PuzzleLoader {
    private static final int GRID_SIZE = 9;

    public static List<int[][]> loadPuzzles(String filePath) throws IOException {
        List<int[][]> puzzles = new ArrayList<>();

        try (InputStream inputStream = PuzzleLoader.class.getClassLoader().getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                int[][] puzzle = parsePuzzleLine(line);
                if (puzzle != null) {
                    puzzles.add(puzzle);
                }
            }
        }

        if (puzzles.isEmpty()) {
            throw new IOException("谜题文件中未找到有效谜题");
        }
        return puzzles;
    }

    private static int[][] parsePuzzleLine(String line) {
        String[] numberStrs = line.split(",");
        if (numberStrs.length != GRID_SIZE * GRID_SIZE) {
            return null;
        }

        int[][] puzzle = new int[GRID_SIZE][GRID_SIZE];
        try {
            for (int i = 0; i < numberStrs.length; i++) {
                int row = i / GRID_SIZE;
                int col = i % GRID_SIZE;
                puzzle[row][col] = Integer.parseInt(numberStrs[i].trim());
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return puzzle;
    }
}
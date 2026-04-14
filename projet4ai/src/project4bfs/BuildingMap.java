package project4bfs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuildingMap {
    public static final char WALL = '#';
    public static final char FREE = '.';
    public static final char START = 'S';
    public static final char EXIT = 'E';
    public static final char PATH = '*';
    public static final char BLOCKED = 'X';
    public static final char FIRE = 'F';

    private final char[][] grid;
    private final int rows;
    private final int cols;

    public BuildingMap(char[][] source) {
        if (source == null || source.length == 0 || source[0].length == 0) {
            throw new IllegalArgumentException("Grid must not be empty.");
        }

        this.rows = source.length;
        this.cols = source[0].length;
        this.grid = new char[rows][cols];

        for (int i = 0; i < rows; i++) {
            if (source[i].length != cols) {
                throw new IllegalArgumentException("All rows must have same size.");
            }
            System.arraycopy(source[i], 0, this.grid[i], 0, cols);
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public char getCell(int row, int col) {
        return grid[row][col];
    }

    public void setCell(int row, int col, char value) {
        grid[row][col] = value;
    }

    public boolean isInside(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public boolean isWalkable(int row, int col) {
        if (!isInside(row, col)) {
            return false;
        }
        char c = grid[row][col];
        return c == FREE || c == START || c == EXIT;
    }

    public boolean isExit(int row, int col) {
        return isInside(row, col) && grid[row][col] == EXIT;
    }

    public Node findStart() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == START) {
                    return new Node(i, j);
                }
            }
        }
        return null;
    }

    public List<Node> findExits() {
        List<Node> exits = new ArrayList<Node>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == EXIT) {
                    exits.add(new Node(i, j));
                }
            }
        }
        return exits;
    }

    public List<Node> getNeighbors(Node node) {
        if (node == null) {
            return Collections.emptyList();
        }

        int[][] directions = {
                {-1, 0},
                {1, 0},
                {0, -1},
                {0, 1}
        };

        List<Node> neighbors = new ArrayList<Node>();
        for (int[] dir : directions) {
            int newRow = node.getRow() + dir[0];
            int newCol = node.getCol() + dir[1];
            if (isWalkable(newRow, newCol)) {
                neighbors.add(new Node(newRow, newCol));
            }
        }
        return neighbors;
    }

    public void clearPathMarkers() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == PATH) {
                    grid[i][j] = FREE;
                }
            }
        }
    }

    public void blockCell(int row, int col) {
        if (isInside(row, col) && grid[row][col] == FREE) {
            grid[row][col] = BLOCKED;
        }
    }

    public void unblockCell(int row, int col) {
        if (isInside(row, col) && grid[row][col] == BLOCKED) {
            grid[row][col] = FREE;
        }
    }

    public void igniteCell(int row, int col) {
        if (isInside(row, col) && (grid[row][col] == FREE || grid[row][col] == BLOCKED)) {
            grid[row][col] = FIRE;
        }
    }

    public BuildingMap copy() {
        return new BuildingMap(this.grid);
    }

    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                sb.append(grid[i][j]);
            }
            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }
}

package project4bfs;

import java.util.ArrayList;
import java.util.List;

public class SimulationManager {
    private BuildingMap workingMap;
    private final char[][] baseGrid;

    public SimulationManager(char[][] baseGrid) {
        this.baseGrid = deepCopy(baseGrid);
        this.workingMap = new BuildingMap(this.baseGrid);
    }

    public void reset() {
        this.workingMap = new BuildingMap(deepCopy(baseGrid));
    }

    public BuildingMap getWorkingMap() {
        return workingMap;
    }

    public BFSExplorer.SearchResult runBFS() {
        BFSExplorer explorer = new BFSExplorer(workingMap);
        return explorer.search();
    }

    public DFSExplorer.SearchResult runDFS() {
        DFSExplorer explorer = new DFSExplorer(workingMap);
        return explorer.search();
    }

    public void applyBlockedCorridor(List<Node> cells) {
        for (Node node : cells) {
            workingMap.blockCell(node.getRow(), node.getCol());
        }
    }

    public void applyClosedDoor(int row, int col) {
        workingMap.blockCell(row, col);
    }

    public void igniteInitialFire(int row, int col) {
        workingMap.igniteCell(row, col);
    }

    public void spreadFireStep() {
        List<Node> toIgnite = new ArrayList<>();
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};

        for (int i = 0; i < workingMap.getRows(); i++) {
            for (int j = 0; j < workingMap.getCols(); j++) {
                if (workingMap.getCell(i, j) == BuildingMap.FIRE) {
                    for (int[] d : dirs) {
                        int nr = i + d[0];
                        int nc = j + d[1];

                        if (workingMap.isInside(nr, nc)) {
                            char c = workingMap.getCell(nr, nc);
                            if (c == BuildingMap.FREE || c == BuildingMap.BLOCKED) {
                                toIgnite.add(new Node(nr, nc));
                            }
                        }
                    }
                }
            }
        }

        for (Node node : toIgnite) {
            workingMap.igniteCell(node.getRow(), node.getCol());
        }
    }

    private char[][] deepCopy(char[][] source) {
        char[][] copy = new char[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i].clone();
        }
        return copy;
    }
}
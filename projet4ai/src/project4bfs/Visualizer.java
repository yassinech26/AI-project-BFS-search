package project4bfs;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Visualizer extends Application {

    private static final int CELL_SIZE = 20;

    private enum EditMode {
        NONE,
        BLOCK,
        FIRE,
        SET_START,
        SET_EXIT
    }

    private SimulationManager simulationManager;
    private BuildingMap map;

    private Canvas canvas;
    private Label distanceLabel;
    private Label exploredLabel;
    private Label statusLabel;
    private TextArea pathArea;

    private Button btnRun;
    private Button btnDFS;
    private Button btnSetStart;
    private Button btnSetExit;
    private Button btnBlockMode;
    private Button btnFireMode;
    private Button btnReset;

    private Timeline currentTimeline;
    private EditMode currentMode = EditMode.NONE;

    @Override
    public void start(Stage stage) {
        char[][] initialGrid = {
                "###################################".toCharArray(),
                "#S........................#......E#".toCharArray(),
                "#.##.#.######.#.####.###.#.####.#.#".toCharArray(),
                "#....#..........#......#.#....#...#".toCharArray(),
                "#.##.######.#.#.#.##.#.#.#.##.#.#.#".toCharArray(),
                "#....#....#.#.#.#.#....#......#...#".toCharArray(),
                "#.####.##.#.#.#.#.#.##..##.#.###.##".toCharArray(),
                "#.#....##.#...#...#......#.#......#".toCharArray(),
                "#.#.######...######.####.#.####.#.#".toCharArray(),
                "#.#........#......#....#.#....#.#E#".toCharArray(),
                "#.########.#.####.####.#.####.#.#.#".toCharArray(),
                "#....#.....#...........#....#.#...#".toCharArray(),
                "####.#.#####.#..##########.#.###.##".toCharArray(),
                "#..........#.#...........#.#....#.#".toCharArray(),
                "#.########.#.###########.#.####.#.#".toCharArray(),
                "#..........#.....E.......#......#.#".toCharArray(),
                "###################################".toCharArray()
        };

        simulationManager = new SimulationManager(initialGrid);
        map = simulationManager.getWorkingMap();

        canvas = new Canvas(map.getCols() * CELL_SIZE, map.getRows() * CELL_SIZE);
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleCanvasClick);

        btnRun = new Button("Run BFS");
        btnDFS = new Button("Run DFS");
        btnSetStart = new Button("Set Start (S)");
        btnSetExit = new Button("Set Exit (E)");
        btnBlockMode = new Button("Block mode");
        btnFireMode = new Button("Fire mode");
        btnReset = new Button("Reset");

        styleButton(btnRun, "#2ecc71");
        styleButton(btnDFS, "#8b5cf6");
        styleButton(btnSetStart, "#06b6d4");
        styleButton(btnSetExit, "#ec4899");
        styleButton(btnBlockMode, "#f39c12");
        styleButton(btnFireMode, "#e74c3c");
        styleButton(btnReset, "#3498db");

        btnRun.setOnAction(e -> runAndAnimateBFS());
        btnDFS.setOnAction(e -> runAndAnimateDFS());

        btnSetStart.setOnAction(e -> {
            stopAnimation();
            currentMode = (currentMode == EditMode.SET_START) ? EditMode.NONE : EditMode.SET_START;
            updateModeStatus();
            refreshMapOnly();
        });

        btnSetExit.setOnAction(e -> {
            stopAnimation();
            currentMode = (currentMode == EditMode.SET_EXIT) ? EditMode.NONE : EditMode.SET_EXIT;
            updateModeStatus();
            refreshMapOnly();
        });

        btnBlockMode.setOnAction(e -> {
            stopAnimation();
            currentMode = (currentMode == EditMode.BLOCK) ? EditMode.NONE : EditMode.BLOCK;
            updateModeStatus();
            refreshMapOnly();
        });

        btnFireMode.setOnAction(e -> {
            stopAnimation();
            currentMode = (currentMode == EditMode.FIRE) ? EditMode.NONE : EditMode.FIRE;
            updateModeStatus();
            refreshMapOnly();
        });

        btnReset.setOnAction(e -> {
            stopAnimation();
            simulationManager.reset();
            map = simulationManager.getWorkingMap();
            currentMode = EditMode.NONE;
            drawMap(null, null, null);

            distanceLabel.setText("Distance: -");
            exploredLabel.setText("Explored states: -");
            statusLabel.setText("Map reset.");
            pathArea.setText("");
            updateModeButtons();
            setButtonsDisabled(false);
        });

        Label title = new Label("Building Evacuation - BFS");
        title.setFont(Font.font("Arial", 24));
        title.setTextFill(Color.WHITE);

        distanceLabel = createInfoLabel("Distance: -");
        exploredLabel = createInfoLabel("Explored states: -");
        statusLabel = createInfoLabel("Mode: NONE");

        pathArea = new TextArea();
        pathArea.setEditable(false);
        pathArea.setWrapText(true);
        pathArea.setPrefHeight(180);
        pathArea.setStyle(
                "-fx-control-inner-background: #1f2937;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;"
        );

        HBox controls = new HBox(12, btnRun, btnDFS, btnSetStart, btnSetExit, btnBlockMode, btnFireMode, btnReset);
        controls.setAlignment(Pos.CENTER);

        VBox topBox = new VBox(12, title, controls);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(15));
        topBox.setStyle("-fx-background-color: linear-gradient(to right, #0f172a, #1e293b);");

        VBox infoPanel = new VBox(15,
                distanceLabel,
                exploredLabel,
                statusLabel,
                new Label("Optimal path:"),
                pathArea
        );
        infoPanel.setPadding(new Insets(20));
        infoPanel.setPrefWidth(320);
        infoPanel.setStyle("-fx-background-color: #111827; -fx-border-color: #374151; -fx-border-width: 0 0 0 1;");
        ((Label) infoPanel.getChildren().get(3)).setTextFill(Color.WHITE);
        ((Label) infoPanel.getChildren().get(3)).setFont(Font.font(16));

        StackPane canvasHolder = new StackPane(canvas);
        canvasHolder.setPadding(new Insets(20));
        canvasHolder.setStyle("-fx-background-color: #0b1220;");

        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setCenter(canvasHolder);
        root.setRight(infoPanel);

        Scene scene = new Scene(root, canvas.getWidth() + 360, canvas.getHeight() + 120);
        stage.setTitle("Projet 4 - BFS Evacuation");
        stage.setScene(scene);
        stage.show();

        updateModeButtons();
        drawMap(null, null, null);
    }

    private void handleCanvasClick(MouseEvent event) {
        if (currentMode == EditMode.NONE) {
            return;
        }

        stopAnimation();
        map = simulationManager.getWorkingMap();

        int col = (int) (event.getX() / CELL_SIZE);
        int row = (int) (event.getY() / CELL_SIZE);

        if (!map.isInside(row, col)) {
            return;
        }

        char cell = map.getCell(row, col);

        if (currentMode == EditMode.SET_START) {
            if (cell == BuildingMap.WALL) {
                statusLabel.setText("Cannot place start on a wall.");
                return;
            }
            // Remove old start if it exists
            Node oldStart = map.findStart();
            if (oldStart != null) {
                map.setCell(oldStart.getRow(), oldStart.getCol(), BuildingMap.FREE);
            }
            map.setCell(row, col, BuildingMap.START);
            statusLabel.setText("Start (S) placed at (" + row + ", " + col + ")");
            currentMode = EditMode.NONE;
        } else if (currentMode == EditMode.SET_EXIT) {
            if (cell == BuildingMap.WALL) {
                statusLabel.setText("Cannot place exit on a wall.");
                return;
            }
            // Delete exit if clicking on existing exit
            if (cell == BuildingMap.EXIT) {
                map.setCell(row, col, BuildingMap.FREE);
                statusLabel.setText("Exit (E) removed from (" + row + ", " + col + ")");
            } else if (cell != BuildingMap.START) {
                map.setCell(row, col, BuildingMap.EXIT);
                statusLabel.setText("Exit (E) placed at (" + row + ", " + col + ")");
            } else {
                statusLabel.setText("Cannot place exit on start position.");
                return;
            }
        } else if (currentMode == EditMode.BLOCK) {
            if (cell == BuildingMap.START || cell == BuildingMap.EXIT || cell == BuildingMap.WALL) {
                statusLabel.setText("You cannot modify S, E, or wall cells.");
                return;
            }
            if (cell == BuildingMap.FREE) {
                simulationManager.applyClosedDoor(row, col);
                statusLabel.setText("Blocked cell at (" + row + ", " + col + ")");
            } else if (cell == BuildingMap.BLOCKED) {
                map.unblockCell(row, col);
                statusLabel.setText("Unblocked cell at (" + row + ", " + col + ")");
            } else if (cell == BuildingMap.FIRE) {
                statusLabel.setText("Fire cell cannot become blocked.");
            }
        } else if (currentMode == EditMode.FIRE) {
            if (cell == BuildingMap.START || cell == BuildingMap.EXIT || cell == BuildingMap.WALL) {
                statusLabel.setText("Cannot place fire on S, E, or wall cells.");
                return;
            }
            if (cell == BuildingMap.FREE || cell == BuildingMap.BLOCKED) {
                simulationManager.igniteInitialFire(row, col);
                simulationManager.spreadFireStep();
                statusLabel.setText("Fire placed at (" + row + ", " + col + ")");
            } else if (cell == BuildingMap.FIRE) {
                statusLabel.setText("This cell already contains fire.");
            }
        }

        distanceLabel.setText("Distance: -");
        exploredLabel.setText("Explored states: -");
        pathArea.setText("");
        refreshMapOnly();
    }

    private void runAndAnimateBFS() {
        stopAnimation();
        map = simulationManager.getWorkingMap();

        BFSExplorer.SearchResult result = simulationManager.runBFS();
        animateExploration(result.getExploredOrder(), result.getPath(), result);
    }

    private void runAndAnimateDFS() {
        stopAnimation();
        map = simulationManager.getWorkingMap();

        DFSExplorer.SearchResult result = simulationManager.runDFS();
        animateExploration(result.getExploredOrder(), result.getPath(), result);
    }

    private void animateExploration(List<Node> explored, List<Node> path, Object result) {
        setButtonsDisabled(true);
        btnReset.setDisable(false);

        currentTimeline = new Timeline();

        for (int i = 0; i < explored.size(); i++) {
            final int index = i;
            currentTimeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(70L * (i + 1)), e -> {
                        List<Node> currentExplored = explored.subList(0, index + 1);
                        List<Node> frontier = calculateFrontier(currentExplored);
                        drawMap(currentExplored, frontier, null);
                    })
            );
        }

        currentTimeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(70L * explored.size() + 250), e -> {
                    drawMap(explored, new ArrayList<>(), path);

                    // Cast to check if it's a valid search result
                    if (result instanceof BFSExplorer.SearchResult) {
                        BFSExplorer.SearchResult bfsResult = (BFSExplorer.SearchResult) result;
                        if (bfsResult.isFound()) {
                            distanceLabel.setText("Distance: " + bfsResult.getDistance());
                            exploredLabel.setText("Explored states: " + bfsResult.getExploredStates());
                            statusLabel.setText("Exit found at " + bfsResult.getReachedExit());
                            pathArea.setText(formatPath(path));
                        } else {
                            distanceLabel.setText("Distance: impossible");
                            exploredLabel.setText("Explored states: " + bfsResult.getExploredStates());
                            statusLabel.setText("No reachable exit.");
                            pathArea.setText("No path found.");
                        }
                    } else if (result instanceof DFSExplorer.SearchResult) {
                        DFSExplorer.SearchResult dfsResult = (DFSExplorer.SearchResult) result;
                        if (dfsResult.isFound()) {
                            distanceLabel.setText("Distance: " + dfsResult.getDistance());
                            exploredLabel.setText("Explored states: " + dfsResult.getExploredStates());
                            statusLabel.setText("Exit found at " + dfsResult.getReachedExit());
                            pathArea.setText(formatPath(path));
                        } else {
                            distanceLabel.setText("Distance: impossible");
                            exploredLabel.setText("Explored states: " + dfsResult.getExploredStates());
                            statusLabel.setText("No reachable exit.");
                            pathArea.setText("No path found.");
                        }
                    }

                    setButtonsDisabled(false);
                    updateModeButtons();
                })
        );

        currentTimeline.play();
    }

    private List<Node> calculateFrontier(List<Node> explored) {
        Set<Node> frontier = new HashSet<>();
        Set<Node> exploredSet = new HashSet<>(explored);

        for (Node node : explored) {
            for (Node neighbor : map.getNeighbors(node)) {
                if (!exploredSet.contains(neighbor) && !frontier.contains(neighbor)) {
                    frontier.add(neighbor);
                }
            }
        }

        return new ArrayList<>(frontier);
    }

    private void stopAnimation() {
        if (currentTimeline != null) {
            currentTimeline.stop();
            currentTimeline = null;
        }
    }

    private void refreshMapOnly() {
        map = simulationManager.getWorkingMap();
        drawMap(null, null, null);
        updateModeButtons();
    }

    private void setButtonsDisabled(boolean disabled) {
        btnRun.setDisable(disabled);
        btnDFS.setDisable(disabled);
        btnSetStart.setDisable(disabled);
        btnSetExit.setDisable(disabled);
        btnBlockMode.setDisable(disabled);
        btnFireMode.setDisable(disabled);
        btnReset.setDisable(false);
    }

    private void updateModeStatus() {
        statusLabel.setText("Mode: " + currentMode);
        updateModeButtons();
    }

    private void updateModeButtons() {
        styleButton(btnRun, "#2ecc71");
        styleButton(btnDFS, "#8b5cf6");
        styleButton(btnReset, "#3498db");

        if (currentMode == EditMode.SET_START) {
            styleButton(btnSetStart, "#0891b2");
            styleButton(btnSetExit, "#ec4899");
            styleButton(btnBlockMode, "#f39c12");
            styleButton(btnFireMode, "#e74c3c");
        } else if (currentMode == EditMode.SET_EXIT) {
            styleButton(btnSetStart, "#06b6d4");
            styleButton(btnSetExit, "#be185d");
            styleButton(btnBlockMode, "#f39c12");
            styleButton(btnFireMode, "#e74c3c");
        } else if (currentMode == EditMode.BLOCK) {
            styleButton(btnSetStart, "#06b6d4");
            styleButton(btnSetExit, "#ec4899");
            styleButton(btnBlockMode, "#d97706");
            styleButton(btnFireMode, "#e74c3c");
        } else if (currentMode == EditMode.FIRE) {
            styleButton(btnSetStart, "#06b6d4");
            styleButton(btnSetExit, "#ec4899");
            styleButton(btnBlockMode, "#f39c12");
            styleButton(btnFireMode, "#b91c1c");
        } else {
            styleButton(btnSetStart, "#06b6d4");
            styleButton(btnSetExit, "#ec4899");
            styleButton(btnBlockMode, "#f39c12");
            styleButton(btnFireMode, "#e74c3c");
        }
    }

    private Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Arial", 16));
        return label;
    }

    private void styleButton(Button button, String color) {
        button.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 10 18 10 18;"
        );
    }

    private String formatPath(List<Node> path) {
        if (path == null || path.isEmpty()) {
            return "No path.";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i));
            if (i < path.size() - 1) {
                sb.append(" -> ");
            }
        }
        return sb.toString();
    }

    private void drawMap(List<Node> explored, List<Node> frontier, List<Node> path) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw base grid
        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {
                char cell = map.getCell(row, col);

                gc.setFill(baseColor(cell));
                gc.fillRoundRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE, 8, 8);

                gc.setStroke(Color.web("#1f2937"));
                gc.strokeRoundRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE, 8, 8);
            }
        }

        // Draw frontier in red (cells waiting to be explored)
        if (frontier != null) {
            for (Node node : frontier) {
                char cell = map.getCell(node.getRow(), node.getCol());
                if (cell == BuildingMap.FREE) {
                    gc.setFill(Color.web("#ef4444"));
                    gc.fillRoundRect(node.getCol() * CELL_SIZE, node.getRow() * CELL_SIZE, CELL_SIZE, CELL_SIZE, 8, 8);
                    gc.setStroke(Color.web("#1f2937"));
                    gc.strokeRoundRect(node.getCol() * CELL_SIZE, node.getRow() * CELL_SIZE, CELL_SIZE, CELL_SIZE, 8, 8);
                }
            }
        }

        // Draw explored in blue (cells already visited)
        if (explored != null) {
            for (Node node : explored) {
                char cell = map.getCell(node.getRow(), node.getCol());
                if (cell == BuildingMap.FREE) {
                    gc.setFill(Color.web("#3b82f6"));
                    gc.fillRoundRect(node.getCol() * CELL_SIZE, node.getRow() * CELL_SIZE, CELL_SIZE, CELL_SIZE, 8, 8);
                    gc.setStroke(Color.web("#1f2937"));
                    gc.strokeRoundRect(node.getCol() * CELL_SIZE, node.getRow() * CELL_SIZE, CELL_SIZE, CELL_SIZE, 8, 8);
                }
            }
        }

        // Draw path in yellow
        if (path != null) {
            for (Node node : path) {
                char cell = map.getCell(node.getRow(), node.getCol());
                if (cell == BuildingMap.FREE) {
                    gc.setFill(Color.web("#facc15"));
                    gc.fillRoundRect(node.getCol() * CELL_SIZE, node.getRow() * CELL_SIZE, CELL_SIZE, CELL_SIZE, 8, 8);
                    gc.setStroke(Color.web("#1f2937"));
                    gc.strokeRoundRect(node.getCol() * CELL_SIZE, node.getRow() * CELL_SIZE, CELL_SIZE, CELL_SIZE, 8, 8);
                }
            }
        }

        // Draw labels (S and E)
        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {
                char cell = map.getCell(row, col);

                if (cell == BuildingMap.START || cell == BuildingMap.EXIT) {
                    gc.setFill(Color.WHITE);
                    gc.setFont(Font.font("Arial", 16));
                    gc.fillText(String.valueOf(cell),
                            col * CELL_SIZE + CELL_SIZE / 2.7,
                            row * CELL_SIZE + CELL_SIZE / 1.6);
                }
            }
        }
    }

    private Color baseColor(char cell) {
        switch (cell) {
            case BuildingMap.WALL:
                return Color.web("#111827");
            case BuildingMap.START:
                return Color.web("#22c55e");
            case BuildingMap.EXIT:
                return Color.web("#ef4444");
            case BuildingMap.BLOCKED:
                return Color.web("#6b7280");
            case BuildingMap.FIRE:
                return Color.web("#f97316");
            case BuildingMap.FREE:
            default:
                return Color.web("#e5e7eb");
        }
    }
}
package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Cell;
import org.apache.commons.lang3.RandomUtils;
import util.BackgroundUtil;

import java.util.Objects;
import java.util.stream.Stream;

import static constant.GameOfLifeConstants.CELL_SIZE;
import static constant.GameOfLifeConstants.COLUMNS_COUNT;
import static constant.GameOfLifeConstants.DEFAULT_DELAY_BETWEEN_GENERATIONS;
import static constant.GameOfLifeConstants.ROWS_COUNT;
import static javafx.scene.input.MouseEvent.MOUSE_CLICKED;

public class GameOfLifeApplication extends Application {

  private Cell[][] cells; // all cells of the simulation (including their state and their corresponding Pane on the visible grid)
  private int generation = 0;
  private int population = 0;

  private Text generationText;
  private Text populationText;
  private GridPane gridPane;

  public static void main(String[] args) {
    launch();
  }

  @Override
  public void start(Stage primaryStage) {
    initCells();

    Thread simulationThread = initThreadForSimulation();

    VBox infoArea = initInfoArea();
    gridPane = initGrid(simulationThread);
    ToolBar toolbar = initToolbar(simulationThread);

    Scene scene = new Scene(new VBox(toolbar, gridPane, infoArea), 1000, 600);
    primaryStage.setTitle("Game of Life");
    primaryStage.setScene(scene);
    primaryStage.setResizable(false);

    primaryStage.show();
  }

  /**
   * Generate all cells of the simulation
   */
  private void initCells() {
    cells = new Cell[ROWS_COUNT][COLUMNS_COUNT];
    for (int i = 0; i < cells.length; i++) {
      for (int j = 0; j < cells[i].length; j++) {
        cells[i][j] = new Cell();
      }
    }
  }

  /**
   * Randomly choose some cells and set them alive!
   */
  private void generateRandomAliveCells() {
    population = 0;

    for (int i = 0; i < cells.length; i++) {
      for (int j = 0; j < cells[i].length; j++) {
        Cell cell = cells[i][j];
        cell.setAlive(false);

        if (RandomUtils.nextInt(0, 100) >= 90) { // cell is alive with probability of 10%
          cell.setAlive(true);
          population++;
        }

        cell.updatePane();
      }
    }
  }

  private VBox initInfoArea() {
    VBox infoArea = new VBox();
    generationText = new Text("Generation " + generation);
    populationText = new Text("Population : " + population);
    infoArea.getChildren().add(generationText);
    infoArea.getChildren().add(populationText);
    return infoArea;
  }

  private GridPane initGrid(Thread simulationThread) {
    gridPane = new GridPane();

    for (int i = 0; i < cells.length; i++) {
      for (int j = 0; j < cells[i].length; j++) {
        Cell cell = cells[i][j];
        Pane pane = cell.getPane();
        pane.setBackground(BackgroundUtil.getBackground(cell.isAlive()));
        pane.setMinSize(CELL_SIZE, CELL_SIZE);

        pane.addEventHandler(MOUSE_CLICKED, event -> {
          if (!simulationThread.isAlive()) {
            cell.toggleCellState();
          }
        });

        gridPane.add(pane, j, i);
      }
    }

    return gridPane;
  }

  private ToolBar initToolbar(Thread simulationThread) {
    ToolBar toolBar = new ToolBar();

    Button startButton = new Button("Start");
    startButton.setOnAction(event -> simulationThread.start());

    Button generateRandomLiveCellsButton = new Button("Generate random live cells");
    generateRandomLiveCellsButton.setOnAction(event -> generateRandomAliveCells());

    toolBar.getItems().add(startButton);
    toolBar.getItems().add(generateRandomLiveCellsButton);

    return toolBar;
  }

  /**
   *  Starts another thread which simulates one round (generation) of GOL and updates the view.
   */
  private Thread initThreadForSimulation() {
    Thread thread = new Thread(() -> {
      Runnable updater = () -> {
        generation++;
        simulateNextGeneration();
        generationText.setText("Generation " + generation);
        populationText.setText("Population : " + population);
      };

      while (true) {
        sleep(); // make a little pause before simulating next generation
        Platform.runLater(updater); // UI update is run on the Application thread
      }
    });

    thread.setDaemon(true); // don't let thread prevent JVM shutdown

    return thread;
  }

  /**
   * Compute the state (live/dead) of all cells for the next generation
   */
  private void simulateNextGeneration() {
    // do the simulation
    for (int i = 0; i < cells.length; i++) {
      for (int j = 0; j < cells[i].length; j++) {
        Cell cell = cells[i][j];

        long neighboursCount = computeLiveNeighboursCount(i, j);
        if (cell.isAlive() && (neighboursCount < 2 || neighboursCount > 3)) {
          cell.setMarkedAsDead(true);
        } else if (cell.isDead() && neighboursCount == 3) {
          cell.setMarkedAsAlive(true);
        }

      }
    }

    // execute the simulation (apply the new status to the cells that has been flagged)
    for (int i = 0; i < cells.length; i++) {
      for (int j = 0; j < cells[i].length; j++) {
        Cell cell = cells[i][j];

        if (cell.isMarkedAsDead()) {
          cell.die();
          cell.setMarkedAsDead(false);
          population--;
        }

        if (cell.isMarkedAsAlive()) {
          cell.becomeAlive();
          cell.setMarkedAsAlive(false);
          population++;
        }
      }
    }
  }

  private long computeLiveNeighboursCount(int i, int j) {
    int lastRow = cells.length - 1;
    int lastColumn = cells[i].length - 1;

    Cell topLeftCell = (i != 0 && j != 0) ? cells[i - 1][j - 1] : null;
    Cell topCell = (i != 0) ? cells[i - 1][j] : null;
    Cell topRightCell = (i != 0 && j != lastColumn) ? cells[i - 1][j + 1] : null;

    Cell leftCell = (j != 0) ? cells[i][j - 1] : null;
    Cell rightCell = (j != lastColumn) ? cells[i][j + 1] : null;

    Cell bottomLeftCell = (i != lastRow && j != 0) ? cells[i + 1][j - 1] : null;
    Cell bottomCell = (i != lastRow) ? cells[i + 1][j] : null;
    Cell bottomRightCell = (i != lastRow && j != lastColumn) ? cells[i + 1][j + 1] : null;

    return Stream.of(topLeftCell, topCell, topRightCell, leftCell, rightCell, bottomLeftCell, bottomCell, bottomRightCell)
      .filter(Objects::nonNull)
      .filter(Cell::isAlive)
      .count();
  }

  private void sleep() {
    try {
      Thread.sleep(DEFAULT_DELAY_BETWEEN_GENERATIONS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}

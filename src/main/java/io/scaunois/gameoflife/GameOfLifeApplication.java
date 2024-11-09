package io.scaunois.gameoflife;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import io.scaunois.gameoflife.model.Cell;
import org.apache.commons.lang3.RandomUtils;
import io.scaunois.gameoflife.util.StyleUtil;

import java.util.Objects;
import java.util.stream.Stream;

import static io.scaunois.gameoflife.constant.GameOfLifeConstants.CELL_SIZE;
import static io.scaunois.gameoflife.constant.GameOfLifeConstants.COLUMNS_COUNT;
import static io.scaunois.gameoflife.constant.GameOfLifeConstants.DEFAULT_DELAY_BETWEEN_GENERATIONS;
import static io.scaunois.gameoflife.constant.GameOfLifeConstants.ROWS_COUNT;
import static javafx.scene.input.MouseEvent.MOUSE_CLICKED;

public class GameOfLifeApplication extends Application {

  // data
  private Cell[][] cells; // all cells of the simulation (including their state and their corresponding Pane on the visible grid)
  private SimpleLongProperty generation = new SimpleLongProperty(0);
  private SimpleLongProperty population = new SimpleLongProperty(0);

  // layout
  private Scene scene;
  private VBox mainContainer;
  private ToolBar toolbar;
  private GridPane gridPane;
  private VBox infoArea;
  private Text generationText;
  private Text populationText;

  // controls (toolbar)
  private Button startButton;
  private Button stopButton;
  private Button resetButton;
  private Button randomGenerationButton;

  private Thread simulationThread;

  public static void main(String[] args) {
    launch();
  }

  @Override
  public void start(Stage primaryStage) {

    // init layout

    initToolbar();
    initEmptyGrid();
    initInfoArea();

    mainContainer = new VBox(toolbar, gridPane, infoArea);
    VBox.setMargin(infoArea, new Insets(10, 0, 0, 0));
    scene = new Scene(mainContainer, 1000, 600);
    primaryStage.setTitle("Game of Life");
    primaryStage.setScene(scene);
    primaryStage.setResizable(false);

    primaryStage.show();
  }

  private void initToolbar() {
    toolbar = new ToolBar();

    startButton = new Button("Start");
    stopButton = new Button("Stop");
    resetButton = new Button("Reset");
    randomGenerationButton = new Button("Generate random population");

    stopButton.setDisable(true);

    startButton.setOnAction(event -> {
      startButton.setDisable(true);
      stopButton.setDisable(false);
      simulationThread = initSimulationThread();
      simulationThread.start();
    });

    stopButton.setOnAction(event -> {
      simulationThread.stop();
      startButton.setDisable(false);
      stopButton.setDisable(true);
    });

    resetButton.setOnAction(event -> {
      if (simulationThread != null && simulationThread.isAlive())
        simulationThread.stop();
      startButton.setDisable(false);
      stopButton.setDisable(true);
      initEmptyGrid();
      mainContainer.getChildren().set(1, gridPane);
      generation.set(0);
      population.set(0);
    });

    randomGenerationButton.setOnAction(event -> generateRandomAliveCells());

    var toolbarButtons = toolbar.getItems();
    toolbarButtons.add(startButton);
    toolbarButtons.add(stopButton);
    toolbarButtons.add(resetButton);
    toolbarButtons.add(randomGenerationButton);
  }

  private Thread initSimulationThread() {
    var thread = new Thread(() -> {

      Runnable updater = () -> {
        generation.set(generation.get() + 1);
        simulateNextGeneration();
      };

      while (true) {
        sleep(); // make a little pause before simulating next generation
        Platform.runLater(updater); // UI update is run on the Application thread
      }
    });

    thread.setDaemon(true);

    return thread;
  }

  /**
   * Randomly choose some cells and set them alive!
   */
  private void generateRandomAliveCells() {
    population.set(0);

    for (int i = 0; i < cells.length; i++) {
      for (int j = 0; j < cells[i].length; j++) {
        Cell cell = cells[i][j];
        cell.setAlive(false);

        if (RandomUtils.nextInt(0, 100) >= 85) { // cell is alive with probability of 15%
          cell.setAlive(true);
          population.set(population.get() + 1);
        }

        cell.updatePane();
      }
    }
  }

  private void initEmptyGrid() {
    initCells();

    gridPane = new GridPane();
    gridPane.setStyle("-fx-border-color: black; -fx-border-width: 2px; -fx-padding: 4px;");

    for (int i = 0; i < cells.length; i++) {
      for (int j = 0; j < cells[i].length; j++) {
        Cell cell = cells[i][j];
        Pane pane = cell.getPane();
        pane.setMinSize(CELL_SIZE, CELL_SIZE);

        pane.addEventHandler(MOUSE_CLICKED, event -> {
          if (simulationThread == null || !simulationThread.isAlive()) {
            cell.toggleCellState();
            population.set(population.get() + (cell.isAlive() ? +1 : -1));
          }
        });

        gridPane.add(pane, j, i);
      }
    }
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

  private void initInfoArea() {
    infoArea = new VBox();

    generationText = new Text();
    generationText.setFont(Font.font(16));
    generationText.textProperty().bind(generation.asString("Generation %d"));

    populationText = new Text();
    populationText.setFont(Font.font(16));
    populationText.textProperty().bind(population.asString("Population: %d"));

    infoArea.getChildren().add(generationText);
    infoArea.getChildren().add(populationText);
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
          population.set(population.get() - 1);
        }

        if (cell.isMarkedAsAlive()) {
          cell.becomeAlive();
          cell.setMarkedAsAlive(false);
          population.set(population.get() + 1);
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

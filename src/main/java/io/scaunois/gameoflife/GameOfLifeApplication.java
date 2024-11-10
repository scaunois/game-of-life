package io.scaunois.gameoflife;

import io.scaunois.gameoflife.constant.GeneratedPopulationSize;
import io.scaunois.gameoflife.model.Cell;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static io.scaunois.gameoflife.constant.GameOfLifeConstants.CELL_SIZE;
import static io.scaunois.gameoflife.constant.GameOfLifeConstants.COLUMNS_COUNT;
import static io.scaunois.gameoflife.constant.GameOfLifeConstants.DEFAULT_DELAY_BETWEEN_GENERATIONS;
import static io.scaunois.gameoflife.constant.GameOfLifeConstants.DEFAULT_RANDOM_POPULATION_DENSITY;
import static io.scaunois.gameoflife.constant.GameOfLifeConstants.ROWS_COUNT;
import static io.scaunois.gameoflife.constant.GeneratedPopulationSize.SMALL;
import static io.scaunois.gameoflife.util.ToolbarUtil.defaultSpacer;
import static javafx.scene.input.MouseEvent.MOUSE_CLICKED;

public class GameOfLifeApplication extends Application {

  // data
  private Cell[][] cells; // all cells of the simulation (including their state and their corresponding Pane on the visible grid)
  private final SimpleLongProperty generation = new SimpleLongProperty(0);
  private final SimpleLongProperty population = new SimpleLongProperty(0);

  // layout
  private VBox mainContainer;
  private GridPane gridPane;

  private Thread simulationThread;

  public static void main(String[] args) {
    launch();
  }

  @Override
  public void start(Stage primaryStage) {

    // init layout

    var toolbars = initToolbars();
    initEmptyGrid();
    var infoArea = initInfoArea();

    mainContainer = new VBox(toolbars.get(0), toolbars.get(1), gridPane, infoArea);
    VBox.setMargin(infoArea, new Insets(10, 0, 0, 0));
    primaryStage.setTitle("Game of Life");
    primaryStage.setScene(new Scene(mainContainer, 1000, 600));
    primaryStage.setResizable(false);

    primaryStage.show();
  }

  private List<ToolBar> initToolbars() {

    // toolbar 1

    var toolbar1 = new ToolBar();

    var startButton = new Button("Start");
    var stopButton = new Button("Stop");
    var resetButton = new Button("Reset");

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
      mainContainer.getChildren().set(2, gridPane);
      generation.set(0);
      population.set(0);
    });

    var toolbar1Items = toolbar1.getItems();
    toolbar1Items.add(startButton);
    toolbar1Items.add(stopButton);
    toolbar1Items.add(resetButton);

    // toolbar 2

    var toolbar2 = new ToolBar();

    var smallPopulationRadioButton = new RadioButton("S");
    smallPopulationRadioButton.setUserData(SMALL);
    var mediumPopulationRadioButton = new RadioButton("M");
    mediumPopulationRadioButton.setUserData(GeneratedPopulationSize.MEDIUM);
    var largePopulationRadioButton = new RadioButton("L");
    largePopulationRadioButton.setUserData(GeneratedPopulationSize.LARGE);

    var radioButtonsGroup = new ToggleGroup();
    smallPopulationRadioButton.setToggleGroup(radioButtonsGroup);
    mediumPopulationRadioButton.setToggleGroup(radioButtonsGroup);
    largePopulationRadioButton.setToggleGroup(radioButtonsGroup);

    AtomicReference<GeneratedPopulationSize> generatedPopulationSize = new AtomicReference<>();
    radioButtonsGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
      if (newToggle != null) {
        generatedPopulationSize.set((GeneratedPopulationSize) newToggle.getUserData());
      }
    });
    mediumPopulationRadioButton.setSelected(true); // default generated population size

    IntegerProperty generatedPopulationDensity = new SimpleIntegerProperty(DEFAULT_RANDOM_POPULATION_DENSITY); // default generated population density
    var populationDensitySpinner = new Spinner<Integer>(0, 100, generatedPopulationDensity.get());
    populationDensitySpinner.setEditable(true);
    populationDensitySpinner.setPrefWidth(60);
    populationDensitySpinner.valueProperty().addListener((obs, oldValue, newValue) -> generatedPopulationDensity.set(newValue));

    var randomGenerationButton = new Button("Generate population");
    randomGenerationButton.setOnAction(event -> generateRandomAliveCells(generatedPopulationSize.get(), generatedPopulationDensity.get()));

    var toolbar2Items = toolbar2.getItems();
    toolbar2Items.add(new Label("Pop. size:"));
    toolbar2Items.add(smallPopulationRadioButton);
    toolbar2Items.add(mediumPopulationRadioButton);
    toolbar2Items.add(largePopulationRadioButton);
    toolbar2Items.add(defaultSpacer());
    toolbar2Items.add(new Label("Pop. density (%):"));
    toolbar2Items.add(populationDensitySpinner);
    toolbar2Items.add(defaultSpacer());
    toolbar2Items.add(randomGenerationButton);

    return List.of(toolbar1, toolbar2);
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
  private void generateRandomAliveCells(GeneratedPopulationSize generatedPopulationSize, int generatedPopulationDensity) {
    population.set(0);

    int MIN_I = switch (generatedPopulationSize) {
      case SMALL -> ROWS_COUNT / 2 - 3;
      case MEDIUM -> ROWS_COUNT / 2 - 6;
      case LARGE -> 0;
    };

    int MAX_I = switch (generatedPopulationSize) {
      case SMALL -> ROWS_COUNT / 2 + 3;
      case MEDIUM -> ROWS_COUNT / 2 + 6;
      case LARGE -> ROWS_COUNT;
    };

    int MIN_J = switch (generatedPopulationSize) {
      case SMALL -> COLUMNS_COUNT / 2 - 3;
      case MEDIUM -> COLUMNS_COUNT / 2 - 6;
      case LARGE -> 0;
    };

    int MAX_J = switch (generatedPopulationSize) {
      case SMALL -> COLUMNS_COUNT / 2 + 3;
      case MEDIUM -> COLUMNS_COUNT / 2 + 6;
      case LARGE -> COLUMNS_COUNT;
    };

    for (int i = MIN_I; i < MAX_I; i++) {
      for (int j = MIN_J; j < MAX_J; j++) {
        Cell cell = cells[i][j];
        cell.setAlive(false);

        if (RandomUtils.nextInt(0, 100) >= 100 - generatedPopulationDensity) {
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

  private VBox initInfoArea() {
    var infoArea = new VBox();

    var generationText = new Text();
    generationText.setFont(Font.font(16));
    generationText.textProperty().bind(generation.asString("Generation %d"));

    var populationText = new Text();
    populationText.setFont(Font.font(16));
    populationText.textProperty().bind(population.asString("Population: %d"));

    infoArea.getChildren().add(generationText);
    infoArea.getChildren().add(populationText);

    return infoArea;
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

package io.scaunois.gameoflife.constant;

public interface GameOfLifeConstants {

  int ROWS_COUNT = 40 + 30;
  int COLUMNS_COUNT = 96 + 60;
  int CELL_SIZE = 10;
  int DEFAULT_RANDOM_POPULATION_DENSITY = 50; // default probability (in %) to generate a living cell when using random generation
  int DEFAULT_DELAY_BETWEEN_GENERATIONS = 200; // delay in ms before next generation is simulated

}

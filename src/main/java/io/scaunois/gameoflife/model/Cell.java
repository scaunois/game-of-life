package io.scaunois.gameoflife.model;

import io.scaunois.gameoflife.constant.BackgroundPaintColor;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import lombok.Getter;
import lombok.Setter;
import io.scaunois.gameoflife.util.StyleUtil;

@Getter
@Setter
public class Cell {

  private boolean alive;
  private boolean markedAsDead;
  private boolean markedAsAlive;
  private Pane pane;

  public Cell() {
    alive = false;
    pane = new Pane();
    pane.setBorder(StyleUtil.getCellBorder());
    pane.setBackground(StyleUtil.getDefaultBackground());
  }

  public boolean isDead() {
    return !alive;
  }

  public void die() {
    alive = false;
    updatePane();
  }

  public void becomeAlive() {
    alive = true;
    updatePane();
  }

  public void toggleCellState() {
    if (alive) {
      die();
    } else {
      becomeAlive();
    }
  }

  public void updatePane() {
    var cellColor = alive ? BackgroundPaintColor.ALIVE_CELL_COLOR : BackgroundPaintColor.DEAD_CELL_COLOR;
    BackgroundFill backgroundFill = new BackgroundFill(cellColor, null, null);
    pane.setBackground(new Background(backgroundFill));
  }
}

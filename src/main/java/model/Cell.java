package model;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import lombok.Getter;
import lombok.Setter;
import util.BackgroundUtil;

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
    pane.setBorder(BackgroundUtil.getCellBorder());
    pane.setBackground(BackgroundUtil.getDefaultBackground());
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
    Paint cellColor = alive ? Color.BLACK : Color.WHITE;
    BackgroundFill backgroundFill = new BackgroundFill(cellColor, null, null);
    pane.setBackground(new Background(backgroundFill));
  }
}

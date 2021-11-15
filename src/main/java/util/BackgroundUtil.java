package util;

import constant.BackgroundPaintColor;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Paint;

public class BackgroundUtil {

  public static Border getCellBorder() {
    return new Border(new BorderStroke(BackgroundPaintColor.CELL_BORDER_COLOR, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
  }

  public static Background getDefaultBackground() {
    BackgroundFill backgroundFill = new BackgroundFill(BackgroundPaintColor.DEAD_CELL_COLOR, null, null);
    return new Background(backgroundFill);
  }

  public static Background getBackground(boolean isAlive) {
    Paint color = isAlive ? BackgroundPaintColor.ALIVE_CELL_COLOR : BackgroundPaintColor.DEAD_CELL_COLOR;
    BackgroundFill backgroundFill = new BackgroundFill(color, null, null);
    return new Background(backgroundFill);
  }

  private BackgroundUtil() {
    // Constructor to prevent class instanciation
  }
}

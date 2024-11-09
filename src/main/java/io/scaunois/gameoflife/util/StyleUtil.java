package io.scaunois.gameoflife.util;

import io.scaunois.gameoflife.constant.BackgroundPaintColor;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public interface StyleUtil {

  static Border getCellBorder() {
    return new Border(new BorderStroke(BackgroundPaintColor.CELL_BORDER_COLOR, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
  }

  static Background getDefaultBackground() {
    var backgroundFill = new BackgroundFill(BackgroundPaintColor.DEAD_CELL_COLOR, null, null);
    return new Background(backgroundFill);
  }
}

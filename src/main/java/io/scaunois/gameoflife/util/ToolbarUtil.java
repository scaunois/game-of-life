package io.scaunois.gameoflife.util;

import javafx.scene.layout.Region;

public interface ToolbarUtil {

    static Region defaultSpacer() {
        var spacer = new Region();
        spacer.setPrefWidth(16);
        return spacer;
    }
}

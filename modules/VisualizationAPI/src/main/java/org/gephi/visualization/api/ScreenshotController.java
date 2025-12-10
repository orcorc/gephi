package org.gephi.visualization.api;

import java.io.File;

public interface ScreenshotController {

    void takeScreenshot();

    void setScaleFactor(int scaleFactor);

    void setTransparentBackground(boolean transparentBackground);

    void setAutoSave(boolean autoSave);

    void setDefaultDirectory(File directory);
}

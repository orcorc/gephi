package org.gephi.visualization.api;

import java.io.File;

public interface ScreenshotController {

    void takeScreenshot();

    void setAntiAliasing(int antiAliasing);

    void setWidth(int width);

    void setHeight(int height);

    void setTransparentBackground(boolean transparentBackground);

    void setAutoSave(boolean autoSave);

    void setDefaultDirectory(File directory);
}

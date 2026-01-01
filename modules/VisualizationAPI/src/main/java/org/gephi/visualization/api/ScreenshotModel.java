package org.gephi.visualization.api;


public interface ScreenshotModel {

    VisualisationModel getVisualisationModel();

    int getScaleFactor();

    boolean isTransparentBackground();

    boolean isAutoSave();

    String getDefaultDirectory();
}

package org.gephi.visualization.screenshot;

import java.io.File;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.api.ScreenshotModel;
import org.gephi.visualization.api.VisualisationModel;
import org.gephi.visualization.apiimpl.VizConfig;
import org.openide.util.NbPreferences;

public class ScreenshotModelImpl implements ScreenshotModel {

    protected static final String LAST_PATH = "ScreenshotMaker_Last_Path";
    protected static final String LAST_PATH_DEFAULT = "ScreenshotMaker_Last_Path_Default";
    // Model
    private final VizModel vizModel;
    // Settings
    private int scaleFactor = 1;
    private boolean transparentBackground = false;
    private boolean autoSave = false;
    private String defaultDirectory;

    public ScreenshotModelImpl(VizModel vizModel, VizConfig config) {
        this.vizModel = vizModel;
        String lastPathDefault = NbPreferences.forModule(ScreenshotControllerImpl.class).get(LAST_PATH_DEFAULT, null);
        defaultDirectory = NbPreferences.forModule(ScreenshotControllerImpl.class).get(LAST_PATH, lastPathDefault);

        // TODO Set default based on VizConfig
    }

    @Override
    public VisualisationModel getVisualisationModel() {
        return vizModel;
    }


    @Override
    public int getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(int scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    @Override
    public boolean isTransparentBackground() {
        return transparentBackground;
    }

    public void setTransparentBackground(boolean transparentBackground) {
        this.transparentBackground = transparentBackground;
    }

    @Override
    public boolean isAutoSave() {
        return autoSave;
    }

    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }

    @Override
    public String getDefaultDirectory() {
        return defaultDirectory;
    }

    public void setDefaultDirectory(File directory) {
        if (directory != null && directory.exists()) {
            defaultDirectory = directory.getAbsolutePath();
            NbPreferences.forModule(ScreenshotControllerImpl.class).put(LAST_PATH, defaultDirectory);
        }
    }
}

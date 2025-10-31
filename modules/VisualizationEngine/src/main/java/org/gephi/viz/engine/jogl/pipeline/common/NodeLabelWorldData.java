package org.gephi.viz.engine.jogl.pipeline.common;

import java.awt.Font;
import org.gephi.viz.engine.spi.WorldData;
import org.gephi.viz.engine.status.GraphRenderingOptions;

public class NodeLabelWorldData implements WorldData {

    private final boolean showNodeLabels;
    private final Font nodeLabelFont;
    private final float nodeLabelScale;
    private final GraphRenderingOptions.LabelColorMode nodeLabelColorMode;
    private final GraphRenderingOptions.LabelSizeMode nodeLabelSizeMode;
    private final boolean hideNonSelectedLabels;
    private final boolean fitNodeLabelsToNodeSize;
    private final float fitNodeLabelsToNodeSizeFactor;
    private final float lightenNonSelectedFactor;
    private final float zoom;

    public NodeLabelWorldData(boolean showNodeLabels,
                              float zoom,
                              Font nodeLabelFont,
                              float nodeLabelScale,
                              GraphRenderingOptions.LabelColorMode nodeLabelColorMode,
                              GraphRenderingOptions.LabelSizeMode nodeLabelSizeMode,
                              boolean hideNonSelectedLabels,
                              boolean fitNodeLabelsToNodeSize,
                              float fitNodeLabelsToNodeSizeFactor,
                              float lightenNonSelectedFactor) {
        this.showNodeLabels = showNodeLabels;
        this.zoom = zoom;
        this.nodeLabelFont = nodeLabelFont;
        this.nodeLabelScale = nodeLabelScale;
        this.nodeLabelColorMode = nodeLabelColorMode;
        this.nodeLabelSizeMode = nodeLabelSizeMode;
        this.hideNonSelectedLabels = hideNonSelectedLabels;
        this.fitNodeLabelsToNodeSize = fitNodeLabelsToNodeSize;
        this.fitNodeLabelsToNodeSizeFactor = fitNodeLabelsToNodeSizeFactor;
        this.lightenNonSelectedFactor = lightenNonSelectedFactor;
    }

    public boolean isShowNodeLabels() {
        return showNodeLabels;
    }

    public float getZoom() {
        return zoom;
    }

    public float getLightenNonSelectedFactor() {
        return lightenNonSelectedFactor;
    }

    public Font getNodeLabelFont() {
        return nodeLabelFont;
    }

    public float getNodeLabelScale() {
        return nodeLabelScale;
    }

    public GraphRenderingOptions.LabelColorMode getNodeLabelColorMode() {
        return nodeLabelColorMode;
    }

    public GraphRenderingOptions.LabelSizeMode getNodeLabelSizeMode() {
        return nodeLabelSizeMode;
    }

    public boolean isHideNonSelectedLabels() {
        return hideNonSelectedLabels;
    }

    public boolean isFitNodeLabelsToNodeSize() {
        return fitNodeLabelsToNodeSize;
    }

    public float getFitNodeLabelsToNodeSizeFactor() {
        return fitNodeLabelsToNodeSizeFactor;
    }
}

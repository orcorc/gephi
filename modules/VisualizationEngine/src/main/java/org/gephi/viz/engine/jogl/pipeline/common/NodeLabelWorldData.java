package org.gephi.viz.engine.jogl.pipeline.common;

import java.awt.Font;
import jogamp.text.TextRenderer;
import org.gephi.viz.engine.spi.WorldData;
import org.gephi.viz.engine.status.GraphRenderingOptions;

public class NodeLabelWorldData implements WorldData {

    private final TextRenderer textRenderer;
    private final boolean showNodeLabels;
    private final float nodeScale;
    private final Font nodeLabelFont;
    private final float nodeLabelScale;
    private final GraphRenderingOptions.LabelColorMode nodeLabelColorMode;
    private final GraphRenderingOptions.LabelSizeMode nodeLabelSizeMode;
    private final boolean hideNonSelectedLabels;
    private final boolean fitNodeLabelsToNodeSize;
    private final float fitNodeLabelsToNodeSizeFactor;
    private final float lightenNonSelectedFactor;
    private final float nodeLabelSizeFactor;
    private final float zoom;

    public NodeLabelWorldData(TextRenderer textRenderer,
                              boolean showNodeLabels,
                              float zoom,
                              float nodeScale,
                              Font nodeLabelFont,
                              float nodeLabelScale,
                              GraphRenderingOptions.LabelColorMode nodeLabelColorMode,
                              GraphRenderingOptions.LabelSizeMode nodeLabelSizeMode,
                              float nodeLabelSizeFactor,
                              boolean hideNonSelectedLabels,
                              boolean fitNodeLabelsToNodeSize,
                              float fitNodeLabelsToNodeSizeFactor,
                              float lightenNonSelectedFactor) {
        this.textRenderer = textRenderer;
        this.showNodeLabels = showNodeLabels;
        this.zoom = zoom;
        this.nodeScale = nodeScale;
        this.nodeLabelFont = nodeLabelFont;
        this.nodeLabelScale = nodeLabelScale;
        this.nodeLabelColorMode = nodeLabelColorMode;
        this.nodeLabelSizeMode = nodeLabelSizeMode;
        this.nodeLabelSizeFactor = nodeLabelSizeFactor;
        this.hideNonSelectedLabels = hideNonSelectedLabels;
        this.fitNodeLabelsToNodeSize = fitNodeLabelsToNodeSize;
        this.fitNodeLabelsToNodeSizeFactor = fitNodeLabelsToNodeSizeFactor;
        this.lightenNonSelectedFactor = lightenNonSelectedFactor;
    }

    public TextRenderer getTextRenderer() {
        return textRenderer;
    }

    public boolean isShowNodeLabels() {
        return showNodeLabels;
    }

    public float getNodeScale() {
        return nodeScale;
    }

    public float getZoom() {
        return zoom;
    }

    public float getLightenNonSelectedFactor() {
        return lightenNonSelectedFactor;
    }

    public float getNodeLabelSizeFactor() {
        return nodeLabelSizeFactor;
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

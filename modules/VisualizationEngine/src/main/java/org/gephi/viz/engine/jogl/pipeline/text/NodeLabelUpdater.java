package org.gephi.viz.engine.jogl.pipeline.text;

import java.awt.geom.Rectangle2D;
import org.gephi.graph.api.Node;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.pipeline.PipelineCategory;
import org.gephi.viz.engine.spi.ElementsCallback;
import org.gephi.viz.engine.spi.WorldUpdater;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.util.structure.NodesCallback;

public class NodeLabelUpdater implements WorldUpdater<JOGLRenderingTarget, Node> {

    private final VizEngine engine;
    private final NodeLabelData labelData;

    public NodeLabelUpdater(VizEngine engine, NodeLabelData labelData) {
        this.engine = engine;
        this.labelData = labelData;
    }

    @Override
    public void init(JOGLRenderingTarget target) {
        // Nothing to do
    }

    @Override
    public void dispose(JOGLRenderingTarget target) {
        // Nothing to do here, renderer will handle TextRenderer disposal
    }

    @Override
    public void updateWorld(VizEngineModel model) {
        final GraphRenderingOptions options = model.getRenderingOptions();

        if (!options.isShowNodeLabels()) {
            labelData.clearLabelData();
            return;
        }

        // Ensure we have a text renderer with the right font
        // This doesn't require GL context
        labelData.ensureTextRenderer(options.getNodeLabelFont());

        // Clear previous frame's data
        labelData.clearLabelData();

        // Get nodes and their properties
        final NodesCallback nodesCallback = labelData.getNodesCallback();
        final boolean someSelection = nodesCallback.hasSelection();
        final String[] texts = nodesCallback.getNodesLabelsArray();
        
        if (texts == null || texts.length == 0) {
            return;
        }

        final Node[] nodes = nodesCallback.getNodesArray();
        final int maxIndex = nodesCallback.getMaxIndex();

        // Rendering parameters
        final GraphRenderingOptions.LabelColorMode labelColorMode = options.getNodeLabelColorMode();
        final GraphRenderingOptions.LabelSizeMode labelSizeMode = options.getNodeLabelSizeMode();
        final float lightenNonSelectedFactor = options.getLightenNonSelectedFactor();
        final float nodeLabelScale = options.getNodeLabelScale();
        final float nodeLabelSizeFactor = options.getNodeLabelSizeFactor();
        final float fitNodeLabelsToNodeSizeFactor = options.getNodeLabelFitToNodeSizeFactor();
        final boolean fitToNodeSize = options.isNodeLabelFitToNodeSize();
        final boolean hideNonSelectedLabels = options.isHideNonSelectedNodeLabels();
        final float zoom = options.getZoom();
        final float nodeScale = options.getNodeScale();

        if (hideNonSelectedLabels && !someSelection) {
            return;
        }

        // Pre-compute all label rendering data and create batches
        // This is the heavy lifting that happens WITHOUT GL context
        for (int i = 0; i <= maxIndex; i++) {
            final Node node = nodes[i];

            if (node == null) {
                continue;
            }

            boolean selected = someSelection && nodesCallback.isSelected(i);

            if (hideNonSelectedLabels && !selected) {
                continue;
            }

            final String text = texts[i];
            if (text == null || text.isEmpty()) {
                continue;
            }

            // Size calculation
            final float nodeSizeFactor = fitToNodeSize ? node.size() * fitNodeLabelsToNodeSizeFactor * nodeScale :
                node.getTextProperties().getSize() * nodeLabelSizeFactor;
            float sizeFactor = nodeLabelScale * nodeSizeFactor;
            if (labelSizeMode.equals(GraphRenderingOptions.LabelSizeMode.SCREEN)) {
                sizeFactor /= zoom;
            }

            // Position calculation (using pre-computed bounds)
            final Rectangle2D bounds = labelData.getTextBounds(text);
            if (bounds == null) {
                continue;
            }
            
            final float widthPx = (float) bounds.getWidth();
            final float ascentPx = (float) (-bounds.getY());
            final float heightPx = (float) bounds.getHeight();
            final float descentPx = heightPx - ascentPx;
            final float drawX = node.x() - (widthPx * sizeFactor) * 0.5f;
            final float drawY = node.y() - ((ascentPx - descentPx) * sizeFactor) * 0.5f;
            
            // Store dimensions back to node
            node.getTextProperties().setDimensions(widthPx * sizeFactor, heightPx * sizeFactor);

            // Color calculation
            final int rgba = labelColorMode.equals(GraphRenderingOptions.LabelColorMode.OBJECT) ? node.getRGBA() :
                node.getTextProperties().getRGBA();
            final float r = (rgba >> 16 & 255) / 255.0F;
            final float g = (rgba >> 8 & 255) / 255.0F;
            final float b = (rgba & 255) / 255.0F;
            final float a = ((rgba >> 24) & 0xFF) / 255f;

            final float finalR, finalG, finalB, finalA;
            if (someSelection && !selected) {
                float lightColorFactor = 1 - lightenNonSelectedFactor;
                finalR = r;
                finalG = g;
                finalB = b;
                finalA = lightColorFactor;
            } else {
                finalR = r;
                finalG = g;
                finalB = b;
                finalA = a;
            }

            // Create a prepared batch for this label (no GL context needed)
            // This pre-creates glyphs using Java2D only
            labelData.addBatch(text, drawX, drawY, sizeFactor, finalR, finalG, finalB, finalA);
        }
    }

    @Override
    public ElementsCallback<Node> getElementsCallback() {
        return labelData.getNodesCallback();
    }

    @Override
    public String getCategory() {
        return PipelineCategory.NODE_LABEL;
    }

    @Override
    public int getPreferenceInCategory() {
        return 0;
    }

    @Override
    public String getName() {
        return "Nodes Labels";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}

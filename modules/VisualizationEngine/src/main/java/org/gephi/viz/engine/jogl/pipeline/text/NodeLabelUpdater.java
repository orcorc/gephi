package org.gephi.viz.engine.jogl.pipeline.text;

import org.gephi.graph.api.Node;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.util.gl.capabilities.GLCapabilitiesSummary;
import org.gephi.viz.engine.pipeline.PipelineCategory;
import org.gephi.viz.engine.spi.ElementsCallback;
import org.gephi.viz.engine.spi.WorldUpdater;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.util.gl.OpenGLOptions;
import org.gephi.viz.engine.util.structure.NodesCallback;

public class NodeLabelUpdater implements WorldUpdater<JOGLRenderingTarget, Node> {

    private final VizEngine engine;
    private final NodeLabelData labelData;
    private boolean vaoSupported = false;

    public NodeLabelUpdater(VizEngine engine, NodeLabelData labelData) {
        this.engine = engine;
        this.labelData = labelData;
    }

    @Override
    public void init(JOGLRenderingTarget target) {
        final GLCapabilitiesSummary capabilities = target.getGlCapabilitiesSummary();
        final OpenGLOptions openGLOptions = engine.getOpenGLOptions();
        vaoSupported = capabilities.isVAOSupported(openGLOptions);
    }

    @Override
    public void dispose(JOGLRenderingTarget target) {
        labelData.dispose();
    }

    @Override
    public void updateWorld(VizEngineModel model) {
        final GraphRenderingOptions options = model.getRenderingOptions();

        if (!options.isShowNodeLabels()) {
            labelData.dispose();
            return;
        }

        // Get nodes and their properties
        final NodesCallback nodesCallback = labelData.getNodesCallback();
        final boolean someSelection = nodesCallback.hasSelection();
        final String[] texts = nodesCallback.getNodesLabelsArray();
        
        if (texts == null || texts.length == 0) {
            return;
        }

        // Get nodes array
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

        // No labels to show
        if (hideNonSelectedLabels && !someSelection) {
            return;
        }

        // Ensure label batches array is large enough
        labelData.ensureLabelBatchesSize(maxIndex);

        // Ensure we have a text renderer with the right font
        labelData.ensureTextRenderer(options.getNodeLabelFont(), vaoSupported);

        // Update label data for each node
        // Only recomputes glyphs if text changed, only recomputes bounds if sizeFactor changed
        for (int i = 0; i <= maxIndex; i++) {
            final Node node = nodes[i];

            if (node == null) {
                // Mark this slot as invalid
                labelData.invalidateBatch(i);
                continue;
            }

            final String text = texts[i];
            if (text == null) {
                // Mark as invalid (no text)
                labelData.invalidateBatch(i);
                continue;
            }

            boolean selected = someSelection && nodesCallback.isSelected(i);

            if (hideNonSelectedLabels && !selected) {
                // Mark as invalid (hidden)
                labelData.invalidateBatch(i);
                continue;
            }

            // Size calculation
            final float nodeSizeFactor = fitToNodeSize ? node.size() * fitNodeLabelsToNodeSizeFactor * nodeScale :
                node.getTextProperties().getSize() * nodeLabelSizeFactor;
            float sizeFactor = nodeLabelScale * nodeSizeFactor;
            if (labelSizeMode.equals(GraphRenderingOptions.LabelSizeMode.SCREEN)) {
                sizeFactor /= zoom;
            }

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

            // Update batch for this node (by storeId)
            // Glyphs are only recreated if text changed, bounds only recomputed if sizeFactor changed
            labelData.updateBatch(node, i, text, sizeFactor, node.x(), node.y(), finalR, finalG, finalB, finalA);
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

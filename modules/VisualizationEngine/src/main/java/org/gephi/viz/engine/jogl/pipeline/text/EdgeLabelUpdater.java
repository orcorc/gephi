package org.gephi.viz.engine.jogl.pipeline.text;

import org.gephi.graph.api.Edge;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.pipeline.PipelineCategory;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.util.structure.EdgesCallback;

public class EdgeLabelUpdater extends AbstractLabelUpdater<Edge> {

    public EdgeLabelUpdater(VizEngine engine, EdgeLabelData edgeLabelData) {
        super(engine, edgeLabelData);
    }

    @Override
    public void updateWorld(VizEngineModel model) {
        final GraphRenderingOptions options = model.getRenderingOptions();

        if (!options.isShowEdgeLabels()) {
            labelData.dispose();
            return;
        }

        // Get edges and their properties
        final EdgesCallback edgesCallback = (EdgesCallback) labelData.getElementsCallback();
        final boolean someSelection = edgesCallback.hasSelection();
        final String[] texts = edgesCallback.getEdgeLabelsArray();

        if (texts == null || texts.length == 0) {
            labelData.setMaxValidIndex(-1);
            return;
        }

        // Get edges array
        final Edge[] edges = edgesCallback.getEdgesArray();
        final int maxIndex = edgesCallback.getMaxIndex();

        // Rendering parameters
        final GraphRenderingOptions.LabelColorMode labelColorMode = options.getEdgeLabelColorMode();
        final GraphRenderingOptions.LabelSizeMode labelSizeMode = options.getEdgeLabelSizeMode();
        final GraphRenderingOptions.EdgeColorMode edgeColorMode = options.getEdgeColorMode();
        final float lightenNonSelectedFactor = options.getLightenNonSelectedFactor();
        final float edgeLabelScale = options.getEdgeLabelScale();
        final boolean hideNonSelectedLabels = options.isHideNonSelectedEdgeLabels();
        final float zoom = options.getZoom();

        // No labels to show
        if (hideNonSelectedLabels && !someSelection) {
            labelData.setMaxValidIndex(-1);
            return;
        }

        // Ensure label batches array is large enough
        labelData.ensureLabelBatchesSize(maxIndex);

        // Ensure we have a text renderer with the right font
        labelData.ensureTextRenderer(options.getEdgeLabelFont(), vaoSupported, mipMapSupported);

        // Set the max valid index for this frame (used by renderer to limit iteration)
        labelData.setMaxValidIndex(maxIndex);

        // Update label data for each edge
        // Only recomputes glyphs if text changed, only recomputes bounds if sizeFactor changed
        for (int i = 0; i <= maxIndex; i++) {
            final Edge edge = edges[i];

            if (edge == null) {
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

            boolean selected = someSelection && edgesCallback.isSelected(i);

            if (hideNonSelectedLabels && !selected) {
                // Mark as invalid (hidden)
                labelData.invalidateBatch(i);
                continue;
            }

            // Size calculation
            final float edgeSizeFactor = (float) Math.sqrt(edge.getTextProperties().getSize());
            float sizeFactor = edgeLabelScale * edgeSizeFactor;
            if (labelSizeMode.equals(GraphRenderingOptions.LabelSizeMode.SCREEN)) {
                sizeFactor /= zoom;
            }

            // Color calculation
            final int rgba =
                labelColorMode.equals(GraphRenderingOptions.LabelColorMode.OBJECT) ? getEdgeColor(edge, edgeColorMode) :
                    edge.getTextProperties().getRGBA();
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

            // Position of the label
            float x, y;
            if (edge.isDirected()) {
                x = (edge.getSource().x() + 2 * edge.getTarget().x()) /
                    3f;
                y = (edge.getSource().y() + 2 * edge.getTarget().y()) /
                    3f;
            } else {
                x = (edge.getSource().x() + edge.getTarget().x()) / 2f;
                y = (edge.getSource().y() + edge.getTarget().y()) / 2f;
            }

            // Update batch
            labelData.updateBatch(edge, i, text, sizeFactor, x, y,
                finalR, finalG, finalB, finalA);
        }
    }

    private int getEdgeColor(final Edge edge, GraphRenderingOptions.EdgeColorMode edgeColorMode) {
        switch (edgeColorMode) {
            case SOURCE: {
                return edge.getSource().getRGBA();
            }
            case TARGET: {
                return edge.getTarget().getRGBA();
            }
            case MIXED: {
                final int s = edge.getSource().getRGBA();
                final int t = edge.getTarget().getRGBA();
                if (s == t) {
                    return s;
                }
                final int b0 = ((s) & 0xFF) + ((t) & 0xFF);
                final int b1 = ((s >>> 8) & 0xFF) + ((t >>> 8) & 0xFF);
                final int b2 = ((s >>> 16) & 0xFF) + ((t >>> 16) & 0xFF);
                final int b3 = ((s >>> 24) & 0xFF) + ((t >>> 24) & 0xFF);
                return ((b3 >>> 1) << 24) | ((b2 >>> 1) << 16) | ((b1 >>> 1) << 8) | (b0 >>> 1);
            }
            case SELF:
            default: {
                return edge.getRGBA();
            }
        }
    }

    @Override
    public String getCategory() {
        return PipelineCategory.EDGE_LABEL;
    }

    @Override
    public String getName() {
        return "Edges Labels";
    }

}

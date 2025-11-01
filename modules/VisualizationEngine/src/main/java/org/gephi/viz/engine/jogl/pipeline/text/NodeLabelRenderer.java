package org.gephi.viz.engine.jogl.pipeline.text;

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.EnumSet;
import jogamp.text.TextRenderer;
import org.gephi.graph.api.Node;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.pipeline.common.NodeLabelWorldData;
import org.gephi.viz.engine.jogl.util.gl.capabilities.GLCapabilitiesSummary;
import org.gephi.viz.engine.pipeline.PipelineCategory;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.spi.Renderer;
import org.gephi.viz.engine.status.GraphRenderingOptions;
import org.gephi.viz.engine.util.gl.Constants;
import org.gephi.viz.engine.util.gl.OpenGLOptions;
import org.gephi.viz.engine.util.structure.NodesCallback;

@SuppressWarnings("rawtypes")
public class NodeLabelRenderer implements Renderer<JOGLRenderingTarget, NodeLabelWorldData> {
    public static final EnumSet<RenderingLayer> LAYERS = EnumSet.of(RenderingLayer.FRONT1);

    private final VizEngine engine;
    private final NodeLabelData nodeLabelData;

    // Java2D text
    private TextRenderer textRenderer;

    // Scratch
    private final float[] mvp = new float[16];

    public NodeLabelRenderer(VizEngine engine, NodeLabelData nodeLabelData) {
        this.engine = engine;
        this.nodeLabelData = nodeLabelData;
    }

    @Override
    public void init(JOGLRenderingTarget target) {
        // Nothing to do
    }

    @Override
    public NodeLabelWorldData worldUpdated(VizEngineModel model, JOGLRenderingTarget target) {
        final GraphRenderingOptions options = model.getRenderingOptions();

        return new NodeLabelWorldData(
            options.isShowNodeLabels(),
            options.getZoom(),
            options.getNodeScale(),
            options.getNodeLabelFont(),
            options.getNodeLabelScale(),
            options.getNodeLabelColorMode(),
            options.getNodeLabelSizeMode(),
            options.getNodeLabelSizeFactor(),
            options.isHideNonSelectedNodeLabels(),
            options.isNodeLabelFitToNodeSize(),
            options.getNodeLabelFitToNodeSizeFactor(),
            options.getLightenNonSelectedFactor()
        );
    }

    @Override
    public void render(NodeLabelWorldData data, JOGLRenderingTarget target, RenderingLayer layer) {
        if (!data.isShowNodeLabels()) {
            return;
        }
        engine.getModelViewProjectionMatrixFloats(mvp);

        final NodesCallback nodesCallback = nodeLabelData.getNodesCallback();
        final boolean someSelection = nodesCallback.hasSelection();
        final String[] texts = nodesCallback.getNodesLabelsArray();
        if (texts == null || texts.length == 0) {
            return;
        }
        final GraphRenderingOptions.LabelColorMode labelColorMode = data.getNodeLabelColorMode();
        final GraphRenderingOptions.LabelSizeMode labelSizeMode = data.getNodeLabelSizeMode();
        final float lightenNonSelectedFactor = data.getLightenNonSelectedFactor();
        final float nodeLabelScale = data.getNodeLabelScale();
        final float nodeLabelSizeFactor = data.getNodeLabelSizeFactor();
        final float fitNodeLabelsToNodeSizeFactor = data.getFitNodeLabelsToNodeSizeFactor();
        final boolean fitToNodeSize = data.isFitNodeLabelsToNodeSize();
        final boolean hideNonSelectedLabels = data.isHideNonSelectedLabels();
        final float zoom = data.getZoom();
        final float nodeScale = data.getNodeScale();

        if (hideNonSelectedLabels && !someSelection) {
            return;
        }

        refreshTextRendererIfNeeded(data.getNodeLabelFont(), target);
        textRenderer.begin3DRendering();
        textRenderer.setTransform(mvp);

        final Node[] nodes = nodesCallback.getNodesArray();
        final int maxIndex = nodesCallback.getMaxIndex();

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

            // Size
            final float nodeSizeFactor = fitToNodeSize ? node.size() * fitNodeLabelsToNodeSizeFactor * nodeScale :
                node.getTextProperties().getSize() * nodeLabelSizeFactor;
            float sizeFactor = nodeLabelScale * nodeSizeFactor;
            if (labelSizeMode.equals(GraphRenderingOptions.LabelSizeMode.SCREEN)) {
                sizeFactor /= zoom;
            }

            // Position
            final Rectangle2D bounds = textRenderer.getBounds(text);
            final float widthPx = (float) bounds.getWidth();
            final float ascentPx = (float) (-bounds.getY());
            final float heightPx = (float) bounds.getHeight();
            final float descentPx = heightPx - ascentPx;
            final float drawX = node.x() - (widthPx * sizeFactor) * 0.5f;
            final float drawY = node.y() - ((ascentPx - descentPx) * sizeFactor) * 0.5f;
            node.getTextProperties().setDimensions(widthPx * sizeFactor, heightPx * sizeFactor);

            // Color
            final int rgba = labelColorMode.equals(GraphRenderingOptions.LabelColorMode.OBJECT) ? node.getRGBA() :
                node.getTextProperties().getRGBA();
            final float r = (rgba >> 16 & 255) / 255.0F;
            final float g = (rgba >> 8 & 255) / 255.0F;
            final float b = (rgba & 255) / 255.0F;
            final float a = ((rgba >> 24) & 0xFF) / 255f;

            if (someSelection && !selected) {
                float lightColorFactor = 1 - lightenNonSelectedFactor;
                textRenderer.setColor(r, g, b, lightColorFactor);
            } else {
                textRenderer.setColor(r, g, b, a);
            }

            textRenderer.draw3D(
                text,
                drawX,
                drawY,
                0f,
                sizeFactor
            );
        }

        textRenderer.end3DRendering();
    }

    private void refreshTextRendererIfNeeded(Font font, JOGLRenderingTarget target) {
        if (textRenderer == null || !textRenderer.getFont().equals(font)) {
            if (textRenderer != null) {
                textRenderer.dispose();
            }
            textRenderer = new TextRenderer(font, /*antialiased*/ true, /*fractionalMetrics*/ true);
            final GLCapabilitiesSummary capabilities = target.getGlCapabilitiesSummary();
            final OpenGLOptions openGLOptions = engine.getOpenGLOptions();
            textRenderer.setUseVertexArrays(capabilities.isVAOSupported(openGLOptions));
            textRenderer.setSmoothing(true);
        }
    }

    @Override
    public EnumSet<RenderingLayer> getLayers() {
        return LAYERS;
    }

    @Override
    public int getOrder() {
        return Constants.RENDERING_ORDER_LABELS;
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
        return "Node Labels";
    }
}


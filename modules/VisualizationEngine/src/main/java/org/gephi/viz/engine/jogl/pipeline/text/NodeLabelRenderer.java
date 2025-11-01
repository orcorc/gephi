package org.gephi.viz.engine.jogl.pipeline.text;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.util.texture.TextureCoords;
import java.awt.Font;
import java.util.EnumSet;
import java.util.List;
import jogamp.text.TextRenderer;
import jogamp.text.util.Glyph;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.jogl.pipeline.common.NodeLabelWorldData;
import org.gephi.viz.engine.jogl.util.gl.capabilities.GLCapabilitiesSummary;
import org.gephi.viz.engine.pipeline.PipelineCategory;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.spi.Renderer;
import org.gephi.viz.engine.util.gl.OpenGLOptions;

@SuppressWarnings("rawtypes")
public class NodeLabelRenderer implements Renderer<JOGLRenderingTarget, NodeLabelWorldData> {
    public static final EnumSet<RenderingLayer> LAYERS = EnumSet.of(RenderingLayer.FRONT1);

    private final VizEngine engine;
    private final NodeLabelData nodeLabelData;

    // Java2D text renderer
    private TextRenderer textRenderer;
    private Font lastUsedFont;

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
        // This is the synchronization point between updater and renderer threads
        // The updater has finished preparing batches, now swap the buffers
        nodeLabelData.swapBuffers();

        return new NodeLabelWorldData(
            model.getRenderingOptions().isShowNodeLabels(),
            model.getRenderingOptions().getZoom(),
            model.getRenderingOptions().getNodeScale(),
            model.getRenderingOptions().getNodeLabelFont(),
            model.getRenderingOptions().getNodeLabelScale(),
            model.getRenderingOptions().getNodeLabelColorMode(),
            model.getRenderingOptions().getNodeLabelSizeMode(),
            model.getRenderingOptions().getNodeLabelSizeFactor(),
            model.getRenderingOptions().isHideNonSelectedNodeLabels(),
            model.getRenderingOptions().isNodeLabelFitToNodeSize(),
            model.getRenderingOptions().getNodeLabelFitToNodeSizeFactor(),
            model.getRenderingOptions().getLightenNonSelectedFactor()
        );
    }

    @Override
    public void render(NodeLabelWorldData data, JOGLRenderingTarget target, RenderingLayer layer) {
        if (!data.isShowNodeLabels()) {
            return;
        }

        final int batchCount = nodeLabelData.getBatchCount();
        if (batchCount == 0) {
            return;
        }

        // Ensure we have a text renderer configured for rendering
        // The updater has already prepared all batches
        refreshTextRendererIfNeeded(data.getNodeLabelFont(), target);
        
        engine.getModelViewProjectionMatrixFloats(mvp);

        final GL gl = GLContext.getCurrentGL();

        textRenderer.begin3DRendering();
        textRenderer.setTransform(mvp);

        // Get the pre-computed batches from the updater
        final List<NodeLabelData.LabelBatch> batches = nodeLabelData.getLabelBatches();

        // Render each prepared batch
        // All glyphs, positions, colors were pre-computed in the updater thread
        for (int i = 0; i < batchCount; i++) {
            final NodeLabelData.LabelBatch batch = batches.get(i);
            
            // Set color for this batch
            textRenderer.setColor(batch.r, batch.g, batch.b, batch.a);
            
            // Render each glyph in the batch
            float x = batch.x;
            for (final Glyph glyph : batch.glyphs) {
                // Upload glyph to texture cache if needed (requires GL)
                if (glyph.location == null) {
                    textRenderer.getGlyphCache().upload(glyph);
                }
                
                // Get texture coordinates
                final TextureCoords coords = textRenderer.getGlyphCache().find(glyph);
                
                // Draw the glyph
                final float advance = textRenderer.getGlyphRenderer().drawGlyph(
                    gl, glyph, x, batch.y, 0f, batch.scale, coords
                );
                x += advance * batch.scale;
            }
        }

        textRenderer.end3DRendering();
    }

    private void refreshTextRendererIfNeeded(Font font, JOGLRenderingTarget target) {
        if (textRenderer == null || !font.equals(lastUsedFont)) {
            if (textRenderer != null) {
                textRenderer.dispose();
            }
            
            // Get the TextRenderer from nodeLabelData if it exists
            // The updater has already created it without GL context
            textRenderer = nodeLabelData.getTextRenderer();
            
            if (textRenderer == null) {
                // Fallback: create a new one if updater hasn't run yet
                textRenderer = new TextRenderer(font, /*antialiased*/ true, /*fractionalMetrics*/ true);
            }
            
            final GLCapabilitiesSummary capabilities = target.getGlCapabilitiesSummary();
            final OpenGLOptions openGLOptions = engine.getOpenGLOptions();
            textRenderer.setUseVertexArrays(capabilities.isVAOSupported(openGLOptions));
            textRenderer.setSmoothing(true);
            lastUsedFont = font;
        }
    }

    @Override
    public EnumSet<RenderingLayer> getLayers() {
        return LAYERS;
    }

    @Override
    public int getOrder() {
        return org.gephi.viz.engine.util.gl.Constants.RENDERING_ORDER_LABELS;
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


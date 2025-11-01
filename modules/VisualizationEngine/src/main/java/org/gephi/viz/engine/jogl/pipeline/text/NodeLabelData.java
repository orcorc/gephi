package org.gephi.viz.engine.jogl.pipeline.text;

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import jogamp.text.TextRenderer;
import jogamp.text.util.Glyph;
import org.gephi.viz.engine.util.structure.NodesCallback;

public class NodeLabelData {

    private final NodesCallback nodesCallback;

    // Double-buffered batches for thread-safe concurrent access
    private final List<LabelBatch> labelBatchesA = new ArrayList<>();
    private final List<LabelBatch> labelBatchesB = new ArrayList<>();
    
    // Updater writes to write buffer, renderer reads from read buffer
    private volatile List<LabelBatch> writeLabelBatches = labelBatchesA;
    private volatile List<LabelBatch> readLabelBatches = labelBatchesB;
    private volatile int readBatchCount = 0;
    private int writeBatchCount = 0;

    // TextRenderer for glyph preparation (doesn't need GL context)
    // Volatile to ensure visibility across threads
    private volatile TextRenderer textRenderer;
    private volatile Font currentFont;

    public NodeLabelData(NodesCallback nodesCallback) {
        this.nodesCallback = nodesCallback;
    }

    public NodesCallback getNodesCallback() {
        return nodesCallback;
    }

    /**
     * Ensures the text renderer is initialized with the correct font.
     * This is called from the updater thread and doesn't require GL context.
     */
    public void ensureTextRenderer(Font font) {
        if (textRenderer == null || !font.equals(currentFont)) {
            if (textRenderer != null) {
                // Note: We can't dispose here as it requires GL context
                // The renderer will handle disposal
                textRenderer = null;
            }
            textRenderer = new TextRenderer(font, /*antialiased*/ true, /*fractionalMetrics*/ true);
            currentFont = font;
        }
    }

    /**
     * Gets the bounds of text using the text renderer.
     * This doesn't require GL context.
     */
    public Rectangle2D getTextBounds(String text) {
        if (textRenderer == null || text == null || text.isEmpty()) {
            return null;
        }
        return textRenderer.getBounds(text);
    }

    /**
     * Clears the pre-computed batches.
     * Called by updater thread - writes to write buffer.
     */
    public void clearLabelData() {
        writeBatchCount = 0;
    }

    /**
     * Adds a pre-computed batch for rendering.
     * Pre-creates glyphs without requiring GL context.
     * Called by updater thread - writes to write buffer.
     */
    public void addBatch(String text, float x, float y, float scale, float r, float g, float b, float a) {
        if (textRenderer == null || text == null || text.isEmpty()) {
            return;
        }

        // Pre-create glyphs using Java2D (no GL context needed)
        final List<Glyph> glyphs = textRenderer.getGlyphProducer().createGlyphs(text);
        if (glyphs == null || glyphs.isEmpty()) {
            return;
        }

        // Store batch data in write buffer
        if (writeBatchCount >= writeLabelBatches.size()) {
            writeLabelBatches.add(new LabelBatch());
        }
        
        final LabelBatch batch = writeLabelBatches.get(writeBatchCount);
        
        // Make a defensive copy to avoid concurrent modification
        // The glyphs list might be reused by TextRenderer
        if (batch.glyphs == null) {
            batch.glyphs = new ArrayList<>(glyphs);
        } else {
            batch.glyphs.clear();
            batch.glyphs.addAll(glyphs);
        }
        
        batch.x = x;
        batch.y = y;
        batch.scale = scale;
        batch.r = r;
        batch.g = g;
        batch.b = b;
        batch.a = a;
        
        writeBatchCount++;
    }

    /**
     * Swaps the read/write buffers.
     * Called by renderer thread in worldUpdated() after updater completes.
     * This makes the newly prepared data visible to the renderer.
     */
    public void swapBuffers() {
        // Swap the buffers
        final List<LabelBatch> temp = readLabelBatches;
        readLabelBatches = writeLabelBatches;
        writeLabelBatches = temp;
        
        // Update read count (volatile write ensures visibility)
        readBatchCount = writeBatchCount;
    }

    /**
     * Gets the list of pre-computed batches for rendering.
     * Called by renderer thread - reads from read buffer.
     */
    public List<LabelBatch> getLabelBatches() {
        return readLabelBatches;
    }

    /**
     * Gets the number of batches to render.
     * Called by renderer thread - reads from read buffer.
     */
    public int getBatchCount() {
        return readBatchCount;
    }

    /**
     * Gets the TextRenderer for use in the rendering thread.
     * The renderer needs to call begin/end rendering with GL context.
     */
    public TextRenderer getTextRenderer() {
        return textRenderer;
    }

    /**
     * Pre-computed batch containing glyphs and rendering parameters.
     */
    public static class LabelBatch {
        public List<Glyph> glyphs;
        public float x;
        public float y;
        public float scale;
        public float r;
        public float g;
        public float b;
        public float a;
    }
}

package org.gephi.viz.engine.jogl.pipeline.common;

import jogamp.text.TextRenderer;
import org.gephi.viz.engine.jogl.pipeline.text.NodeLabelData;
import org.gephi.viz.engine.spi.WorldData;

public class NodeLabelWorldData implements WorldData {

    private final TextRenderer textRenderer;
    private final NodeLabelData.LabelBatch[] labelBatches;
    private final int maxIndex;

    public NodeLabelWorldData(TextRenderer textRenderer, NodeLabelData.LabelBatch[] labelBatches, int maxIndex) {
        this.textRenderer = textRenderer;
        this.labelBatches = labelBatches;
        this.maxIndex = maxIndex;
    }

    public TextRenderer getTextRenderer() {
        return textRenderer;
    }

    public NodeLabelData.LabelBatch[] getLabelBatches() {
        return labelBatches;
    }

    public int getMaxIndex() {
        return maxIndex;
    }
}

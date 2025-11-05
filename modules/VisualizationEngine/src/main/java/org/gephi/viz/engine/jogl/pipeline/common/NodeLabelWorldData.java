package org.gephi.viz.engine.jogl.pipeline.common;

import jogamp.text.TextRenderer;
import org.gephi.viz.engine.spi.WorldData;

public class NodeLabelWorldData implements WorldData {

    private final TextRenderer textRenderer;

    public NodeLabelWorldData(TextRenderer textRenderer) {
        this.textRenderer = textRenderer;
    }

    public TextRenderer getTextRenderer() {
        return textRenderer;
    }
}

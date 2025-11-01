package org.gephi.viz.engine.jogl.pipeline.text;

import org.gephi.graph.api.Node;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.VizEngineModel;
import org.gephi.viz.engine.jogl.JOGLRenderingTarget;
import org.gephi.viz.engine.pipeline.PipelineCategory;
import org.gephi.viz.engine.spi.ElementsCallback;
import org.gephi.viz.engine.spi.WorldUpdater;

public class NodeLabelUpdater implements WorldUpdater<JOGLRenderingTarget, Node> {

    private final VizEngine engine;
    private final NodeLabelData labelData;

    public NodeLabelUpdater(VizEngine engine, NodeLabelData labelData) {
        this.engine = engine;
        this.labelData = labelData;
    }

    @Override
    public void init(JOGLRenderingTarget target) {

    }

    @Override
    public void dispose(JOGLRenderingTarget target) {

    }

    @Override
    public void updateWorld(VizEngineModel model) {

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

package org.gephi.viz.engine.jogl.pipeline.text;

import org.gephi.viz.engine.util.structure.NodesCallback;

public class NodeLabelData {

    private final NodesCallback nodesCallback;

    public NodeLabelData(NodesCallback nodesCallback) {
        this.nodesCallback = nodesCallback;
    }

    public NodesCallback getNodesCallback() {
        return nodesCallback;
    }
}

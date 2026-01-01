package org.gephi.visualization;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.gephi.graph.api.Node;
import org.gephi.viz.engine.VizEngine;
import org.gephi.viz.engine.status.GraphSelection;
import org.gephi.viz.engine.status.GraphSelectionImpl;

public class SelectionModelImpl {

    // Model
    private final VizModel visualisationModel;
    // Settings
    private int mouseSelectionDiameter;
    private boolean mouseSelectionZoomProportional;
    //States
    private boolean rectangleSelection = false;
    private boolean selectionEnable = true;
    private boolean customSelection = false;
    private boolean singleNodeSelection = false;
    private boolean nodeSelection = false;

    public SelectionModelImpl(VizModel visualisationModel) {
        this.visualisationModel = visualisationModel;

        // Settings
        this.mouseSelectionDiameter = VizConfig.getDefaultMouseSelectionDiameter();
    }

    public GraphSelection toGraphSelection() {
        GraphSelectionImpl gs = new GraphSelectionImpl();
        if (selectionEnable) {
            if (rectangleSelection) {
                gs.setMode(GraphSelection.GraphSelectionMode.RECTANGLE_SELECTION);
            } else if (customSelection) {
                gs.setMode(GraphSelection.GraphSelectionMode.CUSTOM_SELECTION);
            } else if (nodeSelection) {
                if (singleNodeSelection) {
                    gs.setMode(GraphSelection.GraphSelectionMode.SINGLE_NODE_SELECTION);
                } else {
                    gs.setMode(GraphSelection.GraphSelectionMode.MULTI_NODE_SELECTION);
                }
            } else {
                gs.setMode(GraphSelection.GraphSelectionMode.SIMPLE_MOUSE_SELECTION);
            }
        }
        gs.setMouseSelectionDiameter(mouseSelectionDiameter);
        gs.setMouseSelectionDiameterZoomProportional(mouseSelectionZoomProportional);
        return gs;
    }

    protected Optional<GraphSelection> currentEngineSelectionModel() {
        return visualisationModel.getEngine().map(VizEngine::getGraphSelection);
    }

    public Collection<Node> getSelectedNodes() {
        return currentEngineSelectionModel()
            .map(GraphSelection::getSelectedNodes)
            .orElse(Collections.emptyList());
    }

    public int getMouseSelectionDiameter() {
        return mouseSelectionDiameter;
    }

    protected void setMouseSelectionDiameter(int mouseSelectionDiameter) {
        this.mouseSelectionDiameter = mouseSelectionDiameter;
    }

    public boolean isMouseSelectionZoomProportional() {
        return mouseSelectionZoomProportional;
    }

    protected void setMouseSelectionZoomProportional(boolean mouseSelectionZoomProportional) {
        this.mouseSelectionZoomProportional = mouseSelectionZoomProportional;
    }

    protected void setSelectionEnable(boolean selectionEnable) {
        this.selectionEnable = selectionEnable;
    }

    protected void setRectangleSelection(boolean rectangleSelection) {
        this.rectangleSelection = rectangleSelection;
    }

    protected void setCustomSelection(boolean customSelection) {
        this.customSelection = customSelection;
    }

    public void setSingleNodeSelection(boolean singleNodeSelection) {
        this.singleNodeSelection = singleNodeSelection;
    }

    public void setNodeSelection(boolean nodeSelection) {
        this.nodeSelection = nodeSelection;
    }

    public boolean isRectangleSelection() {
        return selectionEnable && rectangleSelection;
    }

    public boolean isDirectMouseSelection() {
        return selectionEnable && !rectangleSelection;
    }

    public boolean isCustomSelection() {
        return selectionEnable && customSelection;
    }

    public boolean isSingleNodeSelection() {
        return singleNodeSelection;
    }

    public boolean isSelectionEnabled() {
        return selectionEnable;
    }

    public boolean isNodeSelection() {
        return nodeSelection;
    }
}

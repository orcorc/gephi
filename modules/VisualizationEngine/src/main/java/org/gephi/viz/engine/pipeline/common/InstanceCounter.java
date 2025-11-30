package org.gephi.viz.engine.pipeline.common;

/**
 *
 * @author Eduardo Ramos
 */
public class InstanceCounter {

    public int unselectedCount = 0;
    public int selectedCount = 0;
    public int unselectedCountToDraw = 0;
    public int selectedCountToDraw = 0;
    public int selfLoopCount = 0;
    public int selfLoopCountToDraw = 0;

    public void promoteCountToDraw() {
        unselectedCountToDraw = unselectedCount;
        selectedCountToDraw = selectedCount;
        selfLoopCountToDraw = selfLoopCount;
    }

    public void clearCount() {
        unselectedCount = 0;
        selectedCount = 0;
        selfLoopCount = 0;
    }

    public int total() {
        return unselectedCount + selectedCount;
    }

    public int totalToDraw() {
        return unselectedCountToDraw + selectedCountToDraw;
    }

    @Override
    public String toString() {
        return "InstanceCounter{" + "unselectedCount=" + unselectedCount
            + ", selectedCount=" + selectedCount
            + ", selfLoopCount=" + selfLoopCount
            + ", unselectedCountToDraw=" + unselectedCountToDraw
            + ", selectedCountToDraw=" + selectedCountToDraw
            + ", selfLoopCountToDraw=" + selfLoopCountToDraw + '}';
    }
}

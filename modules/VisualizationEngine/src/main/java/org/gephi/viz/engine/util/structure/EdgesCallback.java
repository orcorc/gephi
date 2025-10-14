package org.gephi.viz.engine.util.structure;

import static org.gephi.viz.engine.util.ArrayUtils.getNextPowerOf2;

import java.util.Arrays;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.ColumnIndex;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.viz.engine.structure.GraphIndex.ElementsCallback;

/**
 *
 * @author Eduardo Ramos
 */
public class EdgesCallback implements ElementsCallback<Edge> {

    private int edgeWeightVersion = -1;
    private float minWeight = 0f;
    private float maxWeight = 1f;
    private Edge[] edgesArray = new Edge[0];
    private int maxIndex = 0;
    private int edgeCount = 0;

    @Override
    public void start(Graph graph) {
        graph.readLock();
        Arrays.fill(edgesArray, null);
        edgesArray = ensureEdgesArraySize(edgesArray, graph.getModel().getMaxEdgeStoreId() + 1);
        maxIndex = 0;
        edgeCount = 0;
    }

    @Override
    public void accept(Edge edge) {
        int storeId = edge.getStoreId();
        if (storeId > maxIndex) {
            maxIndex = storeId;
        }
        edgesArray[storeId] = edge;
    }

    @Override
    public void end(Graph graph) {
        graph.readUnlock();
        // Count non-null edges
        // This can't be done in accept as edges can be duplicated and accept is called via multiple threads (parallel stream)
        for (Edge edge : edgesArray) {
            if (edge != null) {
                edgeCount++;
            }
        }
        // Refresh min/max edge weight (if needed)
        Column weightCol = graph.getModel().getEdgeTable().getColumn(3); //Weight column
        ColumnIndex edgeWeightIndex = graph.getModel().getEdgeIndex().getColumnIndex(weightCol);
        if (edgeWeightIndex.getVersion() != edgeWeightVersion) {
            edgeWeightVersion = edgeWeightIndex.getVersion();
            Number minValue = edgeWeightIndex.getMinValue();
            Number maxValue = edgeWeightIndex.getMaxValue();
            minWeight = minValue != null ? minValue.floatValue() : 0f;
            maxWeight = maxValue != null ? maxValue.floatValue() : 1f;
        }
    }

    public void reset() {
        edgesArray = new Edge[0];
        maxIndex = 0;
        edgeCount = 0;
    }

    public Edge[] getEdgesArray() {
        return edgesArray;
    }

    public int getMaxIndex() {
        return maxIndex;
    }

    public int getCount() {
        return edgeCount;
    }

    public float getMinWeight() {
        return minWeight;
    }

    public float getMaxWeight() {
        return maxWeight;
    }

    protected Edge[] ensureEdgesArraySize(Edge[] array, int size) {
        if (size > array.length) {
            int newSize = getNextPowerOf2(size);
            System.out.println("Growing edge vector from " + array.length + " to " + newSize + " elements");

            final Edge[] newVector = new Edge[newSize];
            System.arraycopy(array, 0, newVector, 0, array.length);

            return newVector;
        } else {
            return array;
        }
    }
}

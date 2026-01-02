package org.gephi.viz.engine.jogl.pipeline.arrays;

import static com.jogamp.opengl.GL.GL_FLOAT;
import static org.gephi.viz.engine.jogl.models.EdgeLineModelUndirected.VERTEX_COUNT;
import static org.gephi.viz.engine.pipeline.RenderingLayer.BACK1;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL3ES3;
import com.jogamp.opengl.util.GLBuffers;
import java.nio.FloatBuffer;
import org.gephi.graph.api.Edge;
import org.gephi.viz.engine.jogl.models.EdgeLineModelDirected;
import org.gephi.viz.engine.jogl.pipeline.common.AbstractEdgeData;
import org.gephi.viz.engine.jogl.pipeline.common.EdgeWorldData;
import org.gephi.viz.engine.jogl.util.ManagedDirectBuffer;
import org.gephi.viz.engine.jogl.util.gl.GLBufferMutable;
import org.gephi.viz.engine.jogl.util.gl.GLFunctions;
import org.gephi.viz.engine.pipeline.RenderingLayer;
import org.gephi.viz.engine.status.GraphSelection;
import org.gephi.viz.engine.util.ArrayUtils;
import org.gephi.viz.engine.util.structure.EdgesCallback;
import org.gephi.viz.engine.util.structure.NodesCallback;

/**
 *
 * @author Eduardo Ramos
 */
public class ArrayDrawEdgeData extends AbstractEdgeData {

    private final int[] bufferName = new int[6];

    private static final int VERT_BUFFER_UNDIRECTED = 0;
    private static final int VERT_BUFFER_DIRECTED = 1;
    private static final int ATTRIBS_BUFFER_DIRECTED = 2;
    private static final int ATTRIBS_BUFFER_UNDIRECTED = 3;
    private static final int VERT_BUFFER_SELFLOOP = 4;
    private static final int ATTRIBS_BUFFER_SELFLOOP = 5;

    private float[] attributesBuffer;

    //For drawing in a loop:
    private float[] attributesDrawBufferBatchOneCopyPerVertex;
    private ManagedDirectBuffer attributesDrawBufferBatchOneCopyPerVertexManagedDirectBuffer;


    public ArrayDrawEdgeData(final EdgesCallback edgesCallback, final NodesCallback nodesCallback) {
        super(edgesCallback, nodesCallback, false, false);
    }

    public void drawArrays(GL2ES2 gl, RenderingLayer layer, EdgeWorldData data, float[] mvpFloats) {
        refreshTime();
        if (edgesCallback.hasSelfLoop()) {
            drawSelfLoop(gl, data, layer, mvpFloats);
        }
        drawUndirected(gl, data, layer, mvpFloats);
        drawDirected(gl, data, layer, mvpFloats);

    }

    private void drawSelfLoop(GL2ES2 gl, EdgeWorldData data,
                              RenderingLayer layer, float[] mvpFloats) {
        final int instanceCount = setupShaderProgramForRenderingLayerSelfLoop(gl, layer, data, mvpFloats);

        final boolean renderingUnselectedEdges = layer == BACK1;
        final int instancesOffset =
            renderingUnselectedEdges ? 0 : selfLoopCounter.selectedCountToDraw;

        final FloatBuffer batchUpdateBuffer =
            attributesDrawBufferBatchOneCopyPerVertexManagedDirectBuffer.floatBuffer();

        final int maxIndex = (instancesOffset + instanceCount);
        for (int edgeBase = instancesOffset; edgeBase < maxIndex; edgeBase += BATCH_SELFLOOP_EDGES_SIZE) {
            final int drawBatchCount = Math.min(maxIndex - edgeBase, BATCH_SELFLOOP_EDGES_SIZE);

            //Need to copy attributes as many times as vertex per model:
            for (int edgeIndex = 0; edgeIndex < drawBatchCount; edgeIndex++) {
                System.arraycopy(
                    attributesBuffer, (edgeBase + edgeIndex) * ATTRIBS_STRIDE_SELFLOOP,
                    attributesDrawBufferBatchOneCopyPerVertex,
                    edgeIndex * ATTRIBS_STRIDE_SELFLOOP * selfLoopMesh.vertexCount,
                    ATTRIBS_STRIDE_SELFLOOP
                );

                ArrayUtils.repeat(
                    attributesDrawBufferBatchOneCopyPerVertex,
                    edgeIndex * ATTRIBS_STRIDE_SELFLOOP * selfLoopMesh.vertexCount,
                    ATTRIBS_STRIDE_SELFLOOP,
                    selfLoopMesh.vertexCount
                );
            }

            batchUpdateBuffer.clear();
            batchUpdateBuffer.put(attributesDrawBufferBatchOneCopyPerVertex, 0,
                drawBatchCount * ATTRIBS_STRIDE_SELFLOOP * selfLoopMesh.vertexCount);
            batchUpdateBuffer.flip();

            attributesGLBufferSelfLoop.bind(gl);
            attributesGLBufferSelfLoop.updateWithOrphaning(gl, batchUpdateBuffer);
            attributesGLBufferSelfLoop.unbind(gl);

            GLFunctions.drawInstanced((GL3ES3) gl, 0, drawBatchCount, selfLoopMesh.vertexCount * drawBatchCount);
        }

        GLFunctions.stopUsingProgram(gl);
        unsetupSelfLoopVertexArrayAttributes(gl);
    }

    private void drawUndirected(GL2ES2 gl, EdgeWorldData data,
                                RenderingLayer layer, float[] mvpFloats) {
        final int instanceCount = setupShaderProgramForRenderingLayerUndirected(gl, layer, data, mvpFloats);

        final boolean renderingUnselectedEdges = layer == BACK1;
        final int instancesOffset = renderingUnselectedEdges ? 0 : undirectedInstanceCounter.unselectedCountToDraw;

        final FloatBuffer batchUpdateBuffer =
            attributesDrawBufferBatchOneCopyPerVertexManagedDirectBuffer.floatBuffer();

        final int maxIndex = (instancesOffset + instanceCount);
        for (int edgeBase = instancesOffset; edgeBase < maxIndex; edgeBase += BATCH_EDGES_SIZE) {
            final int drawBatchCount = Math.min(maxIndex - edgeBase, BATCH_EDGES_SIZE);

            //Need to copy attributes as many times as vertex per model:
            for (int edgeIndex = 0; edgeIndex < drawBatchCount; edgeIndex++) {
                System.arraycopy(
                    attributesBuffer, (edgeBase + edgeIndex) * ATTRIBS_STRIDE,
                    attributesDrawBufferBatchOneCopyPerVertex, edgeIndex * ATTRIBS_STRIDE * VERTEX_COUNT_UNDIRECTED,
                    ATTRIBS_STRIDE
                );

                ArrayUtils.repeat(
                    attributesDrawBufferBatchOneCopyPerVertex,
                    edgeIndex * ATTRIBS_STRIDE * VERTEX_COUNT_UNDIRECTED,
                    ATTRIBS_STRIDE,
                    VERTEX_COUNT_UNDIRECTED
                );
            }

            batchUpdateBuffer.clear();
            batchUpdateBuffer.put(attributesDrawBufferBatchOneCopyPerVertex, 0,
                drawBatchCount * ATTRIBS_STRIDE * VERTEX_COUNT_UNDIRECTED);
            batchUpdateBuffer.flip();

            attributesGLBufferUndirected.bind(gl);
            attributesGLBufferUndirected.updateWithOrphaning(gl, batchUpdateBuffer);
            attributesGLBufferUndirected.unbind(gl);

            GLFunctions.drawInstanced((GL3ES3) gl, 0, drawBatchCount, VERTEX_COUNT * drawBatchCount);
        }

        GLFunctions.stopUsingProgram(gl);
        unsetupUndirectedVertexArrayAttributes(gl);
    }

    private void drawDirected(GL2ES2 gl, EdgeWorldData data,
                              RenderingLayer layer, float[] mvpFloats) {
        final int instanceCount = setupShaderProgramForRenderingLayerDirected(gl, layer, data, mvpFloats);

        final boolean renderingUnselectedEdges = layer == BACK1;
        final int instancesOffset;
        if (renderingUnselectedEdges) {
            instancesOffset = undirectedInstanceCounter.totalToDraw();
        } else {
            instancesOffset = undirectedInstanceCounter.totalToDraw() + directedInstanceCounter.unselectedCountToDraw;
        }

        final FloatBuffer batchUpdateBuffer =
            attributesDrawBufferBatchOneCopyPerVertexManagedDirectBuffer.floatBuffer();

        final int maxIndex = (instancesOffset + instanceCount);
        for (int edgeBase = instancesOffset; edgeBase < maxIndex; edgeBase += BATCH_EDGES_SIZE) {
            final int drawBatchCount = Math.min(maxIndex - edgeBase, BATCH_EDGES_SIZE);

            //Need to copy attributes as many times as vertex per model:
            for (int edgeIndex = 0; edgeIndex < drawBatchCount; edgeIndex++) {
                System.arraycopy(
                    attributesBuffer, (edgeBase + edgeIndex) * ATTRIBS_STRIDE,
                    attributesDrawBufferBatchOneCopyPerVertex, edgeIndex * ATTRIBS_STRIDE * VERTEX_COUNT_DIRECTED,
                    ATTRIBS_STRIDE
                );

                ArrayUtils.repeat(
                    attributesDrawBufferBatchOneCopyPerVertex,
                    edgeIndex * ATTRIBS_STRIDE * VERTEX_COUNT_DIRECTED,
                    ATTRIBS_STRIDE,
                    VERTEX_COUNT_DIRECTED
                );
            }

            batchUpdateBuffer.clear();
            batchUpdateBuffer.put(attributesDrawBufferBatchOneCopyPerVertex, 0,
                drawBatchCount * ATTRIBS_STRIDE * VERTEX_COUNT_DIRECTED);
            batchUpdateBuffer.flip();

            attributesGLBufferDirected.bind(gl);
            attributesGLBufferDirected.updateWithOrphaning(gl, batchUpdateBuffer);
            attributesGLBufferDirected.unbind(gl);


            GLFunctions.drawArraysSingleInstance(gl, 0, EdgeLineModelDirected.VERTEX_COUNT * drawBatchCount);
        }

        GLFunctions.stopUsingProgram(gl);
        unsetupDirectedVertexArrayAttributes(gl);
    }

    @Override
    protected void initBuffers(GL gl) {
        super.initBuffers(gl);
        attributesDrawBufferBatchOneCopyPerVertex = new float[ATTRIBS_STRIDE * VERTEX_COUNT_MAX *
            BATCH_EDGES_SIZE];//Need to copy attributes as many times as vertex per model
        attributesDrawBufferBatchOneCopyPerVertexManagedDirectBuffer =
            new ManagedDirectBuffer(GL_FLOAT, ATTRIBS_STRIDE * VERTEX_COUNT_MAX * BATCH_EDGES_SIZE);

        gl.glGenBuffers(bufferName.length, bufferName, 0);

        {

            float[] undirectedVertexDataArray = new float[undirectedEdgeMesh.vertexData.length * BATCH_EDGES_SIZE];
            System.arraycopy(undirectedEdgeMesh.vertexData, 0, undirectedVertexDataArray, 0,
                undirectedEdgeMesh.vertexData.length);
            ArrayUtils.repeat(undirectedVertexDataArray, 0, undirectedEdgeMesh.vertexData.length, BATCH_EDGES_SIZE);

            final FloatBuffer undirectedVertexData = GLBuffers.newDirectFloatBuffer(undirectedVertexDataArray);

            vertexGLBufferUndirected =
                new GLBufferMutable(bufferName[VERT_BUFFER_UNDIRECTED], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
            vertexGLBufferUndirected.bind(gl);
            vertexGLBufferUndirected.init(gl, undirectedVertexData, GLBufferMutable.GL_BUFFER_USAGE_STATIC_DRAW);
            vertexGLBufferUndirected.unbind(gl);
        }

        {

            float[] directedVertexDataArray = new float[directedEdgeMesh.vertexData.length * BATCH_EDGES_SIZE];
            System.arraycopy(directedEdgeMesh.vertexData, 0, directedVertexDataArray, 0,
                directedEdgeMesh.vertexData.length);
            ArrayUtils.repeat(directedVertexDataArray, 0, directedEdgeMesh.vertexData.length, BATCH_EDGES_SIZE);

            final FloatBuffer directedVertexData = GLBuffers.newDirectFloatBuffer(directedVertexDataArray);

            vertexGLBufferDirected =
                new GLBufferMutable(bufferName[VERT_BUFFER_DIRECTED], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
            vertexGLBufferDirected.bind(gl);
            vertexGLBufferDirected.init(gl, directedVertexData, GLBufferMutable.GL_BUFFER_USAGE_STATIC_DRAW);
            vertexGLBufferDirected.unbind(gl);
        }

        {

            float[] selfLoopVertexDataArray = new float[selfLoopMesh.vertexData.length * BATCH_SELFLOOP_EDGES_SIZE];
            System.arraycopy(directedEdgeMesh.vertexData, 0, selfLoopVertexDataArray, 0,
                selfLoopMesh.vertexData.length);
            ArrayUtils.repeat(selfLoopVertexDataArray, 0, directedEdgeMesh.vertexData.length, BATCH_SELFLOOP_EDGES_SIZE);

            final FloatBuffer selfLoopVertexData = GLBuffers.newDirectFloatBuffer(selfLoopVertexDataArray);

            vertexGLBufferDirected =
                new GLBufferMutable(bufferName[VERT_BUFFER_SELFLOOP], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
            vertexGLBufferDirected.bind(gl);
            vertexGLBufferDirected.init(gl, selfLoopVertexData, GLBufferMutable.GL_BUFFER_USAGE_STATIC_DRAW);
            vertexGLBufferDirected.unbind(gl);
        }

        //Initialize for batch edges size:
        attributesGLBufferDirected =
            new GLBufferMutable(bufferName[ATTRIBS_BUFFER_DIRECTED], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        attributesGLBufferDirected.bind(gl);
        attributesGLBufferDirected.init(gl, (long) VERTEX_COUNT_MAX * ATTRIBS_STRIDE * Float.BYTES * BATCH_EDGES_SIZE,
            GLBufferMutable.GL_BUFFER_USAGE_DYNAMIC_DRAW);
        attributesGLBufferDirected.unbind(gl);

        attributesGLBufferUndirected =
            new GLBufferMutable(bufferName[ATTRIBS_BUFFER_UNDIRECTED], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        attributesGLBufferUndirected.bind(gl);
        attributesGLBufferUndirected.init(gl, (long) VERTEX_COUNT_MAX * ATTRIBS_STRIDE * Float.BYTES * BATCH_EDGES_SIZE,
            GLBufferMutable.GL_BUFFER_USAGE_DYNAMIC_DRAW);
        attributesGLBufferUndirected.unbind(gl);

        attributesGLBufferSelfLoop =
            new GLBufferMutable(bufferName[ATTRIBS_BUFFER_SELFLOOP], GLBufferMutable.GL_BUFFER_TYPE_ARRAY);
        attributesGLBufferSelfLoop.bind(gl);
        attributesGLBufferSelfLoop.init(gl,
            (long) selfLoopMesh.vertexData.length * ATTRIBS_STRIDE_SELFLOOP * Float.BYTES * BATCH_SELFLOOP_EDGES_SIZE,
            GLBufferMutable.GL_BUFFER_USAGE_DYNAMIC_DRAW);
        attributesGLBufferSelfLoop.unbind(gl);

        attributesBuffer = new float[ATTRIBS_STRIDE * BATCH_EDGES_SIZE];
    }

    public void updateBuffers() {
        undirectedInstanceCounter.promoteCountToDraw();
        directedInstanceCounter.promoteCountToDraw();
        selfLoopCounter.promoteCountToDraw();
    }

    @Override
    protected void updateData(final GraphSelection selection) {

        int totalEdges = edgesCallback.getCount();
        final float[] attribs
            = attributesBuffer
            = ArrayUtils.ensureCapacityNoCopy(attributesBuffer, totalEdges * ATTRIBS_STRIDE);

        final Edge[] visibleEdgesArray = edgesCallback.getEdgesArray();
        final float[] edgeWeightsArray = edgesCallback.getEdgeWeightsArray();
        final int maxIndex = edgesCallback.getMaxIndex();
        final boolean directed = edgesCallback.isDirected();
        final boolean undirected = edgesCallback.isUndirected();

        int attribsIndex = 0;
        attribsIndex = updateUndirectedData(
            directed,
            maxIndex, visibleEdgesArray, edgeWeightsArray,
            attribs, attribsIndex
        );
        updateDirectedData(
            undirected, maxIndex, visibleEdgesArray, edgeWeightsArray,
            attribs, attribsIndex
        );
    }

    @Override
    public void dispose(GL gl) {
        super.dispose(gl);
        attributesDrawBufferBatchOneCopyPerVertex = null;
        attributesDrawBufferBatchOneCopyPerVertexManagedDirectBuffer.destroy();

        attributesBuffer = null;
    }
}

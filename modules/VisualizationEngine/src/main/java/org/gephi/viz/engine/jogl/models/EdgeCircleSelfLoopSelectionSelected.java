package org.gephi.viz.engine.jogl.models;

import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_COLOR;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_POSITION;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_SELFLOOP_NODE_SIZE;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_SIZE;
import static org.gephi.viz.engine.util.gl.Constants.ATTRIB_NAME_VERT;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_COLOR_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_POSITION_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_SELFLOOP_NODE_SIZE_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_SIZE_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.SHADER_VERT_LOCATION;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_BACKGROUND_COLOR;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_COLOR_LIGHTEN_FACTOR;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_EDGE_SCALE_MAX;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_EDGE_SCALE_MIN;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_GLOBAL_TIME;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_MIN_WEIGHT;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_MODEL_VIEW_PROJECTION;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_SELECTION_TIME;
import static org.gephi.viz.engine.util.gl.Constants.UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR;

import com.jogamp.opengl.GL2ES2;
import org.gephi.viz.engine.jogl.util.gl.GLShaderProgram;
import org.gephi.viz.engine.util.NumberUtils;
import org.gephi.viz.engine.util.gl.Constants;

public class EdgeCircleSelfLoopSelectionSelected {
    // Attributes 5
    // Index	0	    1	    2	        3	    4
    // Value	posX	posY    color	    size	nodeSize
    //
    public static final int VERTEX_FLOATS = 2;
    public static final int POSITION_FLOATS = 2;
    public static final int COLOR_FLOATS = 1;
    public static final int SIZE_FLOATS = 1;
    public static final int NODE_SIZE_FLOATS = 1;

    public static final int TOTAL_ATTRIBUTES_FLOATS
        = POSITION_FLOATS
        + COLOR_FLOATS
        + SIZE_FLOATS
        + NODE_SIZE_FLOATS;
    private GLShaderProgram program;

    private static final String SHADERS_ROOT = Constants.SHADERS_ROOT + "edge";
    private static final String SHADERS_NODE_CIRCLE_SOURCE = "selfloop_selected";

    public void initGLPrograms(GL2ES2 gl) {
        program = new GLShaderProgram(SHADERS_ROOT, SHADERS_NODE_CIRCLE_SOURCE, SHADERS_NODE_CIRCLE_SOURCE)
            .addUniformName(UNIFORM_NAME_MODEL_VIEW_PROJECTION)
            .addUniformName(UNIFORM_NAME_BACKGROUND_COLOR)
            .addUniformName(UNIFORM_NAME_COLOR_LIGHTEN_FACTOR)
            .addUniformName(UNIFORM_NAME_GLOBAL_TIME)
            .addUniformName(UNIFORM_NAME_SELECTION_TIME)
            .addUniformName(UNIFORM_NAME_EDGE_SCALE_MIN)
            .addUniformName(UNIFORM_NAME_EDGE_SCALE_MAX)
            .addUniformName(UNIFORM_NAME_MIN_WEIGHT)
            .addUniformName(UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR)
            .addAttribLocation(ATTRIB_NAME_VERT, SHADER_VERT_LOCATION)
            .addAttribLocation(ATTRIB_NAME_POSITION, SHADER_POSITION_LOCATION)
            .addAttribLocation(ATTRIB_NAME_COLOR, SHADER_COLOR_LOCATION)
            .addAttribLocation(ATTRIB_NAME_SIZE, SHADER_SIZE_LOCATION)
            .addAttribLocation(ATTRIB_NAME_SELFLOOP_NODE_SIZE, SHADER_SELFLOOP_NODE_SIZE_LOCATION)
            .init(gl);
    }

    public void useProgram(GL2ES2 gl, float[] mvpFloats, float[] backgroundColorFloats, float colorLightenFactor,
                           float globalTime, float selectionTime, float edgeScale, float minWeight, float maxWeight,
                           float edgeRescaleMin, float edgeRescaleMax) {
        program.use(gl);

        gl.glUniformMatrix4fv(program.getUniformLocation(UNIFORM_NAME_MODEL_VIEW_PROJECTION), 1, false, mvpFloats, 0);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_COLOR_LIGHTEN_FACTOR), colorLightenFactor);
        gl.glUniform4fv(program.getUniformLocation(UNIFORM_NAME_BACKGROUND_COLOR), 1, backgroundColorFloats, 0);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_GLOBAL_TIME), globalTime);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_SELECTION_TIME), selectionTime);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_EDGE_SCALE_MIN), edgeRescaleMin * edgeScale);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_EDGE_SCALE_MAX), edgeRescaleMax * edgeScale);
        gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_MIN_WEIGHT), minWeight);
        if (NumberUtils.equalsEpsilon(minWeight, maxWeight, 1e-3f)) {
            gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR), 1);
        } else {
            gl.glUniform1f(program.getUniformLocation(UNIFORM_NAME_WEIGHT_DIFFERENCE_DIVISOR), maxWeight - minWeight);
        }
    }
}

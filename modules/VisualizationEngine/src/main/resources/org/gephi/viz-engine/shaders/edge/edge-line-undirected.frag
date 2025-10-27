//#include "../common.frag.glsl"

//#include "common.edge.struct.glsl"
in VertexData vertexData;

out vec4 fragColor;

void main(void) {
    fragColor = vertexData.color;
}

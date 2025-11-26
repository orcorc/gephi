//#include "../common.frag.glsl"


in vec2 vLocal;
struct VertexData {
    vec4 color;
};
flat in VertexData vertexData;
out vec4 fragColor;

void main(void) {
    vec4 color = vertexData.color;
    fragColor = color;
}

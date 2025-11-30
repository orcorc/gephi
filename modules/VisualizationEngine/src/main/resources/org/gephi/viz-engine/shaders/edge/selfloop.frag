//#include "../common.frag.glsl"


in vec2 vLocal;
struct VertexData {
    vec4 color;
    float size;
};
flat in VertexData vertexData;
out vec4 fragColor;

void main(void) {
    vec4 color = vertexData.color;
    float size = vertexData.size;
    if (dot(vLocal, vLocal)<= .5) discard;
    fragColor =  color;
}

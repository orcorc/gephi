//#include "../common.vert.glsl"

in vec2 vert;
in vec2 position;
in vec4 elementColor;
in float size;
in float nodeSize;

uniform mat4 mvp;
struct VertexData {
    vec4 color;
};
flat out VertexData vertexData;
out vec2 vLocal;

void main() {
    vLocal = vert;

    vec2 instancePosition = nodeSize *10 * vert + position +size*0;
    //vec2 instancePosition = 100.f * vert + position;
    gl_Position = mvp * vec4(instancePosition, 0.0, 1.0);

    //bgra -> rgba because Java color is argb big-endian
    vec4 color = elementColor.bgra / 255.0;

    color.rgb = color.rgb;

    vertexData.color = color;
}

//#include "../common.frag.glsl"

//#include "common.node.frag.uniform.glsl"

//#include "common.node.struct.glsl"

in vec2 vLocal;

flat in VertexData vertexData;
out vec4 fragColor;

void main(void) {
    float r2 = dot(vLocal, vLocal);
    float t  = 1.0 - borderSize;// inner edge of border
    float t2 = t * t;

    vec4 color = vertexData.color;
    color.rgb = r2 < t2 ? color.rgb: color.rgb * nodeBorderDarkenFactor;

    fragColor = color;
}

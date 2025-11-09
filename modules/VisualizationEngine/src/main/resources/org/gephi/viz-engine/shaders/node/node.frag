//#include "../common.frag.glsl"

//#include "common.node.frag.uniform.glsl"

//#include "common.node.struct.glsl"

in vec2 vLocal;

flat in VertexData vertexData;
out vec4 fragColor;

void main(void) {
    float r2 = dot(vLocal, vLocal);
    float t  = 1.0 - borderSize; // inner edge of border
    float t2 = t * t;

    float mask = step(t2, r2);  // 0 inside, 1 in border

    vec3 base = vertexData.color.rgb;
    vec3 dark = base * nodeBorderDarkenFactor;

    vec3 rgb = mix(base, dark, mask);
    fragColor = vec4(rgb, vertexData.color.a);
}

//#include "../common.frag.glsl"

#define BORDER_SIZE 0.16
#define NODER_BORDER_DARKEN_FACTOR 0.498

//#include "common.node.struct.glsl"

in vec2 vLocal;

flat in VertexData vertexData;
out vec4 fragColor;

void main(void) {
    float r = length(vLocal);            // 0..1

    float t = 1.0 - BORDER_SIZE;          // inner edge of border
    float mask = step(t, r);             // 0 inside, 1 in border band

    vec3 base = vertexData.color.rgb;
    vec3 dark = base * NODER_BORDER_DARKEN_FACTOR;

    vec3 rgb = mix(base, dark, mask);
    fragColor = vec4(rgb, vertexData.color.a);
}

//#include "../common.frag.glsl"

#define BORDER_SIZE 0.16
#define NODER_BORDER_DARKEN_FACTOR 0.498

//#include "common.node.struct.glsl"

in vec2 vLocal;

flat in VertexData vertexData;
out vec4 fragColor;

void main(void) {
    float r2 = dot(vLocal, vLocal);
    float t  = 1.0 - BORDER_SIZE;// inner edge of border
    float t2 = t * t;

    float mask = step(t2, r2);// 0 inside, 1 in border

    vec3 base = vertexData.color.rgb;
    vec3 dark = base * NODER_BORDER_DARKEN_FACTOR;

    vec3 rgb = mix(base, dark, mask);
    fragColor = vec4(rgb, vertexData.color.a);
}

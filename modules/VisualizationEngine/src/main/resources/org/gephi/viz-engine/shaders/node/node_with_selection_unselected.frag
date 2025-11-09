//#include "../common.frag.glsl"

//#include "common.node.frag.glsl"

uniform vec4 backgroundColor;
uniform float colorLightenFactor;

//#include "../common.animation.glsl"

//#include "common.node.struct.glsl"

in vec2 vLocal;

flat in VertexData vertexData;
out vec4 fragColor;

void main(void) {
    float r2 = dot(vLocal, vLocal);
    float t  = 1.0 - BORDER_SIZE;// inner edge of border
    float t2 = t * t;

    float mask = step(t2, r2);// 0 inside, 1 in border

    vec4 color = vertexData.color;
    color.rgb = mix(color.rgb, color.rgb * NODER_BORDER_DARKEN_FACTOR, mask);

    // Animation:
    color.rgb = mix(color.rgb, backgroundColor.rgb, colorLightenFactor * animationCurve);

    fragColor = color;
}

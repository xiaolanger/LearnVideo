#extension GL_OES_EGL_image_external : require

precision mediump float;
varying vec2 coord;
uniform samplerExternalOES texture;

void main() {
    vec4 color = texture2D(texture, coord);
    float gray = (color.r + color.g + color.b) / 3.0;
    gl_FragColor = vec4(gray, gray, gray, 1.0);
}

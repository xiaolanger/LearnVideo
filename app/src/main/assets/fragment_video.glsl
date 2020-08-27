#extension GL_OES_EGL_image_external : require

precision mediump float;
varying vec2 coord;
uniform samplerExternalOES texture;

void main() {
    gl_FragColor = texture2D(texture, coord);
}

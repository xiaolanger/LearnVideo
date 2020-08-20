attribute vec4 vertexCoord;
attribute vec2 fragmentCoord;
varying vec2 coord;

void main() {
    gl_Position = vertexCoord;
    coord = fragmentCoord;
}
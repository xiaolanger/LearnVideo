attribute vec4 vertexCoord;
attribute vec2 fragmentCoord;
uniform mat4 matrix;
varying vec2 coord;

void main() {
    gl_Position = vertexCoord * matrix;
    coord = fragmentCoord;
}

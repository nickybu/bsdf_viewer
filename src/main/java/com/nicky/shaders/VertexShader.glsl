#version 410 core

// Input format: expects an array of floats
// When we fill buffer, we define buffer chunks to be processed by shader
// Starting from position 0, expect to receive a vector composed of (x,y,z)
layout (location=0) in vec3 position;
layout (location=1) in vec3 inColour;

out vec3 exColour; //output colour to fragment shader

uniform mat4 projectionMatrix;
uniform mat4 modelViewMatrix;

// Returns received position in output variable gl_Position
void main() {
    gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
    exColour = inColour;
}
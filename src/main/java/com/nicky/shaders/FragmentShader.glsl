#version 410 core

in vec3 exColour;
out vec4 fragColor;

void main() {
    // Set fixed colour for each fragment
    fragColor = vec4(exColour, 1.0);
}
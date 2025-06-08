#version 330 core

in vec4 vColor;
in vec2 vUV;

uniform sampler2D uTexture;
uniform bool uTextured;

out vec4 FragColor;

void main() {
    vec4 texColor = uTextured ? texture(uTexture, vUV) : vec4(1.0);
    FragColor = vColor * texColor;
}

/*
GLSL로 작성, OpenGL 전용언어
attribute로 속성을 정의함.
aPosition vec4 타입으로 4차원 벡터 타입인데, 각 정점의 위치 정보를 가지고 있음
TexCoord vec2타입으로 정점에 대응되는 텍스처 좌표를 나타냄
vTexCoord fragmentShader로 좌표를 보간해 전달
gl_Position 출력 위치
**/
attribute vec4 aPosition;
attribute vec2 aTexCoord;
varying vec2 vTexCoord;

void main() {
    gl_Position = aPosition;
    vTexCoord = aTexCoord;
}
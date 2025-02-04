/*
GL_OES_EGL_image_external 확장을 사용하도록 요구하는 것인데, 카메라 프리뷰, 비디오 스트림 등에서 가져온 텍스처를 사용할 수 있도록 해줌
precision는 정밀도를 설정하는데 mediump는 중간 정도의 정밀도를 사용함
varying 선언하여 버텍스 쉐이더에서 프래그먼트 쉐이더로 보간되어 전달 받은 변수/ 각 프래그먼트(화소)에 대해 정점 간의 값을 선형 보간하여 사용
vTexCoord를 사용해서 외부 텍스처에서 해당 픽셀의 색상을 샘플링함
uniform 은 외부 텍스처를 샘플링하기 위해 사용되는 텍스처 유니폼 변수임, CPU 쪽에서 쉐이더 프로그램에 설정됨 samplerExternalOES는 외부 텍스처를 샘플링할 때 사용하는 타입
color uTexture에서 vTexCoord 픽셀의 색상을 읽어옴 vec4 타입으로 반환(RGBA)
gl_FragColor 에서 최종 출력/ TODO 현재는 색상 반전으로 설정되어 있음
**/
#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTexCoord;
uniform samplerExternalOES uTexture;
void main() {
    vec4 color = texture2D(uTexture, vTexCoord);
    gl_FragColor = vec4(vec3(1.0) - color.rgb, color.a);
}
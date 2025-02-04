package kr.co.opengl

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.os.SystemClock
import androidx.annotation.RawRes
import kr.co.seedocs.open_gl.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * OpenGL ES 2.0로 외부 텍스처(카메라 프리뷰)를 렌더링하며 필터(색 반전)를 적용하는 렌더 스레드.
 * OpenGL 컨텍스트가 특정 스레드에 묶여 있기 때문에 전용 렌더링 스레드에서 수행하는 것이 일반적이라 스레드로 구성
 */
internal class GLRenderThread(
    private val surfaceTexture: SurfaceTexture,
    private var viewWidth: Int,
    private var viewHeight: Int,
    private val context: Context,
) : Thread() {

    /**
     * [Volatile] 애노테이션을 사용하여 여러 스레드에서 running 변수에 동시에 접근할 때의 동기화를 보장합니다.
     */
    @Volatile
    private var running = true

    private lateinit var eglDisplay: EGLDisplay
    private lateinit var eglContext: EGLContext
    private lateinit var eglSurface: EGLSurface

    private var program = 0
    private var positionHandle = 0
    private var texCoordHandle = 0
    private var textureUniformHandle = 0

    // 정점 데이터 //TODO 카메라로 받은 화면 전체를 필터 씌워줄거임
    // 각 정점은 X, Y, U, V 순서 (총 4개 좌표)
    private val vertexData = floatArrayOf(
        // X, Y,      U, V
        -1f, -1f,    0f, 1f,
        1f, -1f,    1f, 1f,
        -1f,  1f,    0f, 0f,
        1f,  1f,    1f, 0f
    )

    // 정점 데이터를 저장할 버퍼
    private lateinit var vertexBuffer: FloatBuffer

    //2x2 텍스처 (4 픽셀, ARGB)
    private val pixels = intArrayOf(
        0xffff0000.toInt(), 0xff00ff00.toInt(),
        0xff0000ff.toInt(), 0xffffffff.toInt()
    )
    private var textureId = 0


    override fun run() {
        initGL()
        initShader()
        initBuffers()
        textureId = createExternalTexture()

        GLES20.glViewport(0, 0, viewWidth, viewHeight)

        while (running) {
            surfaceTexture.updateTexImage()
            drawFrame()
            // 프레임 렌더 후 버퍼 교환
            EGL14.eglSwapBuffers(eglDisplay, eglSurface)

            // 약 60fps
            SystemClock.sleep(16)
        }

        deinitGL()
    }


    fun onSurfaceChanged(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
        GLES20.glViewport(0, 0, viewWidth, viewHeight)
    }

    fun requestExitAndWait() {
        running = false
        try {
            join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    /**
     * EGL 및 OpenGL ES 2.0 초기화 작업을 수행합니다.
     *
     * 이 메서드는 다음 단계를 수행합니다:
     * 1. 기본 EGL 디스플레이를 가져오고 초기화합니다.
     * 2. 렌더링에 필요한 EGLConfig를 선택합니다.
     * 3. OpenGL ES 2.0 컨텍스트를 생성합니다.
     * 4. [surfaceTexture]와 연동된 EGLSurface를 생성합니다.
     * 5. 현재 스레드에 EGLContext와 EGLSurface를 바인딩하여,
     *    이후의 OpenGL 명령들이 이 컨텍스트와 서피스를 대상으로 실행되도록 합니다.
     *
     * @see EGL14.eglGetDisplay
     * @see EGL14.eglInitialize
     * @see EGL14.eglChooseConfig
     * @see EGL14.eglCreateContext
     * @see EGL14.eglCreateWindowSurface
     * @see EGL14.eglMakeCurrent
     */
    private fun initGL() {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        val version = IntArray(2)
        EGL14.eglInitialize(eglDisplay, version, 0/*version 의 인덱스*/, version, 1)

        val configAttribs = intArrayOf(
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_DEPTH_SIZE, 8,
            EGL14.EGL_STENCIL_SIZE, 8,
            EGL14.EGL_NONE
        )

        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        EGL14.eglChooseConfig(eglDisplay, configAttribs, 0, configs, 0, 1, numConfigs, 0)

        val eglConfig = configs[0]
        val contextAttribs = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION,
            2,
            EGL14.EGL_NONE
        )
        eglContext =
            EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)

        val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)
        eglSurface =
            EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, surfaceTexture, surfaceAttribs, 0)
        EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
    }

    /**
     * EGL 관련 자원 해제
     */
    private fun deinitGL() {
        EGL14.eglMakeCurrent(
            eglDisplay,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_CONTEXT
        )
        EGL14.eglDestroySurface(eglDisplay, eglSurface)
        EGL14.eglDestroyContext(eglDisplay, eglContext)
        EGL14.eglTerminate(eglDisplay)
    }

    /**
     * 정점 데이터를 저장할 버퍼를 초기화합니다.
     *
     * [vertexData] 배열에 있는 각 정점의 데이터를
     * native order를 사용하는 직접 할당된 [ByteBuffer]를 통해 [FloatBuffer]로 변환하여 [vertexBuffer]에 저장합니다.
     *
     * 각 정점은 X, Y 좌표와 텍스처 좌표 U, V 값을 포함합니다.
     */
    private fun initBuffers() {
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(vertexData)
                position(0)
            }
    }

    /**
     * OpenGL ES 셰이더 프로그램을 초기화합니다.
     *
     * 이 메서드는 다음 단계를 수행합니다:
     * 1. 리소스(R.raw.vertex_shader, R.raw.fragment_shader)에 저장된 정점 및 프래그먼트 셰이더 코드를 로드합니다.
     * 2. 각각의 셰이더 코드를 컴파일하여 셰이더 객체를 생성합니다.
     * 3. 생성된 셰이더들을 하나의 프로그램으로 연결(link)합니다.
     * 4. 프로그램에 정의된 정점 속성(`aPosition`, `aTexCoord`)과 텍스처 uniform(`uTexture`)의 핸들을 가져옵니다.
     *
     * ```
     * 정점과 텍스처 참고 자료: https://developer.android.com/develop/ui/views/graphics/agsl/agsl-vs-glsl?hl=ko#coordinate_space
     * ```
     *
     * @see R.raw.vertex_shader
     * @see R.raw.fragment_shader
     */
    private fun initShader() {

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, R.raw.vertex_shader)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, R.raw.fragment_shader)

        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        textureUniformHandle = GLES20.glGetUniformLocation(program, "uTexture")
    }

    /**
     * 지정된 타입과 리소스 ID에 해당하는 셰이더 코드를 로드하고 컴파일합니다.
     *
     * GLSL로 이루어진 코드를 작성한 리소스 파일로부터 셰이더 소스 코드를 읽어 들여, 주어진 [type]에 해당하는 셰이더 객체를 생성하고 컴파일합니다.
     * 컴파일 실패 시 에러 로그를 출력한 후, [RuntimeException]을 발생시킵니다.
     *
     * res/raw에 GLSL코드가 작성되어 있습니다.
     *
     * @param type 셰이더 타입 (예: [GLES20.GL_VERTEX_SHADER] 또는 [GLES20.GL_FRAGMENT_SHADER])
     * @param shaderCode 리소스 ID에 해당하는 셰이더 코드 (예: [R.raw.vertex_shader])
     * @return 컴파일된 셰이더 객체의 ID
     * @see R.raw
     */
    private fun loadShader(type: Int, @RawRes shaderCode: Int): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(
            shader,
            context.resources.openRawResource(shaderCode).bufferedReader().use { it.readText() }
        )
        GLES20.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES20.glGetShaderiv(
            shader,
            GLES20.GL_COMPILE_STATUS,
            compiled,
            0
        )
        if (compiled[0] == 0) {
            GLES20.glDeleteShader(shader)
            throw RuntimeException("Could not compile shader $type: ${GLES20.glGetShaderInfoLog(shader)}")
        }
        return shader
    }

    /**
     *
     */
    private fun loadTexture(): Int {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        val texId = textures[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)//GL_NEAREST -> 가장 가까운 픽셀 색상 사용하는 방식, 텍스처 확대/축소 시 선명한 픽셀화 효과
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)


        // IntBuffer로 변환 후 텍스처 로드
        val pixelBuffer = ByteBuffer.allocateDirect(pixels.size * 4) //pixels 배열에 있는 모든 정수 데이터를 저장하기 위해 필요한 메모리 크기를 할당, Int -> 4byte, size * 4
            .order(ByteOrder.nativeOrder())//nativeOrder 플랫폼 기본 바이트 순서를 사용 하도록 설정, 성능 최적화 & 호환성
            .asIntBuffer()
        pixelBuffer.put(pixels).position(0)

        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, // 텍스처 대상 (2D 텍스처)
            0,                    // 레벨 0 (베이스 이미지 레벨)
            GLES20.GL_RGBA,       // 내부 포맷 (RGBA 포맷)
            2,                    // 텍스처 너비 (2픽셀)
            2,                    // 텍스처 높이 (2픽셀)
            0,                    // 경계(border): 항상 0이어야 함
            GLES20.GL_RGBA,       // 데이터 포맷 (픽셀 데이터의 포맷)
            GLES20.GL_UNSIGNED_BYTE, // 데이터 타입 (각 채널이 unsigned byte)
            pixelBuffer         // 픽셀 데이터가 저장된 버퍼
        )
        return texId
    }

    private fun drawFrame() {
        // 화면 클리어
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        GLES20.glUseProgram(program)

        // 정점 버퍼의 stride: 4개 float * 4바이트 = 16바이트, 포지션은 처음 2개 float
        vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(
            positionHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            4 * 4,
            vertexBuffer
        )

        // 텍스처 좌표: offset 2 float
        vertexBuffer.position(2)
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(
            texCoordHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            4 * 4,
            vertexBuffer
        )

        // 텍스처 활성화 및 바인딩
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUniform1i(textureUniformHandle, 0)


        // 속성 비활성화
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    private fun createExternalTexture(): Int {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        val texId = textures[0]
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texId)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        return texId
    }
}
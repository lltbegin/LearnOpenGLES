package com.example.learnopengles.learn.texture

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLES20.GL_TEXTURE0
import android.opengl.GLES20.GL_TEXTURE1
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class TextureFilter {
    private val vertexShaderCode =
        "precision mediump float;\n" +
                "attribute vec4 a_Position;\n" +
                "attribute vec2 a_textureCoordinate;\n" +
                "varying vec2 v_textureCoordinate;" +
                "void main() {" +
                "    v_textureCoordinate = a_textureCoordinate;\n" +
                "    gl_Position = a_Position;\n" +
                "}"

    private val fragmentShaderCode =
        "precision mediump float;\n" +
                "varying vec2 v_textureCoordinate;\n" +
                "uniform sampler2D u_texture;" +
                "void main() {\n" +
                "    vec4 resultColor = texture2D(u_texture, v_textureCoordinate);" +
                "    resultColor = vec4(resultColor.r*0.5,resultColor.g,resultColor.b,resultColor.a);" +
                "    gl_FragColor = resultColor;\n" +
//                "    gl_FragColor = texture2D(u_texture, v_textureCoordinate);\n" +
                "}"

    // 三角形顶点数据
    // 顶点坐标的坐标系是每个轴的范围都是-1~1，其实也可以超出-1和1，只不过超出就不在渲染的范围内了
    private val vertexData = floatArrayOf(-1f, -1f, -1f, 1f, 1f, 1f, -1f, -1f, 1f, 1f, 1f, -1f)

    // 纹理坐标的坐标原点在左下角，每个轴的范围是0~1，同样的也可以超出0和1，超出之后的表现会根据设置的纹理参数有所不同
    private val textureCoordinateData = floatArrayOf(0f, 1f, 0f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 1f, 1f)

    private val TEXTURE_COORDINATE_COMPONENT_COUNT = 2
    private lateinit var textureCoordinateDataBuffer: FloatBuffer

    // 每个顶点的成份数
    // The num of components of per vertex
    private val VERTEX_COMPONENT_COUNT = 2
    private var programId: Int = 0

    fun init() {
        // 创建GL程序
        programId = GLES20.glCreateProgram()

        // 加载、编译vertex shader和fragment shader
        val vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        val fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(vertexShader, vertexShaderCode)
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode)
        GLES20.glCompileShader(vertexShader)
        GLES20.glCompileShader(fragmentShader)

        // 将shader程序附着到GL程序上
        GLES20.glAttachShader(programId, vertexShader)
        GLES20.glAttachShader(programId, fragmentShader)

        // 链接GL程序
        GLES20.glLinkProgram(programId)

        // 将三角形顶点数据放入buffer中
        val buffer = ByteBuffer.allocateDirect(vertexData.size * java.lang.Float.SIZE)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        buffer.put(vertexData)
        buffer.position(0)

        // 应用GL程序
        GLES20.glUseProgram(programId)

        // 获取字段a_Position在shader中的位置
        val location = GLES20.glGetAttribLocation(programId, "a_Position")

        // 启动对应位置的参数
        GLES20.glEnableVertexAttribArray(location)

        // 指定a_Position所使用的顶点数据
        GLES20.glVertexAttribPointer(
            location,
            VERTEX_COMPONENT_COUNT,
            GLES20.GL_FLOAT,
            false,
            0,
            buffer
        )


        // 将纹理坐标数据放入buffer中
        // Put the texture coordinates into the textureCoordinateDataBuffer
        textureCoordinateDataBuffer =
            ByteBuffer.allocateDirect(textureCoordinateData.size * java.lang.Float.SIZE / 8)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        textureCoordinateDataBuffer.put(textureCoordinateData)
        textureCoordinateDataBuffer.position(0)

        // 获取字段a_textureCoordinate在shader中的位置
        // Get the location of a_textureCoordinate in the shader
        val aTextureCoordinateLocation =
            GLES20.glGetAttribLocation(programId, "a_textureCoordinate")

        // 启动对应位置的参数
        // Enable the parameter of the location
        GLES20.glEnableVertexAttribArray(aTextureCoordinateLocation)

        // 指定a_textureCoordinate所使用的顶点数据
        // Specify the data of a_textureCoordinate
        GLES20.glVertexAttribPointer(
            aTextureCoordinateLocation,
            TEXTURE_COORDINATE_COMPONENT_COUNT,
            GLES20.GL_FLOAT,
            false,
            0,
            textureCoordinateDataBuffer
        )

    }

    fun onDrawFrame(width: Int, height: Int) {
        // 设置清屏颜色
        // Set the color which the screen will be cleared to
        GLES20.glClearColor(0.9f, 0.9f, 0.9f, 1f)

        // 清屏
        // Clear the screen
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // 设置视口，这里设置为整个GLSurfaceView区域
        // Set the viewport to the full GLSurfaceView
        GLES20.glViewport(0, 0, width, height)

        // 调用draw方法用TRIANGLES的方式执行渲染，顶点数量为3个
        // Call the draw method with GL_TRIANGLES to render 3 vertices
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexData.size / VERTEX_COMPONENT_COUNT)
    }

    fun generateTexture(bitmap: Bitmap) {
        // 创建图片纹理
        val b = ByteBuffer.allocate(bitmap.width * bitmap.height * 4)
        bitmap.copyPixelsToBuffer(b)
        b.position(0)


//        创建好纹理之后，它还是空的
        val textures = IntArray(1)
        GLES20.glGenTextures(textures.size, textures, 0)
        val imageTexture = textures[0]
        GLES20.glActiveTexture(GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, imageTexture)
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_NEAREST
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )


//        通过glTexImage2D方法将上面得到的ByteBuffer加载到纹理中
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_RGBA,
            bitmap.width,
            bitmap.height,
            0,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            b
        )

        val uTextureLocation = GLES20.glGetAttribLocation(programId, "u_texture")
        GLES20.glUniform1i(uTextureLocation, 0)
    }

}
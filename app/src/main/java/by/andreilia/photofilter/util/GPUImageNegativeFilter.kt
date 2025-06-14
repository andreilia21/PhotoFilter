package by.andreilia.photofilter.util

import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter

class GPUImageNegativeFilter() : GPUImageFilter(
    NO_FILTER_VERTEX_SHADER, FRAGMENT_SHADER
) {

    companion object {
        private const val FRAGMENT_SHADER: String = "" +
            "varying highp vec2 textureCoordinate;\n" +
            "uniform sampler2D inputImageTexture;\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    lowp vec3 textureColor = 1.0 - texture2D(inputImageTexture, textureCoordinate).rgb;\n" +
            "    gl_FragColor = vec4(textureColor.r, textureColor.g, textureColor.b, 1.0);\n" +
            "}"
    }

}

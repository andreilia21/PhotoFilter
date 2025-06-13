package by.andreilia.photofilter.util

import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter

class GPUImageNegativeFilter() : GPUImageFilter(
    NO_FILTER_VERTEX_SHADER, COLOR_MATRIX_FRAGMENT_SHADER
) {

    companion object {
        private val COLOR_MATRIX_FRAGMENT_SHADER: String = """
varying highp vec2 textureCoordinate;

uniform sampler2D inputImageTexture;

void main()
{
    lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);
    
    gl_FragColor = 1.0 - textureColor;
}
""".trim()
    }
}

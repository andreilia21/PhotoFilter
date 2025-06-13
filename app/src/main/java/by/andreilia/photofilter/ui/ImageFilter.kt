package by.andreilia.photofilter.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.ColorMatrix
import by.andreilia.photofilter.R

@Stable
enum class ImageFilter(
    @field:StringRes
    val title: Int,
    val colorMatrix: ColorMatrix,
    val intensityAvailable: Boolean = true,
) {
    Original(
        title = R.string.filter_original,
        colorMatrix = ColorMatrix(),
        intensityAvailable = false
    ),
    Sepia(
        title = R.string.filter_sepia,
        colorMatrix = ColorMatrix(floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f,     0f,     0f,     1f, 0f
        )),
    ),
    Vignette(
        title = R.string.filter_vignette,
        colorMatrix = ColorMatrix(),
    ),
    Negative(
        title = R.string.filter_negative,
        colorMatrix = ColorMatrix(),
    ),
}

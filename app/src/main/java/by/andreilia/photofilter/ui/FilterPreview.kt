package by.andreilia.photofilter.ui

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.ImageBitmap

@Stable
data class FilterPreview(
    val filter: ImageFilter,
    val bitmap: ImageBitmap
)
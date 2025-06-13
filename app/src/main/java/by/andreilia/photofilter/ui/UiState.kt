package by.andreilia.photofilter.ui

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.ImageBitmap

sealed interface UiState {
    data object NoPhotoSelected : UiState

    @Stable
    data class PhotoSelected(
        val imageBitmap: ImageBitmap,
        val filter: ImageFilter,
        val filters: List<ImageFilter>,
        val intensity: Float = 0.5f
    ) : UiState
}
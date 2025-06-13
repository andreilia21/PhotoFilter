package by.andreilia.photofilter.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val filters = ImageFilter.entries.toList()

    private val selectedPhoto = MutableStateFlow<ImageBitmap?>(null)

    private val selectedFilter = MutableStateFlow(ImageFilter.Original)

    private val intensity = savedStateHandle.getStateFlow("intensity", 0.5f)

    val state: StateFlow<UiState> = selectedPhoto
        .combine(selectedFilter) { bitmap, filter -> bitmap to filter }
        .combine(intensity) { pair, intensity -> pair to intensity }
        .map {
            val (pair, intensity) = it
            val (bitmap, filter) = pair

            if (bitmap == null) {
                UiState.NoPhotoSelected
            } else {
                UiState.PhotoSelected(
                    imageBitmap = bitmap,
                    filter = filter,
                    filters = filters,
                    intensity = intensity
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.NoPhotoSelected
        )


    fun selectPhoto(uri: Uri) {
        viewModelScope.launch {
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            application.contentResolver.takePersistableUriPermission(uri, flag)
            selectedPhoto.emit(application.loadBitmap(uri)?.asImageBitmap())
        }
    }

    fun setIntensity(intensity: Float) {
        savedStateHandle["intensity"] = intensity
    }
}

private fun Context.loadBitmap(uri: Uri): Bitmap? {
    // Check the API level to use the appropriate method for decoding the Bitmap
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        // For Android P (API level 28) and higher, use ImageDecoder to decode the Bitmap
        val source = ImageDecoder.createSource(contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    } else {
        // For versions prior to Android P, use BitmapFactory to decode the Bitmap
        val bitmap = contentResolver.openInputStream(uri)?.use { stream ->
            Bitmap.createBitmap(BitmapFactory.decodeStream(stream))
        }
        bitmap
    }
}


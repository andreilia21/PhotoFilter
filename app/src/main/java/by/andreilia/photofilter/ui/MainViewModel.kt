package by.andreilia.photofilter.ui

import android.R.attr.bitmap
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import by.andreilia.photofilter.util.ImageUtils.downSize
import jp.co.cyberagent.android.gpuimage.GPUImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


class MainViewModel(
    private val application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val filters = ImageFilter.entries.toList()

    private val selectedPhoto = MutableStateFlow<ImageBitmap?>(null)

    private val selectedFilter = MutableStateFlow(ImageFilter.Original)

    private val intensity = savedStateHandle.getStateFlow("intensity", 0.5f)

    private val gpuImage = GPUImage(application)

    private val previews = selectedPhoto.map { bitmap ->
        if (bitmap == null) {
            emptyList()
        } else {
            filters.map {
                FilterPreview(
                    filter = it,
                    bitmap = it.applyTo(gpuImage, bitmap.downscaled(), 0.5f)
                )
            }
        }
    }

    private fun ImageBitmap.downscaled(): ImageBitmap {
        return asAndroidBitmap().downSize().asImageBitmap()
    }

    val state: StateFlow<UiState> = selectedPhoto
        .combine(selectedFilter) { bitmap, filter -> bitmap to filter }
        .combine(intensity) { pair, intensity -> pair to intensity }
        .combine(previews) { pair, previews -> pair to previews }
        .map {
            val (pairToIntensity, previews) = it
            val (bitmapToFilter, intensity) = pairToIntensity
            val (bitmap, filter) = bitmapToFilter

            if (bitmap == null) {
                UiState.NoPhotoSelected
            } else {
                UiState.PhotoSelected(
                    imageBitmap = filter.applyTo(gpuImage, bitmap, intensity),
                    filter = filter,
                    previews = previews,
                    intensity = intensity
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.NoPhotoSelected
        )

    fun selectFilter(filter: ImageFilter) {
        viewModelScope.launch {
            selectedFilter.emit(filter)
        }
    }

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

    suspend fun saveImage() = withContext(Dispatchers.IO) {
        val image = selectedPhoto.value ?: return@withContext
        val filter = selectedFilter.value
        val intensity = intensity.value

        val result = filter.applyTo(gpuImage, image, intensity).asAndroidBitmap()
        val fileName = "IMG_${System.currentTimeMillis()}.png"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, fileName)
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            }

            val contentResolver = application.contentResolver
            val uri: Uri? =
                contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                val fileDescriptor = contentResolver.openFileDescriptor(it, "w")?.fileDescriptor

                fileDescriptor?.let { descriptor ->
                    val outputStream = FileOutputStream(descriptor)
                    result.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.close()
                }
            }
        } else {
            val sd = Environment.getExternalStorageDirectory()
            val dest = File(sd, fileName)

            val bitmap = result
            val out = FileOutputStream(dest)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.close()
        }
    }
}

private fun Context.loadBitmap(uri: Uri): Bitmap? {
    // Check the API level to use the appropriate method for decoding the Bitmap
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        // For Android P (API level 28) and higher, use ImageDecoder to decode the Bitmap
        val source = ImageDecoder.createSource(contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, info, source ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = true
        }
    } else {
        // For versions prior to Android P, use BitmapFactory to decode the Bitmap
        val bitmap = contentResolver.openInputStream(uri)?.use { stream ->
            Bitmap.createBitmap(BitmapFactory.decodeStream(stream))
        }
        bitmap
    }
}


package by.andreilia.photofilter.ui

import android.graphics.PointF
import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import by.andreilia.photofilter.R
import by.andreilia.photofilter.util.GPUImageNegativeFilter
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorMatrixFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSepiaToneFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageVignetteFilter


@Stable
enum class ImageFilter(
    @field:StringRes
    val title: Int,
    val intensityAvailable: Boolean = true,
) {
    Original(
        title = R.string.filter_original,
        intensityAvailable = false
    ) {
        override fun applyTo(
            gpuImage: GPUImage,
            bitmap: ImageBitmap,
            intensity: Float
        ): ImageBitmap {
            return bitmap
        }
    },
    Sepia(
        title = R.string.filter_sepia,
    ) {
        override fun applyTo(
            gpuImage: GPUImage,
            bitmap: ImageBitmap,
            intensity: Float
        ): ImageBitmap {
            gpuImage.setImage(bitmap.asAndroidBitmap())
            gpuImage.setFilter(GPUImageSepiaToneFilter(intensity))
            return gpuImage.bitmapWithFilterApplied.asImageBitmap()
        }
    },
    Vignette(
        title = R.string.filter_vignette,
    ) {
        override fun applyTo(
            gpuImage: GPUImage,
            bitmap: ImageBitmap,
            intensity: Float
        ): ImageBitmap {
            gpuImage.setImage(bitmap.asAndroidBitmap())
            gpuImage.setFilter(
                GPUImageVignetteFilter(
                    PointF(
                        0.5f,
                        0.5f
                    ),
                    FloatArray(3),
                    intensity * 0.5f,
                    0.7f
                )
            )
            return gpuImage.bitmapWithFilterApplied.asImageBitmap()
        }
    },
    Negative(
        title = R.string.filter_negative,
        intensityAvailable = false
    ) {
        override fun applyTo(
            gpuImage: GPUImage,
            bitmap: ImageBitmap,
            intensity: Float
        ): ImageBitmap {
            gpuImage.setImage(bitmap.asAndroidBitmap())
            gpuImage.setFilter(GPUImageNegativeFilter())
            return gpuImage.bitmapWithFilterApplied.asImageBitmap()
        }
    },
    OldTimes(
        title = R.string.filter_old_times,
        intensityAvailable = true
    ) {
        override fun applyTo(
            gpuImage: GPUImage,
            bitmap: ImageBitmap,
            intensity: Float
        ): ImageBitmap {
            gpuImage.setImage(bitmap.asAndroidBitmap())
            gpuImage.setFilter(
                GPUImageColorMatrixFilter(
                    intensity,
                    floatArrayOf(
                        1.0f, 0.05f, 0.0f, 0.0f,
                        -0.2f, 1.1f, -0.2f, 0.11f,
                        0.2f, 0.0f, 1.0f, 0.0f,
                        0.0f, 0.0f, 0.0f, 1.0f
                    )
                )
            )
            return gpuImage.bitmapWithFilterApplied.asImageBitmap()
        }
    },
    Milk(
        title = R.string.filter_milk,
        intensityAvailable = true
    ) {
        override fun applyTo(
            gpuImage: GPUImage,
            bitmap: ImageBitmap,
            intensity: Float
        ): ImageBitmap {
            gpuImage.setImage(bitmap.asAndroidBitmap())
            gpuImage.setFilter(
                GPUImageColorMatrixFilter(
                    intensity,
                    floatArrayOf(
                        0.0f, 1.0f, 0.0f, 0.0f,
                        0.0f, 1.0f, 0.0f, 0.0f,
                        0.0f, 0.64f, 0.5f, 0.0f,
                        0.0f, 0.0f, 0.0f, 1.0f
                    )
                )
            )
            return gpuImage.bitmapWithFilterApplied.asImageBitmap()
        }
    },
    ;

    abstract fun applyTo(gpuImage: GPUImage, bitmap: ImageBitmap, intensity: Float): ImageBitmap
}


package by.andreilia.photofilter.ui

import android.graphics.Bitmap
import android.graphics.PointF
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
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
            gpuImage.setFilter(GPUImageVignetteFilter(
                PointF(
                    0.5f,
                    0.5f
                ),
                FloatArray(3),
                intensity * 0.5f,
                0.7f
            ))
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
    }
    ;

    abstract fun applyTo(gpuImage: GPUImage, bitmap: ImageBitmap, intensity: Float): ImageBitmap
}

private fun ImageBitmapConfig.toBitmapConfig(): Bitmap.Config {
    return when (this) {
        ImageBitmapConfig.Argb8888 -> Bitmap.Config.ARGB_8888
        ImageBitmapConfig.Alpha8 -> Bitmap.Config.ALPHA_8
        ImageBitmapConfig.Rgb565 -> Bitmap.Config.RGB_565
        ImageBitmapConfig.F16 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Bitmap.Config.RGBA_F16
        } else {
            Bitmap.Config.ARGB_8888
        }

        ImageBitmapConfig.Gpu -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Bitmap.Config.HARDWARE
        } else {
            Bitmap.Config.ARGB_8888
        }

        else -> Bitmap.Config.ARGB_8888
    }
}
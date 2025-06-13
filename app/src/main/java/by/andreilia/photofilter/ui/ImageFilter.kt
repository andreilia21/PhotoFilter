package by.andreilia.photofilter.ui

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import androidx.core.graphics.get
import androidx.core.graphics.set
import by.andreilia.photofilter.R


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
            bitmap: ImageBitmap,
            intensity: Float
        ): ImageBitmap {
            val depth = 20

            val width: Int = bitmap.width
            val height: Int = bitmap.height
            var red: Int
            var green: Int
            var blue: Int
            var grey: Int
            val pixels = IntArray(width * height)
            bitmap.readPixels(pixels)

            for (i in pixels.indices) {
                red = Color.red(pixels[i])
                green = Color.green(pixels[i])
                blue = Color.blue(pixels[i])

                grey = (red + green + blue) / 3
                blue = grey
                green = blue
                red = green
                red = red + (depth * 2)
                green += depth

                if (red > 255) red = 255
                if (green > 255) green = 255
                if (blue > 255) blue = 255

                blue -= (intensity * 255).toInt()

                if (blue > 255) blue = 255
                if (blue < 0) blue = 0

                pixels[i] = Color.argb(Color.alpha(pixels[i]), red, green, blue)
            }

            val bitmapOut = createBitmap(width, height, config = bitmap.config.toBitmapConfig())
            bitmapOut.setPixels(pixels, 0, width, 0, 0, width, height)

            return bitmapOut.asImageBitmap()
        }
    },
    Vignette(
        title = R.string.filter_vignette,
    ) {
        override fun applyTo(
            bitmap: ImageBitmap,
            intensity: Float
        ): ImageBitmap {
            val clampedIntensity = intensity.coerceIn(0.0f, 1.0f)

            val width = bitmap.width
            val height = bitmap.height
            val vignetteBitmap = createBitmap(width, height, bitmap.config.toBitmapConfig())

            val centerX = width / 2.0
            val centerY = height / 2.0
            val maxDistanceSquared = centerX * centerX + centerY * centerY

            val bitmapIn = bitmap.asAndroidBitmap()
            for (x in 0 until width) {
                for (y in 0 until height) {
                    val pixelColor = bitmapIn[x, y]

                    val dx = (x - centerX)
                    val dy = (y - centerY)
                    val distanceSquared = dx * dx + dy * dy

                    val distanceFactor = distanceSquared / maxDistanceSquared

                    val vignetteScale = 1 - (clampedIntensity * distanceFactor)

                    val red = (Color.red(pixelColor) * vignetteScale).toInt().coerceIn(0, 255)
                    val green = (Color.green(pixelColor) * vignetteScale).toInt().coerceIn(0, 255)
                    val blue = (Color.blue(pixelColor) * vignetteScale).toInt().coerceIn(0, 255)
                    val alpha = Color.alpha(pixelColor)

                    vignetteBitmap[x, y] = Color.argb(alpha, red, green, blue)
                }
            }

            return vignetteBitmap.asImageBitmap()
        }
    },
    Negative(
        title = R.string.filter_negative,
        intensityAvailable = false
    ) {
        override fun applyTo(
            bitmap: ImageBitmap,
            intensity: Float
        ): ImageBitmap {
            val width = bitmap.width
            val height = bitmap.height

            val resultBitmap = createBitmap(width, height, bitmap.config.toBitmapConfig())
            val bitmapIn = bitmap.asAndroidBitmap()

            for (x in 0 until width) {
                for (y in 0 until height) {
                    val pixelColor = bitmapIn[x, y]

                    val a = Color.alpha(pixelColor)
                    val r = Color.red(pixelColor)
                    val g = Color.green(pixelColor)
                    val b = Color.blue(pixelColor)

                    resultBitmap[x, y] = Color.argb(a, 255 - r, 255 - g, 255 - b)
                }
            }

            return resultBitmap.asImageBitmap()
        }
    }
    ;
    abstract fun applyTo(bitmap: ImageBitmap, intensity: Float): ImageBitmap
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
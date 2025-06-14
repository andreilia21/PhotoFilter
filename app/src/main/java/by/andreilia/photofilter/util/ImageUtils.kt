package by.andreilia.photofilter.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import java.io.ByteArrayOutputStream
import java.io.IOException
import androidx.core.graphics.scale
import androidx.core.graphics.createBitmap

object ImageUtils {

    fun Drawable.toByteArray(): ByteArray {
        return convertDrawableToByteArray(this)
    }

    fun Drawable.toDownSizedByteArray(): ByteArray {
        return resizeAndConvertDrawable(this)
    }

    fun ByteArray.toBitmap(): Bitmap {
        return convertByteArrayToBitmap(this)
    }

    fun Bitmap.downSize(): Bitmap {
        return downsizeBitmap(this)
    }

    fun Bitmap.toByteArray(): ByteArray {
        return convertBitmapToByteArray(this)
    }

    private fun downsizeBitmap(fullBitmap: Bitmap): Bitmap {
        var width = fullBitmap.width
        var height = fullBitmap.height
        val needsDownsize = bitmapNeedsDownsize(fullBitmap)
        if (!needsDownsize) {
            return fullBitmap
        }
        var bitmap = fullBitmap
        while (bitmapNeedsDownsize(bitmap)) {
            bitmap = convertByteArrayToBitmap(
                getDownsizedImageBytes(
                    bitmap,
                    2.let { width /= it; width },
                    2.let { height /= it; height })
            )
        }
        return bitmap
    }

    private fun bitmapNeedsDownsize(bitmap: Bitmap): Boolean {
        return bitmap.byteCount > 800 * 1024
    }

    private fun getDownsizedImageBytes(fullBitmap: Bitmap?, scaleWidth: Int, scaleHeight: Int): ByteArray {
        val scaledBitmap = fullBitmap!!.scale(scaleWidth, scaleHeight)
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        return outputStream.toByteArray()
    }

    fun getByteArrayFromUri(resolver: ContentResolver, uri: Uri): ByteArray {
        return convertBitmapToByteArray(requireNotNull(getBitmapFromUri(resolver, uri)))
    }

    fun getBitmapFromUri(contentResolver: ContentResolver, uri: Uri): Bitmap? {
        return try {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val width = if (!drawable.bounds.isEmpty) drawable
            .bounds.width() else drawable.intrinsicWidth
        val height = if (!drawable.bounds.isEmpty) drawable
            .bounds.height() else drawable.intrinsicHeight
        val bitmap = createBitmap(if (width <= 0) 1 else width, if (height <= 0) 1 else height)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun resizeAndConvertDrawable(drawable: Drawable): ByteArray {
        return downsizeBitmap(getBitmapFromDrawable(drawable)).toByteArray()
    }

    private fun convertDrawableToByteArray(drawable: Drawable): ByteArray {
        return convertBitmapToByteArray(getBitmapFromDrawable(drawable))
    }

    private fun convertByteArrayToBitmap(image: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(image, 0, image.size)
    }
}

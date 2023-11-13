package xyz.teamgravity.landmarkrecognizer

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class LandmarkImageAnalyzer(
    private val classifier: LandmarkClassifier
) : ImageAnalysis.Analyzer {

    private companion object {
        const val FRAME_SKIP_COUNT = 60
        const val IMAGE_SIZE = 321
    }

    private var frameSkipCounter = 0
    private var onResult: ((List<LandmarkClassificationModel>) -> Unit)? = null

    private fun process(bitmap: Bitmap): Bitmap {
        if (IMAGE_SIZE > bitmap.width || IMAGE_SIZE > bitmap.height) throw IllegalArgumentException()
        val xStart = (bitmap.width - IMAGE_SIZE) / 2
        val yStart = (bitmap.height - IMAGE_SIZE) / 2
        if (xStart < 0 || yStart < 0) throw IllegalArgumentException()
        return Bitmap.createBitmap(bitmap, xStart, yStart, IMAGE_SIZE, IMAGE_SIZE)
    }

    ///////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////

    override fun analyze(image: ImageProxy) {
        image.use { proxy ->
            frameSkipCounter++
            if (frameSkipCounter >= FRAME_SKIP_COUNT) {
                frameSkipCounter = 0
                val bitmap = process(proxy.toBitmap())
                val result = classifier.classify(bitmap, proxy.imageInfo.rotationDegrees)
                onResult?.invoke(result)
            }
        }
    }

    fun setOnResult(onResult: (List<LandmarkClassificationModel>) -> Unit) {
        this.onResult = onResult
    }
}
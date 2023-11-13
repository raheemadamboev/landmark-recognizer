package xyz.teamgravity.landmarkrecognizer

import android.content.Context
import android.graphics.Bitmap
import android.view.Surface
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier

class LandmarkClassifier(
    private val context: Context
) {

    private companion object {
        const val THRESHOLD = 0.5F
        const val MAX_RESULT = 3
        const val MODEL_PATH = "model/landmark-recognizer.tflite"
    }

    private val classifier: ImageClassifier by lazy {
        val base = BaseOptions.builder()
            .setNumThreads(2)
            .build()
        val options = ImageClassifier.ImageClassifierOptions.builder()
            .setBaseOptions(base)
            .setScoreThreshold(THRESHOLD)
            .setMaxResults(MAX_RESULT)
            .build()
        return@lazy ImageClassifier.createFromFileAndOptions(context, MODEL_PATH, options)
    }

    private fun getOrientationFromRotation(rotation: Int): ImageProcessingOptions.Orientation {
        return when (rotation) {
            Surface.ROTATION_90 -> ImageProcessingOptions.Orientation.TOP_LEFT
            Surface.ROTATION_180 -> ImageProcessingOptions.Orientation.RIGHT_BOTTOM
            Surface.ROTATION_270 -> ImageProcessingOptions.Orientation.BOTTOM_RIGHT
            else -> ImageProcessingOptions.Orientation.RIGHT_TOP
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////

    fun classify(bitmap: Bitmap, rotation: Int): List<LandmarkClassificationModel> {
        val processor = ImageProcessor.Builder().build()
        val options = ImageProcessingOptions.builder()
            .setOrientation(getOrientationFromRotation(rotation))
            .build()
        val image = processor.process(TensorImage.fromBitmap(bitmap))

        return classifier.classify(image, options)?.flatMap { classifications ->
            classifications.categories.map { category ->
                LandmarkClassificationModel(
                    name = category.displayName,
                    score = category.score
                )
            }
        }?.distinctBy { it.name } ?: emptyList()
    }
}
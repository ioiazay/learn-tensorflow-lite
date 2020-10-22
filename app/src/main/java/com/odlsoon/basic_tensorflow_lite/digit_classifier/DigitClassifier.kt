package com.odlsoon.basic_tensorflow_lite.digit_classifier

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks.call
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DigitClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    var isInitialized = false
    private val executorService: ExecutorService = Executors.newCachedThreadPool()

    private var inputImageWidth: Int = 0
    private var inputImageHeight: Int = 0
    private var modelInputSize: Int = 0

    private val modelName = "mnist.tflite"

    fun initialize(): Task<Void> {
        return call(
            executorService,
            Callable<Void> {
                initializeInterpreter()
                null
            }
        )
    }

    private fun initializeInterpreter() {
        val assetManager = context.assets
        val model = loadModelFile(assetManager, modelName)
        val options = Interpreter.Options()
        options.setUseNNAPI(true)
        val interpreter = Interpreter(model, options)

        val inputShape = interpreter.getInputTensor(0).shape()
        inputImageWidth = inputShape[1]
        inputImageHeight = inputShape[2]
        modelInputSize = FLOAT_TYPE_SIZE * inputImageWidth * inputImageHeight * PIXEL_SIZE

        this.interpreter = interpreter
        isInitialized = true
    }

    private fun loadModelFile(assetManager: AssetManager, filename: String): ByteBuffer {
        val fileDescriptor = assetManager.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun classify(bitmap: Bitmap): String {
        check(isInitialized) { "TF Lite Interpreter is not initialized yet." }

        val resizedImage = Bitmap.createScaledBitmap(
            bitmap,
            inputImageWidth,
            inputImageHeight,
            true
        )

        val byteBuffer = convertBitmapToByteBuffer(resizedImage)
        val output = Array(1) { FloatArray(OUTPUT_CLASSES_COUNT) }

        interpreter?.run(byteBuffer, output)

        val result = output[0]
        val maxIndex = result.indices.maxBy { result[it] } ?: -1

        return "Hasil Prediksi: %d".format(maxIndex, result[maxIndex])
    }

    fun classifyAsync(bitmap: Bitmap): Task<String> {
        return call(executorService, Callable { classify(bitmap) })
    }

    fun close() {
        call(
            executorService,
            Callable<String> {
                interpreter?.close()
                null
            }
        )
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(inputImageWidth * inputImageHeight)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in pixels) {
            val r = (pixelValue shr 16 and 0xFF)
            val g = (pixelValue shr 8 and 0xFF)
            val b = (pixelValue and 0xFF)

            val normalizedPixelValue = (r + g + b) / 3.0f / 255.0f
            byteBuffer.putFloat(normalizedPixelValue)
        }

        return byteBuffer
    }

    companion object {
        private const val FLOAT_TYPE_SIZE = 4
        private const val PIXEL_SIZE = 1
        private const val OUTPUT_CLASSES_COUNT = 10
    }
}
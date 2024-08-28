package com.yechy.example.image

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.yechy.example.R
import com.yechy.example.utils.ThreadHelper
import com.yechy.example.utils.ThreadHelper.Task
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder


/**
 *
 * Created by cloud on 2024/8/6.
 */
class ImageActivity: AppCompatActivity() {

    companion object {

        const val TAG = "ImageActivity"
        fun actionStart(context: Context) {
            val intent = Intent(context, ImageActivity::class.java)
            context.startActivity(intent)
        }

        private const val INPUT_WIDTH = 512
        private const val INPUT_HEIGHT = 512
    }

    private lateinit var selectBtn: Button
    private lateinit var previewIv: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var infoTv: TextView
    private lateinit var spinner: Spinner

    private lateinit var interpreter: Interpreter

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        previewIv.setImageBitmap(null)
        infoTv.text = "耗时："
        uri?.let {
            progressBar.visibility = View.VISIBLE
            val startTime = System.currentTimeMillis()
            ThreadHelper.executeByCpu(object : Task<Bitmap?>() {
                override fun doInBackground(): Bitmap? {
                    var inputStream: InputStream? = null
                    try {
                        inputStream = contentResolver.openInputStream(it)
                        val options = BitmapFactory.Options()
                        options.inJustDecodeBounds = true
                        val bitmap = BitmapFactory.decodeStream(inputStream, null, null)

                        Log.d(TAG, "imageWidth: ${bitmap?.width}, imageHeight: ${bitmap?.height}, bitmap: ${bitmap?.byteCount}")
                        bitmap?.let {
                            val result = makeHighBrightnessPixelsTransparent(it, 192)
                            return result
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        inputStream?.close()
                    }
                    return null
                }

                override fun onCancel() {

                }

                override fun onFail(t: Throwable?) {
                    progressBar.visibility = View.GONE
                }

                override fun onSuccess(result: Bitmap?) {
                    val endTime = System.currentTimeMillis()
                    infoTv.text = "耗时：${endTime - startTime}ms"
                    progressBar.visibility = View.GONE
                    result?.let {
                        previewIv.setImageBitmap(result)
                    }
                }
            })

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        selectBtn = findViewById(R.id.btn_select_image)
        previewIv = findViewById(R.id.iv_preview)
        progressBar = findViewById(R.id.progress_indicator)
        progressBar.visibility = View.GONE
        infoTv = findViewById(R.id.tv_info)
        spinner = findViewById(R.id.spinner)

        selectBtn.setOnClickListener {
            pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        val byteBuffer = FileUtil.loadMappedFile(this, "deeplabv3.tflite")
        val options = Interpreter.Options()
        interpreter = Interpreter(byteBuffer, options)
    }

    fun removeBackground(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_WIDTH, INPUT_HEIGHT, false)

        val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)
        val outputBuffer = ByteBuffer.allocateDirect(1 * INPUT_WIDTH * INPUT_HEIGHT * 1 * 4)
        outputBuffer.order(ByteOrder.nativeOrder())

        interpreter.run(inputBuffer, outputBuffer)

        outputBuffer.rewind()
        val outputBitmap = Bitmap.createBitmap(INPUT_WIDTH, INPUT_HEIGHT, Bitmap.Config.ARGB_8888)
        convertByteBufferToBitmap(outputBuffer, outputBitmap)
        return Bitmap.createScaledBitmap(outputBitmap, width, height, false)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(1 * INPUT_WIDTH * INPUT_HEIGHT * 3 * 4)
        buffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(INPUT_WIDTH * INPUT_HEIGHT)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        for (pixel in intValues) {
            buffer.putFloat(((pixel shr 16) and 0xFF) / 255.0f)
            buffer.putFloat(((pixel shr 8) and 0xFF) / 255.0f)
            buffer.putFloat((pixel and 0xFF) / 255.0f)
        }
        buffer.rewind()
        return buffer
    }

    private fun convertByteBufferToBitmap(buffer: ByteBuffer, bitmap: Bitmap) {
        buffer.rewind()
        val pixels = IntArray(INPUT_WIDTH * INPUT_HEIGHT)
        for (i in pixels.indices) {
            val value = buffer.getFloat()
            pixels[i] = if (value > 0.5f) Color.TRANSPARENT else Color.BLACK
        }
        bitmap.setPixels(pixels, 0, INPUT_WIDTH, 0, 0, INPUT_WIDTH, INPUT_HEIGHT)
    }

    private fun makeHighBrightnessPixelsTransparent(
        bitmap: Bitmap,
        brightnessThreshold: Int
    ): Bitmap {
        val width: Int = bitmap.getWidth()
        val height: Int = bitmap.getHeight()
        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)


        // 用于存储像素的RGB分量和亮度
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)


        // 遍历像素数组
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val red = Color.red(pixel)
            val green = Color.green(pixel)
            val blue = Color.blue(pixel)

            // 计算亮度
            val brightness = (0.299 * red + 0.587 * green + 0.114 * blue).toInt()

            // 应用阈值处理
            if (brightness > brightnessThreshold) {
                pixels[i] = Color.TRANSPARENT
            }
        }


        // 将处理后的像素数组设置回Bitmap
        resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        return resultBitmap
    }

    private fun makeHighBrightnessPixelsTransparentEfficiently(
        bitmap: Bitmap,
        brightnessThreshold: Int
    ): Bitmap {
        // 创建一个可变的Bitmap
        val resultBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        // 创建一个新的画布
        val canvas = Canvas(resultBitmap)
        val paint = Paint()

        // 使用PorterDuff.Mode.MULTIPLY模式
        paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_IN))

        // 创建一个颜色滤镜，用于将低亮度像素变为透明
        val colorMatrix = ColorMatrix()
        colorMatrix.setScale(0.299f, 0.587f, 0.114f, 1.0f) // 计算亮度

        val alphaMatrix = ColorMatrix()
        alphaMatrix.set(
            floatArrayOf(
                1f,
                0f,
                0f,
                0f,
                0f,
                0f,
                1f,
                0f,
                0f,
                0f,
                0f,
                0f,
                1f,
                0f,
                0f,
                -brightnessThreshold / 255f,
                -brightnessThreshold / 255f,
                -brightnessThreshold / 255f,
                1f,
                0f
            )
        )

        colorMatrix.postConcat(alphaMatrix)
        paint.setColorFilter(ColorMatrixColorFilter(colorMatrix))

        // 绘制Bitmap
        canvas.drawBitmap(resultBitmap, 0f, 0f, paint)

        return resultBitmap
    }


    fun Bitmap.rgbArray(): Array<Array<FloatArray>> {
        val pixelsValues = IntArray(this.width * this.height)
        this.getPixels(pixelsValues, 0, this.width, 0, 0, this.width, this.height)
        val result = Array(this.height) { Array(this.width) { FloatArray(3) } }

        var pixel = 0
        for (y in 0 until this.height) {
            for (x in 0 until this.width) {
                val value = pixelsValues[pixel++]
                result[y][x][0] = ((value shr 16 and 0xFF).toFloat())//r
                result[y][x][1] = ((value shr 8 and 0xFF).toFloat())//g
                result[y][x][2] = ((value and 0xFF).toFloat())//b
            }
        }
        return result
    }

    private fun rgbToByteBuffer(array: Array<Array<FloatArray>>, width: Int, height: Int, normalizeOp: ((Float) -> Float) = { it }): ByteBuffer {
        val inputImage = ByteBuffer.allocateDirect(1 * width * height * 3 * 4)
        inputImage.order(ByteOrder.nativeOrder()).rewind()
        for (y in 0 until height) {
            for (x in 0 until width) {
                for (z in listOf(0, 1, 2)) {
                    val value = array[y][x][z]
                    inputImage.putFloat(normalizeOp(value))
                }
            }
        }
        inputImage.rewind()
        return inputImage
    }
}
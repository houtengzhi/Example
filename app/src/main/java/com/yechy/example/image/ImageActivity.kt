package com.yechy.example.image

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.yechy.example.R

/**
 *
 * Created by cloud on 2024/8/6.
 */
class ImageActivity: AppCompatActivity() {

    companion object {
        fun actionStart(context: Context) {
            val intent = Intent(context, ImageActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var selectBtn: Button
    private lateinit var previewIv: ImageView

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        selectBtn = findViewById(R.id.btn_select_image)
        previewIv = findViewById(R.id.iv_preview)

        selectBtn.setOnClickListener {
            pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }
}
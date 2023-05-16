package com.yechy.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.yechy.example.animator.AnimatorActivity
import com.yechy.example.ellipsize.EllipsizeTextViewActivity
import com.yechy.example.switch.SwitchActivity
import com.yechy.example.topappbar.ElevationOverlayAnimationActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_appbar_elevation).setOnClickListener {
            ElevationOverlayAnimationActivity.actionStart(this)
        }

        findViewById<Button>(R.id.btn_switch).setOnClickListener {
            SwitchActivity.actionStart(this)
        }

        findViewById<Button>(R.id.btn_ellpsize).setOnClickListener {
            EllipsizeTextViewActivity.actionStart(this)
        }

        findViewById<Button>(R.id.btn_animator).setOnClickListener {
            AnimatorActivity.actionStart(this)
        }

        logAllLocales()
    }

    fun logAllLocales() {
        val list = Locale.getAvailableLocales()
        list.forEach {
            Log.d("Locale", "country:${it.country}, lan=${it.language}, script=${it.script}, displayLanguage=${it.displayLanguage}")
        }
    }
}
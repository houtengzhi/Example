package com.yechy.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.yechy.example.switch.SwitchActivity
import com.yechy.example.topappbar.AppBarElevationActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_appbar_elevation).setOnClickListener {
            AppBarElevationActivity.actionStart(this)
        }

        findViewById<Button>(R.id.btn_switch).setOnClickListener {
            SwitchActivity.actionStart(this)
        }
    }
}
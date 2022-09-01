package com.yechy.example.switch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.yechy.example.R

/**
 *
 * Created by cloud on 2022/9/1.
 */
class SwitchActivity: AppCompatActivity() {

    companion object {
        fun actionStart(context: Context) {
            val intent = Intent(context, SwitchActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_switch)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val switchExample = findViewById<SwitchMaterial>(R.id.switch_test)
        val switchDisable = findViewById<SwitchMaterial>(R.id.switch_disbale)
        switchDisable.setOnCheckedChangeListener { compoundButton, b ->
            switchExample.isEnabled = !b
            if (switchExample.isEnabled) {
                switchExample.thumbDrawable.alpha = 0xFF
                switchExample.trackDrawable.alpha = 0xFF
            } else {
                switchExample.thumbDrawable.alpha = 0x61
                switchExample.trackDrawable.alpha = 0x61
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
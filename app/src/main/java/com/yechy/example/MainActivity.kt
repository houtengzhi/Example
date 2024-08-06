package com.yechy.example

import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yechy.example.animator.AnimatorActivity
import com.yechy.example.ellipsize.EllipsizeTextViewActivity
import com.yechy.example.switch.SwitchActivity
import com.yechy.example.topappbar.ElevationOverlayAnimationActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }
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

        findViewById<Button>(R.id.btn_countdown).setOnClickListener {
            showCountDownDialog(10)
        }

        logAllLocales()

        val activity = this
        Timer().schedule(object : TimerTask() {
            override fun run() {
                Log.d(TAG, "time")
                AnimatorActivity.actionStart(activity)
            }

        }, 10000)
    }

    fun logAllLocales() {
        val list = Locale.getAvailableLocales()
        list.forEach {
            Log.d("Locale", "country:${it.country}, lan=${it.language}, script=${it.script}, displayLanguage=${it.displayLanguage}")
        }
    }

    var mBreakoutRoomClosedCountDownDialog : AlertDialog? = null
    fun showCountDownDialog(countDown: Int) {
        val countDownTimer: CountDownTimer = object : CountDownTimer((countDown + 1) * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d("CountDown", "millisUntilFinished=${millisUntilFinished/1000}")
                mBreakoutRoomClosedCountDownDialog?.let {
                    if (it.isShowing()) {
                        it.setTitle("Breakout room will close in ${millisUntilFinished/1000} seconds")
                    }
                }
            }

            override fun onFinish() {
                Log.d("CountDown", "onFinish")
                mBreakoutRoomClosedCountDownDialog?.let {
                    if (it.isShowing) {
                        it.dismiss()
                    }
                }
            }
        }
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("Breakout room will close in $countDown seconds")
            .setMessage(
                "All participants have been given $countDown seconds to leave their Breakout Rooms"
            )
            .setPositiveButton("Leave",
                DialogInterface.OnClickListener { dialog, which ->  })
            .setNegativeButton("Cancel", null)
            .setOnDismissListener(DialogInterface.OnDismissListener {
                countDownTimer.cancel()
                mBreakoutRoomClosedCountDownDialog = null
            })
            .setCancelable(false)
        mBreakoutRoomClosedCountDownDialog = builder.show()
        countDownTimer.start()
    }
}
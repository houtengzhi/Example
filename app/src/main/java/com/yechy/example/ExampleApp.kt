package com.yechy.example

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log

/**
 *
 * Created by cloud on 2023/5/25.
 */
class ExampleApp: Application() {

    companion object {
        const val TAG = "ExampleApp"
    }

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                Log.d(TAG, "onActivityCreated() activity=${activity}")
            }

            override fun onActivityStarted(activity: Activity) {
                Log.d(TAG, "onActivityStarted() activity=${activity}")
            }

            override fun onActivityResumed(activity: Activity) {
                Log.d(TAG, "onActivityResumed() activity=${activity}")
            }

            override fun onActivityPaused(activity: Activity) {
                Log.d(TAG, "onActivityPaused() activity=${activity}")
            }

            override fun onActivityStopped(activity: Activity) {
                Log.d(TAG, "onActivityStopped() activity=${activity}")
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                Log.d(TAG, "onActivitySaveInstanceState() activity=${activity}")
            }

            override fun onActivityDestroyed(activity: Activity) {
                Log.d(TAG, "onActivityDestroyed() activity=${activity}")
            }

        })
    }
}
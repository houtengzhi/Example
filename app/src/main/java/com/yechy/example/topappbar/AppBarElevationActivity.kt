package com.yechy.example.topappbar

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.yechy.example.R

/**
 *
 * Created by cloud on 2022/8/31.
 */
class AppBarElevationActivity: AppCompatActivity() {

    companion object {
        fun actionStart(context: Context) {
            val intent = Intent(context, AppBarElevationActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appbarlayout_elevation)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
//        val viewPager = findViewById<ViewPager>(R.id.view_pager)
//
//        viewPager.adapter = TabPagerAdapter(supportFragmentManager)
//        tabLayout.setupWithViewPager(viewPager)
//
//        for (i in 0 until tabLayout.tabCount) {
//            tabLayout.getTabAt(i)?.text = "Tab ${i+1}"
//        }

        supportFragmentManager.beginTransaction()
            .add(R.id.layout_container, ExampleListFragment())
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
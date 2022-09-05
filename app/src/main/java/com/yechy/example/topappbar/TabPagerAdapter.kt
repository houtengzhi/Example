package com.yechy.example.topappbar

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

/**
 *
 * Created by cloud on 2022/9/1.
 */
class TabPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        if (position == 0) {
            return ExampleListFragment()
        } else if (position == 1) {
            return ScrollableContentFragment()
        }
        return Fragment()
    }
}
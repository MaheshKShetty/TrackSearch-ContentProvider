package com.mshetty.tracksearch.search.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class PagerAdapter(fragmentManager: FragmentManager , private var fragmentList: List<Fragment> ,
                   private var titleList:List<String>) : FragmentPagerAdapter(fragmentManager,
                    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
     return titleList.get(index = position)
    }

    override fun getCount(): Int {
        return fragmentList.size
    }
}
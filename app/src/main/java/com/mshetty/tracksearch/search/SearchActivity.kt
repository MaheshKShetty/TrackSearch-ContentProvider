package com.mshetty.tracksearch.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment

import com.mshetty.tracksearch.search.fragments.SearchFragment
import com.mshetty.tracksearch.databinding.ActivitySearchBinding
import com.mshetty.tracksearch.search.adapter.ViewPagerAdapter
import com.mshetty.tracksearch.search.data.Constants

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragmentList = ArrayList<Fragment>()
        fragmentList.add(SearchFragment.newInstance(Constants.SEARCH_SCREEN1))
        binding.viewPager.adapter = ViewPagerAdapter(this, fragmentList)
        binding.viewPager.currentItem = intent?.getIntExtra(Constants.POSITION, 0) ?: 0
    }
}

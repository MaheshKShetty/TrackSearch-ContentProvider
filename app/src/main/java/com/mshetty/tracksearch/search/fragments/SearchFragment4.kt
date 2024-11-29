package com.mshetty.tracksearch.search.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mshetty.tracksearch.search.adapter.PagerAdapter
import com.mshetty.tracksearch.search.searchview.CustomSearchView
import com.mshetty.tracksearch.R
import com.mshetty.tracksearch.databinding.LayoutSearch4Binding
import com.mshetty.tracksearch.databinding.LayoutSearchBinding
import com.mshetty.tracksearch.search.data.Constants


class SearchFragment4 : Fragment(), CustomSearchView.OnTextChangeListener {

    var fragmentList = ArrayList<Fragment>()
    private lateinit var binding: LayoutSearch4Binding

    companion object {
        fun newInstance(): SearchFragment4 {
            return SearchFragment4()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup? ,
         savedInstanceState: Bundle?): View? {
        binding = LayoutSearch4Binding.inflate(LayoutInflater.from(context), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fragmentList.clear()
        fragmentList.add(SearchTabFragment.newInstance(Constants.ALL))
        fragmentList.add(SearchTabFragment.newInstance(Constants.VIDEO))
        fragmentList.add(SearchTabFragment.newInstance(Constants.IMAGES))
        val title = ArrayList<String>()
        title.addAll(resources.getStringArray(R.array.search_tab_title))
        binding.viewPager.adapter = PagerAdapter(childFragmentManager ,fragmentList,title)
        binding.searchView.setOnTextChangeListener(this)
        binding.viewPager.currentItem = 0
        binding.tabs.setupWithViewPager(binding.viewPager)
    }

    override fun onTextChanged(s: String) {
        (fragmentList[binding.viewPager.currentItem] as? SearchTabFragment)?.setSuggestionText(s)
    }

    override fun onQuerySubmit(s: String) {
        (fragmentList[binding.viewPager.currentItem] as? SearchTabFragment)?.setQuery(s)
    }
}

package com.mshetty.tracksearch.search.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FilterQueryProvider
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.mshetty.tracksearch.search.adapter.SearchAdapter
import com.mshetty.tracksearch.search.data.SearchHistoryUtil
import com.mshetty.tracksearch.search.db.HistoryContract
import com.mshetty.tracksearch.search.searchview.CustomSearchView
import com.mshetty.tracksearch.R
import com.mshetty.tracksearch.databinding.LayoutSearchBinding
import com.mshetty.tracksearch.search.data.Constants

class SearchFragment : Fragment(), CustomSearchView.OnTextChangeListener {

    private lateinit var adapter: SearchAdapter
    private lateinit var binding: LayoutSearchBinding

    companion object {
        fun newInstance(): SearchFragment {
            val fragment = SearchFragment()
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutSearchBinding.inflate(LayoutInflater.from(context), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.searchView.setOnTextChangeListener(this)
        binding.searchView.background = context?.let {
            AppCompatResources.getDrawable(
                it,
                R.drawable.ic_search_bottom_cornered_bg
            )
        }
        val arr = resources.getStringArray(R.array.suggestions)
        context?.let { SearchHistoryUtil.addSuggestions(it, arr.toList(), Constants.ALL) }
        val cursor = SearchHistoryUtil.getCursor(
            context, HistoryContract.HistoryEntry.CONTENT_URI,
            null, HistoryContract.HistoryEntry.COLUMN_IS_HISTORY,
            null, null
        )
        adapter = SearchAdapter(context,cursor, 0)
        adapter.filterQueryProvider = FilterQueryProvider { constraint ->
            val filter: String = constraint.toString()
            if (filter.isEmpty()) {
                SearchHistoryUtil.getCursor(
                    context, HistoryContract.HistoryEntry.CONTENT_URI,
                    null, HistoryContract.HistoryEntry.COLUMN_IS_HISTORY,
                    null, null
                )
            } else {
                SearchHistoryUtil.getCursor(
                    context,
                    (HistoryContract.HistoryEntry.CONTENT_URI),
                    null, HistoryContract.HistoryEntry.COLUMN_QUERY,
                    "%$filter%", null
                )
            }
        }
        binding.suggestionList.adapter = adapter
        binding.suggestionList.isTextFilterEnabled = true
        binding.suggestionList.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val suggestion: String = getSuggestionAtPosition(position)
                binding.searchView.setQuery(suggestion)
        }
    }

    private fun getSuggestionAtPosition(position: Int): String {
        return if (position < 0 || position >= adapter.count) {
            ""
        } else {
            adapter.getItem(position).toString()
        }
    }

    override fun onTextChanged(s: String) {
        adapter.filter.filter(s)
        adapter.notifyDataSetChanged()
    }

    override fun onQuerySubmit(s: String) {
        context?.let {
            SearchHistoryUtil.saveQueryToDb(
                it,
                s,
                System.currentTimeMillis(),
                Constants.ALL
            )
        }
    }

}

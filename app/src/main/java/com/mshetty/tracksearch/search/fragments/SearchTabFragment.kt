package com.mshetty.tracksearch.search.fragments

import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FilterQueryProvider
import androidx.fragment.app.Fragment
import com.mshetty.tracksearch.search.adapter.SearchAdapter
import com.mshetty.tracksearch.search.data.SearchHistoryUtil
import com.mshetty.tracksearch.search.db.HistoryContract
import com.mshetty.tracksearch.R
import com.mshetty.tracksearch.databinding.LayoutSearchTypeBinding
import com.mshetty.tracksearch.search.data.Constants.Companion.ALL
import com.mshetty.tracksearch.search.data.Constants.Companion.IMAGES
import com.mshetty.tracksearch.search.data.Constants.Companion.SEARCH_SCREEN4
import com.mshetty.tracksearch.search.data.Constants.Companion.VIDEO

class SearchTabFragment : Fragment() {

    private var cursor: Cursor? = null
    private var type: String? = null
    private var adapter: SearchAdapter? = null
    private lateinit var binding: LayoutSearchTypeBinding

    companion object {
        fun newInstance(type: String): SearchTabFragment {
            val fragment = SearchTabFragment()
            fragment.type = type
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutSearchTypeBinding.inflate(LayoutInflater.from(context), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val arr = resources.getStringArray(R.array.suggestions)
        context?.let { SearchHistoryUtil.addSuggestions(it, arr.toList(), IMAGES) }
        cursor = when (type) {
            ALL -> {
                SearchHistoryUtil.cursor
            }

            VIDEO -> {
                context?.let { SearchHistoryUtil.getSearchType(it, VIDEO) }
            }

            IMAGES -> {
                context?.let { SearchHistoryUtil.getSearchType(it, IMAGES) }
            }

            else -> {
                SearchHistoryUtil.cursor
            }
        }
        adapter = SearchAdapter(context, cursor, 0, SEARCH_SCREEN4)
        adapter?.filterQueryProvider = FilterQueryProvider { constraint ->
            val filter: String = constraint.toString()
            if (filter.isEmpty()) {
                when (type) {
                    ALL -> {
                        SearchHistoryUtil.cursor
                    }

                    VIDEO -> {
                        context?.let { SearchHistoryUtil.getSearchType(it, VIDEO) }
                    }

                    IMAGES -> {
                        context?.let { SearchHistoryUtil.getSearchType(it, IMAGES) }
                    }

                    else -> {
                        SearchHistoryUtil.cursor
                    }
                }
            } else {
                cursor = SearchHistoryUtil.getCursor(
                    context,
                    (HistoryContract.HistoryEntry.CONTENT_URI),
                    null, HistoryContract.HistoryEntry.COLUMN_QUERY,
                    "%$filter%", type
                )
                cursor
            }
        }
        binding.suggestionList.adapter = adapter
        binding.suggestionList.isTextFilterEnabled = true
    }

    fun setSuggestionText(s: String) {
        adapter?.filter?.filter(s)
    }

    fun setQuery(s: String) {
        context?.let { SearchHistoryUtil.saveQueryToDb(it, s, System.currentTimeMillis(), type) }
    }
}

package com.mshetty.tracksearch.search.fragments

import SwipeToDeleteCallback
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.mshetty.tracksearch.R
import com.mshetty.tracksearch.databinding.LayoutSearchBinding
import com.mshetty.tracksearch.search.adapter.SearchRecyclerAdapter
import com.mshetty.tracksearch.search.data.Constants
import com.mshetty.tracksearch.search.data.SearchHistoryUtil
import com.mshetty.tracksearch.search.db.HistoryContract
import com.mshetty.tracksearch.search.searchview.CustomSearchView

class SearchFragment : Fragment(), CustomSearchView.OnTextChangeListener {

    private lateinit var adapter: SearchRecyclerAdapter
    private var _binding: LayoutSearchBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = SearchFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupSearchView()
        setupRecyclerView()
        loadSuggestions()
    }

    private fun setupSearchView() {
        binding.searchView.apply {
            setOnTextChangeListener(this@SearchFragment)
            background = context?.let {
                AppCompatResources.getDrawable(it, R.drawable.ic_search_bottom_cornered_bg)
            }
        }
    }

    private fun setupRecyclerView() {
        binding.suggestionList.layoutManager = LinearLayoutManager(context)
        adapter = SearchRecyclerAdapter(
            context,
            null,
            onItemClick = { suggestion -> binding.searchView.setQuery(suggestion) },
            onItemDelete = { query -> deleteItemFromDatabase(query) }
        )
        binding.suggestionList.adapter = adapter

        val swipeCallback = SwipeToDeleteCallback(adapter) { position, action ->
            if (action == SwipeAction.DELETE) {
                adapter.deleteItem(position)
                updateCursorAfterDeletion()
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.suggestionList)
    }

    private fun loadSuggestions(filter: String? = null) {
        val uri = HistoryContract.HistoryEntry.CONTENT_URI
        val selection = filter?.let { "${HistoryContract.HistoryEntry.COLUMN_QUERY} LIKE ?" }
        val selectionArgs = filter?.let { arrayOf("%$it%") }
        val cursor: Cursor? = context?.contentResolver?.query(uri, null, selection, selectionArgs, null)
        adapter.swapCursor(cursor)
    }

    private fun deleteItemFromDatabase(query: String) {
        context?.let { SearchHistoryUtil.deleteItemFromDatabase(it, query) }
    }

    override fun onTextChanged(s: String) {
        loadSuggestions(s)
    }

    override fun onQuerySubmit(s: String) {
        context?.let {
            SearchHistoryUtil.saveQueryToDb(it, s, System.currentTimeMillis(), Constants.ALL)
        }
    }

    override fun onBackPressed() {
        showExitDialog()
    }

    private fun updateCursorAfterDeletion() {
        val uri = HistoryContract.HistoryEntry.CONTENT_URI
        val cursor: Cursor? = context?.contentResolver?.query(uri, null, null, null, null)
        adapter.swapCursor(cursor)
    }

    private fun showExitDialog() {
        context?.let {
            AlertDialog.Builder(it)
                .setTitle(getString(R.string.exit_dialog))
                .setPositiveButton(getString(R.string.exit)) { _, _ -> activity?.let { finishAffinity(it) } }
                .setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                .setCancelable(false)
                .show()
        }
    }
}
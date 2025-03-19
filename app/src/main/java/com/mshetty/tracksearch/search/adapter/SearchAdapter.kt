package com.mshetty.tracksearch.search.adapter

import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.mshetty.tracksearch.R
import com.mshetty.tracksearch.search.db.HistoryContract

class SearchRecyclerAdapter(
    private val context: Context?,
    private var cursor: Cursor?,
    private val onItemClick: (String) -> Unit,
    private val onItemDelete: (String) -> Unit
) : RecyclerView.Adapter<SearchRecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivSuggestion: AppCompatImageView = itemView.findViewById(R.id.ivSuggesstion)
        private val tvSuggestion: AppCompatTextView = itemView.findViewById(R.id.tvSuggestion)
        private val clContainer: ConstraintLayout = itemView.findViewById(R.id.clContainer)
        private var divider: View? = itemView.findViewById(R.id.divider)

        fun bind(cursor: Cursor) {
            val text =
                cursor.getString(cursor.getColumnIndexOrThrow(HistoryContract.HistoryEntry.COLUMN_QUERY))
            val isHistory =
                cursor.getInt(cursor.getColumnIndexOrThrow(HistoryContract.HistoryEntry.COLUMN_IS_HISTORY)) != 0
            val searchType =
                cursor.getString(cursor.getColumnIndexOrThrow(HistoryContract.HistoryEntry.COLUMN_QUERY_TYPE))
            val isLastItem = cursor.isLast
            val historyItem = SearchHistoryItem(text, isHistory, searchType, isLastItem)

            tvSuggestion.text = historyItem.text
            tvSuggestion.setTextColor(Color.BLACK)

            if (historyItem.isLastItem) {
                clContainer.background = context?.let {
                    AppCompatResources.getDrawable(it, R.drawable.ic_search_bottom_cornered_bg)
                }
            } else {
                context?.getColor(R.color.white_three)?.let { clContainer.setBackgroundColor(it) }
            }

            ivSuggestion.setImageResource(if (historyItem.isHistory) R.drawable.ic_search_history_white else R.drawable.ic_search_history_white)

            // Handle item click
            itemView.setOnClickListener {
                onItemClick(historyItem.text)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_search_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        cursor?.moveToPosition(position)
        cursor?.let { holder.bind(it) }
    }

    override fun getItemCount(): Int = cursor?.count ?: 0

    fun swapCursor(newCursor: Cursor?) {
        cursor?.close()
        cursor = newCursor
        notifyDataSetChanged()
    }

    fun deleteItem(position: Int) {
        cursor?.moveToPosition(position)
        val query = cursor?.getColumnIndexOrThrow(HistoryContract.HistoryEntry.COLUMN_QUERY)
            ?.let { cursor?.getString(it) }
        query?.let { onItemDelete(it) }
        this.notifyItemRemoved(position)
    }
}

data class SearchHistoryItem(
    val text: String, val isHistory: Boolean, val searchType: String?, val isLastItem: Boolean
)

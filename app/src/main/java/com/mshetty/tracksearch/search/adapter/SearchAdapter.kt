package com.mshetty.tracksearch.search.adapter

import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.mshetty.tracksearch.R
import com.mshetty.tracksearch.search.db.HistoryContract

class SearchAdapter(val context: Context?, cursor: Cursor?, flags: Int = 0) :
    CursorAdapter(context, cursor, flags) {

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        return LayoutInflater.from(context).inflate(R.layout.layout_search_item, parent, false)
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val viewHolder = ListViewHolder(view)
        view.tag = viewHolder
        val text =
            cursor.getString(cursor.getColumnIndexOrThrow(HistoryContract.HistoryEntry.COLUMN_QUERY))
        val isHistory =
            cursor.getInt(cursor.getColumnIndexOrThrow(HistoryContract.HistoryEntry.COLUMN_IS_HISTORY)) != 0
        val searchType =
            cursor.getString(cursor.getColumnIndexOrThrow(HistoryContract.HistoryEntry.COLUMN_QUERY_TYPE))
        val isLastItem = cursor.isLast
        val historyItem = SearchHistoryItem(text, isHistory, searchType, isLastItem)
        viewHolder.bindItem(historyItem)
    }

    override fun getItem(position: Int): Any {
        var retString = ""
        val cursor = cursor
        if (cursor != null && !cursor.isClosed) {
            if (cursor.moveToPosition(position)) {
                retString =
                    cursor.getString(cursor.getColumnIndexOrThrow(HistoryContract.HistoryEntry.COLUMN_QUERY))
            }
        }
        return retString
    }

    private inner class ListViewHolder(val convertView: View) {
        private val ivSuggestion: AppCompatImageView = convertView.findViewById(R.id.ivSuggesstion)
        private val tvSuggestion: AppCompatTextView = convertView.findViewById(R.id.tvSuggestion)
        private val clContainer: ConstraintLayout = convertView.findViewById(R.id.clContainer)
        private var divider: View? = null

        fun bindItem(item: SearchHistoryItem) {
            tvSuggestion.text = item.text
            tvSuggestion.setTextColor(Color.BLACK)
            divider = convertView.findViewById(R.id.divider)
            if (item.isLastItem) {
                clContainer.background = context?.let {
                    AppCompatResources.getDrawable(
                        it,
                        R.drawable.ic_search_bottom_cornered_bg
                    )
                }
            } else {
                context?.getColor(R.color.white_three)?.let { clContainer.setBackgroundColor(it) }
            }
            val iconRes =
                if (item.isHistory) R.drawable.ic_search_history_white else R.drawable.ic_search_history_white
            ivSuggestion.setImageResource(iconRes)
        }
    }
}

data class SearchHistoryItem(
    val text: String, val isHistory: Boolean, val searchType: String?, val isLastItem: Boolean
)

package com.mshetty.tracksearch.search.db

import android.content.ContentUris
import android.net.Uri
import android.provider.BaseColumns
import com.mshetty.tracksearch.search.data.Constants.Companion.DB_AUTHORITY

object HistoryContract {

    const val PATH_HISTORY = "history"
    const val CONTENT_AUTHORITY = DB_AUTHORITY
    private val BASE_CONTENT_URI = Uri.parse("content://$CONTENT_AUTHORITY")

    object HistoryEntry : BaseColumns {
        const val COLUMN_QUERY = "query"
        const val COLUMN_IS_HISTORY = "is_history"
        const val COLUMN_QUERY_TYPE = "query_type"
        const val COLUMN_INSERT_DATE = "insert_date"
        val CONTENT_URI : Uri = BASE_CONTENT_URI.buildUpon().appendPath(PATH_HISTORY).build()
        val CONTENT_TYPE = "vnd.android.cursor.dir/$CONTENT_URI/$PATH_HISTORY"

        fun buildHistoryUri(id : Long) : Uri {
            return ContentUris.withAppendedId(CONTENT_URI ,id)
        }
    }
}
package com.mshetty.tracksearch.search.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.mshetty.tracksearch.search.db.HistoryContract
import java.util.ArrayList

object SearchHistoryUtil {


    fun saveQueryToDb(context: Context, query: String?, time: Long, type: String?) {
        if (!TextUtils.isEmpty(query) && time > 0) {
            val values = ContentValues()
            values.put(HistoryContract.HistoryEntry.COLUMN_QUERY,query)
            values.put(HistoryContract.HistoryEntry.COLUMN_INSERT_DATE,time)
            values.put(HistoryContract.HistoryEntry.COLUMN_IS_HISTORY,1)
            values.put(HistoryContract.HistoryEntry.COLUMN_QUERY_TYPE,type)
            context.contentResolver.insert(HistoryContract.HistoryEntry.CONTENT_URI,values)
        }
    }

    fun deleteItemFromDatabase(context: Context, query: String) {
        val rowsDeleted = context.contentResolver.delete(
            HistoryContract.HistoryEntry.CONTENT_URI,
            "${HistoryContract.HistoryEntry.COLUMN_QUERY} = ?",
            arrayOf(query.trim())
        )
        if(rowsDeleted == -1){
            Log.e("TAG", "Error deleting item from database")
        }
    }

}
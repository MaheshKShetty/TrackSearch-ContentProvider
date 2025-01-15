package com.mshetty.tracksearch.search.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import com.mshetty.tracksearch.search.db.HistoryContract
import java.util.ArrayList

object SearchHistoryUtil {

    var cursor : Cursor? = null

    private fun setCursor(context : Context) : Cursor? {
        cursor = getCursor(context , HistoryContract.HistoryEntry.CONTENT_URI ,
            null , HistoryContract.HistoryEntry.COLUMN_IS_HISTORY , null , null)
        return cursor
    }


    fun saveQueryToDb(context: Context, query: String?, time: Long, type: String?) {
        if (!TextUtils.isEmpty(query) && time > 0) {
            val values = ContentValues()
            values.put(HistoryContract.HistoryEntry.COLUMN_QUERY,query)
            values.put(HistoryContract.HistoryEntry.COLUMN_INSERT_DATE,time)
            values.put(HistoryContract.HistoryEntry.COLUMN_IS_HISTORY,1)
            values.put(HistoryContract.HistoryEntry.COLUMN_QUERY_TYPE,type)
            context.contentResolver.insert(HistoryContract.HistoryEntry.CONTENT_URI,values)
        }
        setCursor(context)
    }

    fun addSuggestions(context : Context,suggestions : List<String?>,type:String) {
        val toSave : ArrayList<ContentValues> = ArrayList()
        for (str : String? in suggestions) {
            val value = ContentValues()
            value.put(HistoryContract.HistoryEntry.COLUMN_QUERY,str)
            value.put(HistoryContract.HistoryEntry.COLUMN_INSERT_DATE,System.currentTimeMillis())
            value.put(HistoryContract.HistoryEntry.COLUMN_IS_HISTORY,0)
            value.put(HistoryContract.HistoryEntry.COLUMN_QUERY_TYPE ,type)
            toSave.add(value)
        }
        val values : Array<ContentValues> = toSave.toTypedArray()
        context.contentResolver.bulkInsert(HistoryContract.HistoryEntry.CONTENT_URI,values)
        setCursor(context)
    }

    fun getCursor(context : Context?, uri : Uri, projection : String?, selection : String?,
                  selectionArgs : String?, sortOrder : String?) : Cursor? {
        cursor = context?.contentResolver?.query(uri, arrayOf(projection), selection, arrayOf(selectionArgs), sortOrder)
        return cursor
    }
}
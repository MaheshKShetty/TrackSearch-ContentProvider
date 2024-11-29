package com.mshetty.tracksearch.search.db

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import com.mshetty.tracksearch.search.data.SearchEntity
import com.mshetty.tracksearch.search.data.Constants

class HistoryProvider : ContentProvider() {

    private var searchRoomDatabase: SearchRoomDatabase? = null

    override fun onCreate(): Boolean {
        searchRoomDatabase = context?.let {
            SearchRoomDatabase.getInstance(it)
        }
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor {
        val rCursor: Cursor = when (sUriMatcher.match(uri)) {
            SEARCH_HISTORY -> when (selection) {
                HistoryContract.HistoryEntry.COLUMN_IS_HISTORY -> {
                    SearchHistoryDataTask(context).execute().get()
                }

                HistoryContract.HistoryEntry.COLUMN_QUERY -> {
                    SearchFilterDataTask(context, selectionArgs, selection, sortOrder).execute()
                        .get()
                }

                HistoryContract.HistoryEntry.COLUMN_QUERY_TYPE -> {
                    SearchFilterDataTask(context, selectionArgs, selection, sortOrder).execute()
                        .get()
                }

                else -> {
                    SearchHistoryDataTask(context).execute().get()
                }
            }

            else -> throw UnsupportedOperationException("Unknown Uri: $uri")
        }
        val context = context
        if (context != null) {
            rCursor.setNotificationUri(context.contentResolver, uri)
        }
        return rCursor
    }

    override fun getType(uri: Uri): String {
        return when (sUriMatcher.match(uri)) {
            SEARCH_HISTORY -> HistoryContract.HistoryEntry.CONTENT_TYPE
            else -> throw UnsupportedOperationException("Uknown Uri: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        val history: Int? = values?.getAsInteger(HistoryContract.HistoryEntry.COLUMN_IS_HISTORY)
        val insertDate: Long? = values?.getAsLong(HistoryContract.HistoryEntry.COLUMN_INSERT_DATE)
        val queryType: String? = values?.getAsString(HistoryContract.HistoryEntry.COLUMN_QUERY_TYPE)
        val query: String? = values?.getAsString(HistoryContract.HistoryEntry.COLUMN_QUERY)
        val searchEntity = SearchEntity(query, queryType, insertDate, history)
        val id: Long
        val retUri: Uri
        if (sUriMatcher.match(uri) == SEARCH_HISTORY) {
            id = InsertSearchDataTask(context, searchEntity).execute().get()
            retUri = if (id > 0) {
                HistoryContract.HistoryEntry.buildHistoryUri(id)
            } else {
                throw UnsupportedOperationException("Unable to insert rows into: $uri")
            }
        } else {
            throw UnsupportedOperationException("Unknown uri: $uri")
        }
        return retUri
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val rows: Int = when (sUriMatcher.match(uri)) {
            SEARCH_HISTORY -> DeleteAndUpdateDb(context).execute().get()
            else -> throw UnsupportedOperationException("Unknown uri: $uri")
        }
        if (rows != 0) {
            val context = context
            context?.contentResolver?.notifyChange(uri, null)
        }
        return rows
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val rows: Int = when (sUriMatcher.match(uri)) {
            SEARCH_HISTORY -> DeleteAndUpdateDb(context).execute().get()
            else -> throw UnsupportedOperationException("Unknown uri: $uri")
        }
        if (selection == null || rows != 0) {
            val context = context
            context?.contentResolver?.notifyChange(uri, null)
        }
        return rows
    }

    companion object {
        private const val SEARCH_HISTORY = 100
        private val sUriMatcher = buildUriMatcher()

        private fun buildUriMatcher(): UriMatcher {
            val content = HistoryContract.CONTENT_AUTHORITY
            val matcher = UriMatcher(UriMatcher.NO_MATCH)
            matcher.addURI(content, HistoryContract.PATH_HISTORY, SEARCH_HISTORY)
            return matcher
        }
    }
}

class InsertSearchDataTask(var context: Context?, private var searchEntity: SearchEntity) :
    AsyncTask<Void, Void, Long>() {
    private var _id: Long = 0
    override fun doInBackground(vararg p0: Void?): Long {
        context?.applicationContext?.let {
            _id = SearchRoomDatabase.getInstance(it).searchDao().insert(searchEntity)
        }
        return _id
    }

}

class SearchHistoryDataTask(var context: Context?) : AsyncTask<Void, Void?, Cursor>() {
    private var cursor: Cursor? = null
    override fun doInBackground(vararg params: Void?): Cursor? {
        context?.let {
            cursor = SearchRoomDatabase.getInstance(it).searchDao().getHistory(5)
        }
        return cursor
    }
}

class SearchFilterDataTask(
    var context: Context?,
    private var selectionArgs: Array<String>?,
    private var selection: String?, private var sortOrder: String?
) : AsyncTask<Void, Void?, Cursor>() {
    private lateinit var cursor: Cursor
    override fun doInBackground(vararg params: Void?): Cursor {
        context?.let {
            cursor = if (selection == HistoryContract.HistoryEntry.COLUMN_QUERY) {
                if (sortOrder == null || sortOrder == Constants.ALL) {
                    SearchRoomDatabase.getInstance(it).searchDao().getSearchFilter(selectionArgs)
                } else {
                    SearchRoomDatabase.getInstance(it)
                        .searchDao().getSearchFilter(selectionArgs, sortOrder)
                }
            } else {
                SearchRoomDatabase.getInstance(it).searchDao().getSearchFilterOnType(selectionArgs)
            }
        }
        return cursor
    }
}

class DeleteAndUpdateDb(var context: Context?) : AsyncTask<Void, Void?, Int>() {
    private var id: Int? = 0
    override fun doInBackground(vararg params: Void?): Int? {
        context?.let {}
        return id
    }
}
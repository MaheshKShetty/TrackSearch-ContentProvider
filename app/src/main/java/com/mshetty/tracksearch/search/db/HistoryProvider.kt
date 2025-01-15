package com.mshetty.tracksearch.search.db

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import com.mshetty.tracksearch.search.data.Constants
import com.mshetty.tracksearch.search.data.SearchEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class HistoryProvider : ContentProvider() {

    private var searchRoomDatabase: SearchRoomDatabase? = null

    override fun onCreate(): Boolean {
        searchRoomDatabase = context?.let {
            SearchRoomDatabase.getInstance(it)
        }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val context = context ?: throw IllegalStateException("Context is null")

        return runBlocking {
            val cursor: Cursor? = when (sUriMatcher.match(uri)) {
                SEARCH_HISTORY -> when (selection) {
                    HistoryContract.HistoryEntry.COLUMN_IS_HISTORY -> {
                        fetchSearchHistory(context)
                    }

                    HistoryContract.HistoryEntry.COLUMN_QUERY, HistoryContract.HistoryEntry.COLUMN_QUERY_TYPE -> {
                        fetchFilteredSearchResults(context, selectionArgs, selection, sortOrder)
                    }

                    else -> {
                        fetchSearchHistory(context)
                    }
                }

                else -> throw UnsupportedOperationException("Unknown Uri: $uri")
            }

            cursor?.setNotificationUri(context.contentResolver, uri)
            cursor
        }
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
        uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?
    ): Int {
        return runBlocking {
            val rows: Int = when (sUriMatcher.match(uri)) {
                SEARCH_HISTORY -> deleteAndUpdateDb(context)
                else -> throw UnsupportedOperationException("Unknown uri: $uri")
            }
            if (rows != 0) {
                val context = context
                context?.contentResolver?.notifyChange(uri, null)
            }
            rows
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return runBlocking {
            val rows: Int = when (sUriMatcher.match(uri)) {
                SEARCH_HISTORY -> deleteAndUpdateDb(context)
                else -> throw UnsupportedOperationException("Unknown uri: $uri")
            }
            if (selection == null || rows != 0) {
                val context = context
                context?.contentResolver?.notifyChange(uri, null)
            }
             rows
        }
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

suspend fun fetchSearchHistory(context: Context, limit: Int = 5): Cursor {
    return withContext(Dispatchers.IO) {
        SearchRoomDatabase.getInstance(context).searchDao().getHistory(limit)
    }
}

suspend fun fetchFilteredSearchResults(
    context: Context?, selectionArgs: Array<String>?, selection: String?, sortOrder: String?
): Cursor? {
    return withContext(Dispatchers.IO) {
        val database = context?.let { SearchRoomDatabase.getInstance(it) }
        when (selection) {
            HistoryContract.HistoryEntry.COLUMN_QUERY -> {
                if (sortOrder == null || sortOrder == Constants.ALL) {
                    database?.searchDao()?.getSearchFilter(selectionArgs)
                } else {
                    database?.searchDao()?.getSearchFilter(selectionArgs, sortOrder)
                }
            }

            else -> database?.searchDao()?.getSearchFilterOnType(selectionArgs)
        }
    }
}


suspend fun deleteAndUpdateDb(context: Context?): Int {
    return withContext(Dispatchers.IO) {
        context?.let {
            val id = 0
            id
        } ?: 0
    }
}
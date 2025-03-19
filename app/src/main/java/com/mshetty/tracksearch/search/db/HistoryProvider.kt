package com.mshetty.tracksearch.search.db

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.mshetty.tracksearch.search.data.Constants
import com.mshetty.tracksearch.search.data.SearchEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class HistoryProvider : ContentProvider() {

    private var searchRoomDatabase: SearchRoomDatabase? = null

    override fun onCreate(): Boolean {
        searchRoomDatabase = context?.let { SearchRoomDatabase.getInstance(it) }
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
            when (sUriMatcher.match(uri)) {
                SEARCH_HISTORY -> when (selection) {
                    HistoryContract.HistoryEntry.COLUMN_IS_HISTORY -> fetchSearchHistory(context)
                    HistoryContract.HistoryEntry.COLUMN_QUERY,
                    HistoryContract.HistoryEntry.COLUMN_QUERY_TYPE -> fetchFilteredSearchResults(
                        context,
                        selectionArgs,
                        selection,
                        sortOrder
                    )

                    else -> fetchSearchHistory(context)
                }

                else -> throw UnsupportedOperationException("Unknown Uri: $uri")
            }.also { cursor ->
                cursor?.setNotificationUri(context.contentResolver, uri)
            }
        }
    }

    override fun getType(uri: Uri): String {
        return when (sUriMatcher.match(uri)) {
            SEARCH_HISTORY -> HistoryContract.HistoryEntry.CONTENT_TYPE
            else -> throw UnsupportedOperationException("Unknown Uri: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        val searchEntity = values?.let {
            SearchEntity(
                columnQuery = it.getAsString(HistoryContract.HistoryEntry.COLUMN_QUERY),
                queryType = it.getAsString(HistoryContract.HistoryEntry.COLUMN_QUERY_TYPE),
                insertDate = it.getAsLong(HistoryContract.HistoryEntry.COLUMN_INSERT_DATE),
                isHistory = it.getAsInteger(HistoryContract.HistoryEntry.COLUMN_IS_HISTORY)
            )
        } ?: throw IllegalArgumentException("Invalid content values")

        return if (sUriMatcher.match(uri) == SEARCH_HISTORY) {
            val id = runBlocking {
                insertSearchEntity(context, searchEntity)
            }
            if (id > 0) {
                HistoryContract.HistoryEntry.buildHistoryUri(id)
            } else {
                throw UnsupportedOperationException("Failed to insert rows into: $uri")
            }
        } else {
            throw UnsupportedOperationException("Unknown Uri: $uri")
        }
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        return runBlocking {
            when (sUriMatcher.match(uri)) {
                SEARCH_HISTORY -> deleteAndUpdateDb(context)
                else -> throw UnsupportedOperationException("Unknown Uri: $uri")
            }.also { rows ->
                if (rows > 0) {
                    context?.contentResolver?.notifyChange(uri, null)
                }
            }
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return runBlocking {
            when (sUriMatcher.match(uri)) {
                SEARCH_HISTORY -> {
                    context?.let {
                        withContext(Dispatchers.IO) {
                            selectionArgs?.let { it1 ->
                                searchRoomDatabase?.searchDao()?.delete(it1)
                            }
                                ?: 0
                        }
                    } ?: 0
                }

                else -> throw UnsupportedOperationException("Unknown Uri: $uri")
            }.also { rows ->
                if (rows > 0) {
                    context?.contentResolver?.notifyChange(uri, null)
                }
            }
        }
    }

    companion object {
        private const val SEARCH_HISTORY = 100
        private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            val content = HistoryContract.CONTENT_AUTHORITY
            addURI(content, HistoryContract.PATH_HISTORY, SEARCH_HISTORY)
        }
    }
}

suspend fun fetchSearchHistory(context: Context, limit: Int = 25): Cursor {
    return withContext(Dispatchers.IO) {
        SearchRoomDatabase.getInstance(context).searchDao().getHistory(limit)
    }
}

suspend fun fetchFilteredSearchResults(
    context: Context?,
    selectionArgs: Array<String>?,
    selection: String?,
    sortOrder: String?
): Cursor? {
    return withContext(Dispatchers.IO) {
        context?.let {
            val database = SearchRoomDatabase.getInstance(it)
            when (selection) {
                HistoryContract.HistoryEntry.COLUMN_QUERY -> {
                    if (sortOrder == null || sortOrder == Constants.ALL) {
                        database.searchDao().getSearchFilter(selectionArgs)
                    } else {
                        database.searchDao().getSearchFilter(selectionArgs, sortOrder)
                    }
                }

                else -> database.searchDao().getSearchFilterOnType(selectionArgs)
            }
        }
    }
}

suspend fun insertSearchEntity(context: Context?, searchEntity: SearchEntity): Long {
    return withContext(Dispatchers.IO) {
        context?.let {
            SearchRoomDatabase.getInstance(it).searchDao().insert(searchEntity)
        } ?: 0
    }
}

suspend fun deleteAndUpdateDb(context: Context?): Int {
    return 0
}

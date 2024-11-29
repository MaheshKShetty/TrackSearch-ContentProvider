package com.mshetty.tracksearch.search.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName ="SEARCH_HISTORY",indices = [Index(value = ["query"], unique = true)])
data class SearchEntity(
    @ColumnInfo(name = "query")
    var columnQuery: String?,
    @ColumnInfo(name = "query_type")
    var queryType: String?,
    @ColumnInfo(name = "insert_date")
    var insertDate: Long?,
    @ColumnInfo(name = "is_history")
    var isHistory: Int? = 0,
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id:Long = 0)

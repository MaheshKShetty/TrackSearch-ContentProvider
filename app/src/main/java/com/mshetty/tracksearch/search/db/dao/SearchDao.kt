package com.mshetty.tracksearch.search.db.dao

import android.database.Cursor
import androidx.room.*
import com.mshetty.tracksearch.search.data.SearchEntity

@Dao
interface SearchDao {

    @Query("SELECT * FROM SEARCH_HISTORY ORDER BY insert_date DESC LIMIT :limit")
    fun getHistory(limit: Int): Cursor

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(search: SearchEntity): Long

    @Delete
    fun delete(search: SearchEntity): Int

    @Update
    fun update(search: SearchEntity): Int

    @Query("SELECT * FROM SEARCH_HISTORY WHERE `query` LIKE :selectionArgs AND query_type IS :sortOrder ORDER BY insert_date DESC")
    fun getSearchFilter(selectionArgs: Array<String>?, sortOrder: String?): Cursor

    @Query("SELECT * FROM SEARCH_HISTORY WHERE `query_type` LIKE :selectionArgs ORDER BY insert_date DESC")
    fun getSearchFilterOnType(selectionArgs: Array<String>?): Cursor

    @Query("SELECT * FROM SEARCH_HISTORY WHERE `query` LIKE :selectionArgs ORDER BY insert_date DESC")
    fun getSearchFilter(selectionArgs: Array<String>?): Cursor
}

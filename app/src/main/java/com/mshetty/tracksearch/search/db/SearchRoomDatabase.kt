package com.mshetty.tracksearch.search.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mshetty.tracksearch.search.data.SearchEntity
import com.mshetty.tracksearch.search.db.dao.SearchDao

@Database(entities = [SearchEntity::class], version = 1,exportSchema = false)
abstract class SearchRoomDatabase : RoomDatabase() {

    abstract fun searchDao(): SearchDao

    companion object {

        private var instance: SearchRoomDatabase? = null

        fun getInstance(context: Context): SearchRoomDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): SearchRoomDatabase {
            return Room.databaseBuilder(context, SearchRoomDatabase::class.java, "SEARCH_HISTORY.db")
                .build()
        }
    }
}

package com.example.flightmobileapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Create a single instance of the database.
@Database(entities = [Url::class], version = 2, exportSchema = false)
abstract class UrlRoomDatabase : RoomDatabase() {
    abstract fun urlDao(): UrlDao

    // Create a singleton object.
    companion object {
        @Volatile
        private var dbInstance: UrlRoomDatabase? = null

        // Return instance of the database. if was not created yet then create one.
        fun getInstance(context: Context): UrlRoomDatabase {
            synchronized(this) {
                var instance: UrlRoomDatabase? = dbInstance
                if (instance == null) {
                    // Create database.
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        UrlRoomDatabase::class.java,
                        "server_url_data_database"
                    ).build()
                }
                return instance
            }
        }
    }
}
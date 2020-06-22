package com.example.flightmobileapp

import androidx.lifecycle.LiveData
import androidx.room.*

// SQL queries are associated with method calls.
@Dao
interface UrlDao {

    @Query("SELECT url, update_time FROM urls_table ORDER BY update_time DESC limit 5")
    fun getUrls(): LiveData<List<Url>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(url: Url): Long

}
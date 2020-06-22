package com.example.flightmobileapp

import androidx.lifecycle.LiveData

// Url repository provides a clean API for data access to the rest of the application.
class UrlRepo(private val urlDao: UrlDao) {

    // Get all the urls according to the defined query.
    val allUrls: LiveData<List<Url>> = urlDao.getUrls()

    // Insert the url to the db using insert query.
    suspend fun insert(url: Url) : Long {
        return urlDao.insert(url)
    }
}
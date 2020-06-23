package com.example.flightmobileapp

import android.app.Application
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

/** View model that holds app data in a lifecycle-conscious way that saves configuration changes. */
class UrlViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UrlRepo
    val allUrls: LiveData<List<Url>>

    // Init db instance and get url list.
    init {
        val urlsDao = UrlRoomDatabase.getInstance(application).urlDao()
        repository = UrlRepo(urlsDao)
        allUrls = repository.allUrls
    }

    /** Insert url to db. */
    public fun insert(url: Url) = viewModelScope.launch(Dispatchers.IO) {
        val newUrl: Long = repository.insert(url)
        // If insertion failed
        if (newUrl <= -1) {
            Toast.makeText(getApplication(),"Url insertion to db failed",Toast.LENGTH_SHORT).show()
        }
    }
}
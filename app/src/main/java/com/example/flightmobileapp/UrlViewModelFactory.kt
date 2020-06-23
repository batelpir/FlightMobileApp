package com.example.flightmobileapp

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

/** Factory for UrlViewModel to match types of returned values. */
class UrlViewModelFactory constructor(private val application: Application): ViewModelProvider.Factory {

    /** Create UrlViewModel. */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(UrlViewModel::class.java)) {
            UrlViewModel(application) as T
        } else {
            throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}
package com.hrhousing.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hrhousing.app.AppContainer

/** Every screen ViewModel in this app takes a single `AppContainer` constructor argument. */
class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val constructor = modelClass.getConstructor(AppContainer::class.java)
        return constructor.newInstance(container)
    }
}

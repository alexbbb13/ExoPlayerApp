package com.alexb.testexoplayerapp.ui

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {add
    private val shouldRestartPlayer: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun shouldRestartPlayer(): LiveData<Boolean> {
        return shouldRestartPlayer
    }

    fun onInitialLocationSet(location: Location) {
        shouldRestartPlayer.postValue(true)
    }
}
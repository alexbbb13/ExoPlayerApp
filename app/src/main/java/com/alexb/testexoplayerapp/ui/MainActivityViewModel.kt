package com.alexb.testexoplayerapp.ui

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    private val shouldRestartPlayer: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    private val shouldPausePlayer: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val seekFromCurrentPlusMs: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val setVolumePlus: MutableLiveData<Float> by lazy {
        MutableLiveData<Float>()
    }
    lateinit var lastLocation: Location

    fun shouldRestartPlayer(): LiveData<Boolean> {
        return shouldRestartPlayer
    }

    // true - pause, false - resume
    fun shouldPausePlayer(): LiveData<Boolean> {
        return shouldPausePlayer
    }

    var playerPaused = false

    fun onInitialLocationSet(location: Location) {
        lastLocation = location
    }

    fun onNextLocation(location: Location) {
        val distance = lastLocation.distanceTo(location)
        if (distance > DISTANCE_THRESHOLD) {
            shouldRestartPlayer.postValue(true)
        }
    }

    /*
     2. Using the user’s location, a change of 10 meters of the current and previous location
     will reset the video and replay from the start.

      3. A shake of the device should pause the video.
      3a.  next shake unpauses the video
    */
    fun onShake() {
        shouldPausePlayer.postValue(playerPaused)
        playerPaused = !playerPaused
    }

    /*
       4. Using gyroscope events, rotation along the z-axis should be able to control the
       current time where the video is playing. While rotation along the x-axis should control
       the volume of the sound. See image below for reference:
    */
    fun onRotateZ(value: Float) {
        //10% of G-force, where 1 means 90 degrees angle
        if (Math.abs(value) > ROTATE_Z_THRESHOLD) {
            if (value > 0) {
                //seek backwards 5000ms
                seekFromCurrentPlusMs.postValue(-5000)
            } else {
                //seek forward 5000ms
                seekFromCurrentPlusMs.postValue(5000)
            }
        }
    }

    fun onRotateY(value: Float) {
        setVolumePlus.postValue(value)
    }

    companion object {
        const val ROTATE_Z_THRESHOLD = 0.1f
        const val ROTATE_Y_THRESHOLD = 0.1f
        const val DISTANCE_THRESHOLD = 10f
    }
}
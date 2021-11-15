package com.alexb.testexoplayerapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.alexb.testexoplayerapp.R
import com.alexb.testexoplayerapp.databinding.ActivityMainBinding
import com.alexb.testexoplayerapp.sensor.ShakeDetector
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.gms.location.*


class MainActivity : AppCompatActivity() {

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val viewModel: MainActivityViewModel by viewModels()
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
            }
            else -> {
                // No location access granted.
            }
        }
    }
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                viewModel.onNextLocation(location)
            }
        }
    }
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var shakeDetector: ShakeDetector
    lateinit var player: SimpleExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        initializePlayer()
        //ask for location permissions
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
        inializeSensorListener()
        setViewmodelObservers()
    }

    private fun inializeSensorListener() {
        shakeDetector = ShakeDetector()
        shakeDetector.setOnShakeListener(object : ShakeDetector.OnShakeListener {
            override fun onShake(count: Int) {
                viewModel.onShake()
            }

            override fun onRotateZ(value: Float) {
                viewModel.onRotateZ(value)
            }

            override fun onRotateY(value: Float) {
                viewModel.onRotateY(value)
            }
        })
        shakeDetector.init(this)
    }

    private fun setViewmodelObservers() {
        viewModel.shouldRestartPlayer().observe(this, Observer<Boolean> { shouldRestart ->
            if (shouldRestart) {
                player.seekTo(0);
                player.setPlayWhenReady(true)
            }
        })
        viewModel.shouldPausePlayer().observe(this, Observer<Boolean> { shouldPause ->
            if (shouldPause) {
                player.pause()
            } else {
                player.setPlayWhenReady(true)
            }
        })
        viewModel.seekFromCurrentPlusMs.observe(this, Observer<Int> { seekValue ->
            player.seekTo(player.contentPosition + seekValue)
        })
        viewModel.setVolumePlus.observe(this, Observer<Float> { volumeDeltaValue ->
            var volumeDelta = volumeDeltaValue
            if (volumeDelta < 0) volumeDelta = 0f
            else if (volumeDelta > 1) volumeDelta = 1f
            player.volume = volumeDelta
        })
    }

    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                viewBinding.videoView.player = exoPlayer
            }.also {
                val mediaItem = MediaItem.fromUri(getString(R.string.video_url))
                it.setMediaItem(mediaItem)
            }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocationAndStartLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location == null) throw RuntimeException("Location is null")
            location?.let {
                //Report initial location to viewModel
                viewModel.onInitialLocationSet(it)
                startLocationUpdates()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
        shakeDetector.pause()
    }

    override fun onResume() {
        super.onResume()
        getLastLocationAndStartLocationUpdates()
        shakeDetector.resume()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
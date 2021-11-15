package com.alexb.testexoplayerapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alexb.testexoplayerapp.R
import com.alexb.testexoplayerapp.databinding.ActivityMainBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer

class MainActivity : AppCompatActivity() {

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }
    lateinit var player: SimpleExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        initializePlayer()
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
}
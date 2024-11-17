package com.example.myapplication

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.C

class PlayerActivity : AppCompatActivity() {
    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var loadingIndicator: ProgressBar
    private var videoUrl: String = ""
    private var lastPosition: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.player_view)
        loadingIndicator = findViewById(R.id.loading_indicator)
        
        // Setup back button
        playerView.findViewById<ImageButton>(R.id.btn_back)?.setOnClickListener {
            finish()
        }

        videoUrl = intent.getStringExtra("videoUrl") ?: ""
        if (videoUrl.isEmpty()) {
            Toast.makeText(this, "Invalid video URL", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Retrieve last position from VideoProgress
        lastPosition = VideoProgress.getProgress(this, videoUrl)
        
        setupPlayer()
    }

    private fun setupPlayer() {
        val drmKey = intent.getStringExtra("drmKey")
        
        player = ExoPlayer.Builder(this).build().apply {
            playerView.player = this
            
            if (drmKey != null) {
                val mediaItem = MediaItem.Builder()
                    .setUri(videoUrl)
                    .setDrmConfiguration(
                        MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
                            .setLicenseUri(intent.getStringExtra("licenseUrl"))
                            .setMultiSession(true)
                            .setKeySetId(drmKey.toByteArray())
                            .build()
                    )
                    .build()
                setMediaItem(mediaItem)
            } else {
                setMediaItem(MediaItem.fromUri(videoUrl))
            }
            
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_BUFFERING -> loadingIndicator.visibility = android.view.View.VISIBLE
                        Player.STATE_READY -> {
                            loadingIndicator.visibility = android.view.View.GONE
                            // Restore position if it exists and we haven't seeked yet
                            if (lastPosition > 0 && currentPosition == 0L) {
                                seekTo(lastPosition)
                            }
                        }
                        Player.STATE_ENDED -> {
                            // Clear progress when video ends naturally
                            VideoProgress.clearProgress(this@PlayerActivity, videoUrl)
                            finish()
                        }
                    }
                }

                override fun onPositionDiscontinuity(
                    oldPosition: Player.PositionInfo,
                    newPosition: Player.PositionInfo,
                    reason: Int
                ) {
                    // Save position when user manually seeks
                    if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                        VideoProgress.saveProgress(this@PlayerActivity, videoUrl, currentPosition)
                    }
                }
            })
            
            prepare()
            playWhenReady = true
        }
    }

    override fun onPause() {
        super.onPause()
        player.playWhenReady = false
        saveCurrentProgress()
    }

    override fun onStop() {
        super.onStop()
        saveCurrentProgress()
    }

    override fun onDestroy() {
        saveCurrentProgress()
        player.release()
        super.onDestroy()
    }

    private fun saveCurrentProgress() {
        if (player.playbackState != Player.STATE_ENDED && player.currentPosition > 0) {
            VideoProgress.saveProgress(this, videoUrl, player.currentPosition)
        }
    }
}
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

class PlayerActivity : AppCompatActivity() {
    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var loadingIndicator: ProgressBar
    private var videoUrl: String = ""
    private var currentPosition: Long = 0

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

        // Recuperar la última posición guardada
        currentPosition = VideoProgress.getProgress(this, videoUrl)
        
        setupPlayer()
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this).build().apply {
            playerView.player = this
            setMediaItem(MediaItem.fromUri(videoUrl))
            
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_BUFFERING -> loadingIndicator.visibility = android.view.View.VISIBLE
                        Player.STATE_READY -> {
                            loadingIndicator.visibility = android.view.View.GONE
                            // Solo restauramos la posición la primera vez
                            if (currentPosition > 0 && player.currentPosition == 0L) {
                                seekTo(currentPosition)
                            }
                        }
                        Player.STATE_ENDED -> {
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
                    // Guardamos la posición cuando el usuario hace seek manualmente
                    if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                        currentPosition = player.currentPosition
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
        if (player.isPlaying) {
            player.playWhenReady = false
        }
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
        if (::player.isInitialized && 
            player.playbackState != Player.STATE_ENDED && 
            player.currentPosition > 0) {
            currentPosition = player.currentPosition
            VideoProgress.saveProgress(this, videoUrl, currentPosition)
        }
    }
}
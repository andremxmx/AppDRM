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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.player_view)
        loadingIndicator = findViewById(R.id.loading_indicator)
        
        // Setup back button
        playerView.findViewById<ImageButton>(R.id.btn_back)?.setOnClickListener {
            finish()
        }

        val videoUrl = intent.getStringExtra("videoUrl") ?: ""
        if (videoUrl.isEmpty()) {
            Toast.makeText(this, "Invalid video URL", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize player with custom controls
        player = ExoPlayer.Builder(this).build().apply {
            playerView.player = this
            
            // Configure media item
            setMediaItem(MediaItem.fromUri(videoUrl))
            
            // Add listener for loading state
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_BUFFERING -> loadingIndicator.visibility = android.view.View.VISIBLE
                        Player.STATE_READY -> loadingIndicator.visibility = android.view.View.GONE
                        Player.STATE_ENDED -> finish()
                    }
                }
            })
            
            // Prepare and play
            prepare()
            playWhenReady = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}
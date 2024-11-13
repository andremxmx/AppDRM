package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector
import com.google.android.exoplayer2.DefaultRenderersFactory

class VideoPlayerActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var loadingIndicator: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_video_player)
            playerView = findViewById(R.id.video_player)
            loadingIndicator = findViewById(R.id.loading_indicator)
            
            // Setup navigation buttons with proper view casting
            playerView.findViewById<Button>(R.id.btn_back_to_movies)?.also { button ->
                button.setOnClickListener {
                    finish()
                }
            }
            
            playerView.findViewById<Button>(R.id.btn_back_to_home)?.also { button ->
                button.setOnClickListener {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                }
            }
            
            findViewById<ImageButton>(R.id.back_button)?.setOnClickListener {
                finish()
            }

            val videoUrl = intent.getStringExtra("video_url")
            if (videoUrl.isNullOrEmpty()) {
                Toast.makeText(this, "Invalid video URL", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            setupPlayer(videoUrl)
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing player: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupPlayer(videoUrl: String) {
        try {
            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent("MyApplication/1.0")
                .setConnectTimeoutMs(30000) // Increased timeout
                .setReadTimeoutMs(30000)
                .setAllowCrossProtocolRedirects(true)

            val loadControl: LoadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    32 * 1024, // Increased minimum buffer
                    64 * 1024, // Increased maximum buffer
                    2500, // Buffer for playback
                    5000 // Buffer for playback after rebuffer
                )
                .setPrioritizeTimeOverSizeThresholds(true)
                .build()

            val renderersFactory = DefaultRenderersFactory(this)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
                .setEnableDecoderFallback(true)

            val mediaItem = MediaItem.Builder()
                .setUri(videoUrl)
                .setMimeType(when {
                    videoUrl.endsWith(".mp4") -> "video/mp4"
                    videoUrl.endsWith(".mkv") -> "video/x-matroska"
                    videoUrl.endsWith(".m3u8") -> "application/x-mpegURL"
                    videoUrl.endsWith(".mpd") -> "application/dash+xml"
                    else -> null
                })
                .build()

            val mediaSourceFactory = when {
                videoUrl.contains(".m3u8") -> HlsMediaSource.Factory(dataSourceFactory)
                videoUrl.contains(".mpd") -> DashMediaSource.Factory(dataSourceFactory)
                else -> ProgressiveMediaSource.Factory(dataSourceFactory)
            }

            player = ExoPlayer.Builder(this)
                .setRenderersFactory(renderersFactory)
                .setLoadControl(loadControl)
                .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
                .build()
                .also { exoPlayer ->
                    playerView.player = exoPlayer
                    exoPlayer.addListener(object : Player.Listener {
                        override fun onPlayerError(error: PlaybackException) {
                            Toast.makeText(this@VideoPlayerActivity,
                                "Error playing video: ${error.message}",
                                Toast.LENGTH_LONG).show()
                        }
                        
                        override fun onPlaybackStateChanged(state: Int) {
                            when (state) {
                                Player.STATE_IDLE -> {
                                    // Handle idle state
                                }
                                Player.STATE_BUFFERING -> {
                                    // Handle buffering state
                                }
                                Player.STATE_READY -> {
                                    // Video is ready to play
                                }
                                Player.STATE_ENDED -> {
                                    finish()
                                }
                            }
                        }
                    })
                    exoPlayer.setMediaSource(mediaSourceFactory.createMediaSource(mediaItem))
                    exoPlayer.prepare()
                    exoPlayer.playWhenReady = true
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error setting up player: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
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
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DataSource

// Add these imports
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager
import com.google.android.exoplayer2.drm.FrameworkMediaDrm
import com.google.android.exoplayer2.drm.LocalMediaDrmCallback
import com.google.android.exoplayer2.source.MediaSource
import android.net.Uri
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import okhttp3.OkHttpClient
import com.google.android.exoplayer2.upstream.LoadErrorHandlingPolicy
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.DefaultLoadErrorHandlingPolicy

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
                .setUserAgent("ExoPlayer")
                .setConnectTimeoutMs(15000)
                .setReadTimeoutMs(15000)
                .setAllowCrossProtocolRedirects(true)
                .setDefaultRequestProperties(mapOf(
                    "Accept" to "*/*",
                    "Accept-Encoding" to "gzip, deflate",
                    "Connection" to "keep-alive"
                ))

            val defaultDataSourceFactory = DefaultDataSource.Factory(this, dataSourceFactory)

            val mediaItem = MediaItem.Builder()
                .setUri(videoUrl)
                .setMimeType(when {
                    videoUrl.endsWith(".mpd") -> "application/dash+xml"
                    videoUrl.contains(".m3u8") -> "application/x-mpegURL"
                    else -> "video/mp4"
                })
                .build()

            val errorHandlingPolicy = object : DefaultLoadErrorHandlingPolicy() {
                override fun getRetryDelayMsFor(
                    loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo
                ): Long {
                    // Retry for HTTP errors
                    return if (loadErrorInfo.exception is HttpDataSource.HttpDataSourceException) {
                        // Retry after 3 seconds
                        3000
                    } else {
                        C.TIME_UNSET
                    }
                }

                override fun getMinimumLoadableRetryCount(dataType: Int): Int {
                    return 3 // Number of retry attempts
                }
            }

            val mediaSourceFactory = DefaultMediaSourceFactory(defaultDataSourceFactory)
                .setLoadErrorHandlingPolicy(errorHandlingPolicy)

            player = ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory)
                .build()
                .also { exoPlayer ->
                    playerView.player = exoPlayer
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                    exoPlayer.playWhenReady = true
                }

        } catch (e: Exception) {
            Toast.makeText(this, "Error setting up player: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun createDashDrmMediaSource(
        videoUrl: String,
        dataSourceFactory: DefaultDataSource.Factory,
        drmKey: String
    ): MediaSource {
        val (k, kid) = drmKey.split(":")
        val licenseJson = """
            {
                "keys": [{
                    "kty": "oct",
                    "kid": "$kid",
                    "k": "$k"
                }],
                "type": "temporary"
            }
        """.trimIndent().toByteArray()

        val drmCallback = LocalMediaDrmCallback(licenseJson)
        val drmSessionManager = DefaultDrmSessionManager.Builder()
            .setUuidAndExoMediaDrmProvider(C.CLEARKEY_UUID) { uuid ->
                FrameworkMediaDrm.newInstance(uuid)
            }
            .build(drmCallback)

        return DashMediaSource.Factory(dataSourceFactory)
            .setDrmSessionManagerProvider { drmSessionManager }
            .createMediaSource(MediaItem.fromUri(videoUrl))
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
}
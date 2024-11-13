package com.example.myapplication

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.slidingpanelayout.widget.SlidingPaneLayout
import androidx.slidingpanelayout.widget.SlidingPaneLayout.PanelSlideListener
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.drm.*
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import android.graphics.Color
import android.graphics.Rect
import android.widget.Button
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import android.util.Log
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.Player
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import android.net.Uri
//import com.google.android.exoplayer2.ui.R as ExoPlayerR  // Añadir este import al inicio

class MainActivity : AppCompatActivity(), Player.Listener {

    private lateinit var playerView: PlayerView
    private var player: ExoPlayer? = null
    private lateinit var channelsRecyclerView: RecyclerView
    private val channels = mutableListOf<Channel>()
    private lateinit var menuButton: ImageButton
    private var currentChannelPosition = 0
    private lateinit var trackSelector: DefaultTrackSelector
    private var libVLC: LibVLC? = null
    private var vlcPlayer: MediaPlayer? = null
    private lateinit var vlcVideoLayout: VLCVideoLayout
    private var isVLCPlayer = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.player_view)
        vlcVideoLayout = findViewById(R.id.vlc_video_layout)
        channelsRecyclerView = findViewById(R.id.channels_recycler)

        setupPlayerAndChannels()
        setupPlayerControls()
        loadChannels()
    }

    private fun setupPlayerControls() {
        // Find control views for both ExoPlayer and VLC
        val exoControls = playerView.findViewById<View>(R.id.controls_container)
        val vlcControls = findViewById<View>(R.id.vlc_controls)?.findViewById<View>(R.id.controls_container)

        // Setup ExoPlayer controls
        setupControlButtons(exoControls)
        if (!isVLCPlayer) {
            exoControls?.visibility = View.VISIBLE
        }

        // Setup VLC controls
        setupControlButtons(vlcControls)
        if (isVLCPlayer) {
            vlcControls?.visibility = View.VISIBLE
        }
    }

    private fun setupControlButtons(controlsView: View?) {
        controlsView?.apply {
            findViewById<Button>(R.id.btn_channels)?.apply {
                setOnClickListener { showChannelList() }
                setOnFocusChangeListener { v, hasFocus ->
                    v.alpha = if (hasFocus) 1f else 0.8f
                    v.scaleX = if (hasFocus) 1.05f else 1f
                    v.scaleY = if (hasFocus) 1.05f else 1f
                }
            }

            findViewById<Button>(R.id.btn_back)?.apply {
                setOnClickListener {
                    if (channelsRecyclerView.visibility == View.VISIBLE) {
                        hideChannelList()
                    } else {
                        finish()
                    }
                }
                setOnFocusChangeListener { v, hasFocus ->
                    v.alpha = if (hasFocus) 1f else 0.8f
                    v.scaleX = if (hasFocus) 1.1f else 1f
                    v.scaleY = if (hasFocus) 1.1f else 1f
                }
            }

            findViewById<ImageButton>(R.id.btn_play_pause)?.apply {
                setOnClickListener {
                    if (isVLCPlayer) {
                        toggleVLCPlayback(this)
                    } else {
                        toggleExoPlayback(this)
                    }
                }
                setOnFocusChangeListener { v, hasFocus ->
                    v.alpha = if (hasFocus) 1f else 0.8f
                    v.scaleX = if (hasFocus) 1.1f else 1f
                    v.scaleY = if (hasFocus) 1.1f else 1f
                }
            }
        }
    }

    private fun toggleExoPlayback(button: ImageButton) {
        player?.let {
            if (it.isPlaying) {
                it.pause()
                button.setImageResource(R.drawable.ic_play)
            } else {
                it.play()
                button.setImageResource(R.drawable.ic_pause)
            }
        }
    }

    private fun toggleVLCPlayback(button: ImageButton) {
        vlcPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                button.setImageResource(R.drawable.ic_play)
            } else {
                it.play()
                button.setImageResource(R.drawable.ic_pause)
            }
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        // Update play/pause button state
        val button = if (isVLCPlayer) {
            findViewById<View>(R.id.vlc_controls)?.findViewById<ImageButton>(R.id.btn_play_pause)
        } else {
            playerView.findViewById<ImageButton>(R.id.btn_play_pause)
        }
        
        button?.setImageResource(
            if (playWhenReady && playbackState == Player.STATE_READY) {
                R.drawable.ic_pause
            } else {
                R.drawable.ic_play
            }
        )
    }

    private fun setupPlayerAndChannels() {
        channelsRecyclerView.layoutManager = LinearLayoutManager(this)
        channelsRecyclerView.adapter = ChannelAdapter(channels) { channel ->
            updatePlayer(channel)
            hideChannelList()
        }
    }

    private fun showChannelList() {
        channelsRecyclerView.visibility = View.VISIBLE
        // Asegurar que el focus vaya al canal actual
        channelsRecyclerView.post {
            channelsRecyclerView.layoutManager?.findViewByPosition(currentChannelPosition)?.requestFocus()
        }
    }

    private fun hideChannelList() {
        channelsRecyclerView.visibility = View.GONE
        if (isVLCPlayer) {
            findViewById<View>(R.id.vlc_controls)?.findViewById<Button>(R.id.btn_channels)?.requestFocus()
        } else {
            playerView.findViewById<Button>(R.id.btn_channels)?.requestFocus()
        }
    }

    private fun loadChannels() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = URL("https://dslive.site/json/channels.json").readText()
                val jsonObject = JSONObject(response)
                val channelsList = mutableListOf<Channel>()

                jsonObject.keys().forEach { key ->
                    val channelObj = jsonObject.getJSONObject(key)
                    channelsList.add(Channel(
                        name = channelObj.getString("name"),
                        logo = channelObj.getString("logo"),
                        playbackUrl = channelObj.getString("playbackUrl"),
                        key = channelObj.getString("key")
                    ))
                }

                withContext(Dispatchers.Main) {
                    channels.clear()
                    channels.addAll(channelsList)
                    channelsRecyclerView.adapter?.notifyDataSetChanged()
                    if (channels.isNotEmpty()) {
                        updatePlayer(channels[0])
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updatePlayer(channel: Channel) {
        try {
            // Primero intentar con ExoPlayer para canales con DRM
            if (channel.key.isNotEmpty()) {
                Log.d("Player", "Using ExoPlayer for DRM content")
                setupExoPlayer(channel)
            } else {
                // Para canales sin DRM, usar VLC
                Log.d("Player", "Using VLC player for non-DRM content")
                setupVLCPlayer(channel.playbackUrl)
            }
            currentChannelPosition = channels.indexOf(channel)
        } catch (e: Exception) {
            Log.e("Player", "Error updating player", e)
            e.printStackTrace()
        }
    }

    private fun setupVLCPlayer(url: String) {
        releasePlayer()
        releaseVLCPlayer()
        
        try {
            val options = ArrayList<String>().apply {
                // Opciones básicas para iniciar
                add("-vv")
                add("--aout=opensles")
                add("--audio-time-stretch")
                add("--avcodec-skiploopfilter")
                add("--avcodec-skip-frame")
                add("--avcodec-skip-idct")
                add("--no-drop-late-frames")
                add("--no-skip-frames")
                add("--rtsp-tcp")
            }
            
            libVLC = LibVLC(applicationContext, options)
            vlcPlayer = MediaPlayer(libVLC)
            
            vlcPlayer?.setEventListener { event ->
                when (event.type) {
                    MediaPlayer.Event.EncounteredError -> {
                        Log.e("VLCPlayer", "Error playing media")
                        runOnUiThread {
                            // Si falla VLC, intentar con ExoPlayer
                            setupExoPlayer(Channel("", "", url, ""))
                        }
                    }
                    MediaPlayer.Event.Playing -> {
                        Log.d("VLCPlayer", "Media playing")
                        runOnUiThread {
                            findViewById<View>(R.id.vlc_controls)?.visibility = View.VISIBLE
                            setupVLCControls(findViewById(R.id.vlc_controls))
                        }
                    }
                }
            }

            playerView.visibility = View.GONE
            val vlcContainer = findViewById<View>(R.id.vlc_container)
            vlcContainer.visibility = View.VISIBLE
            vlcVideoLayout.visibility = View.VISIBLE
            
            vlcPlayer?.apply {
                attachViews(vlcVideoLayout, null, false, false)
                val media = Media(libVLC, Uri.parse(url)).apply {
                    // Configuración básica del medio
                    setHWDecoderEnabled(true, true)
                    addOption(":network-caching=1500")
                    addOption(":live-caching=1500")
                }
                this.media = media
                media.release()
                play()
            }
            
            isVLCPlayer = true
            findViewById<View>(R.id.vlc_controls)?.visibility = View.VISIBLE
            setupPlayerControls()
            
        } catch (e: Exception) {
            Log.e("VLCPlayer", "Error setting up VLC player", e)
            setupExoPlayer(Channel("", "", url, ""))
        }
    }

    private fun setupVLCControls(controls: View) {
        controls.findViewById<View>(R.id.controls_container)?.visibility = View.VISIBLE
        
        controls.findViewById<Button>(R.id.btn_channels)?.apply {
            visibility = View.VISIBLE
            setOnClickListener { showChannelList() }
        }

        controls.findViewById<Button>(R.id.btn_back)?.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                if (channelsRecyclerView.visibility == View.VISIBLE) {
                    hideChannelList()
                } else {
                    finish()
                }
            }
        }

        controls.findViewById<ImageButton>(R.id.btn_play_pause)?.apply {
            visibility = View.VISIBLE
            setImageResource(if (vlcPlayer?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play)
            setOnClickListener {
                if (vlcPlayer?.isPlaying == true) {
                    vlcPlayer?.pause()
                    setImageResource(R.drawable.ic_play)
                } else {
                    vlcPlayer?.play()
                    setImageResource(R.drawable.ic_pause)
                }
            }
        }

        // Keep controls always visible
        controls.findViewById<View>(R.id.controls_container)?.apply {
            alpha = 1f
            visibility = View.VISIBLE
        }
    }

    private fun setupExoPlayer(channel: Channel) {
        releaseVLCPlayer()
        playerView.visibility = View.VISIBLE
        vlcVideoLayout.visibility = View.GONE
        
        releasePlayer()
        initializePlayer()

        val dataSourceFactory = DefaultDataSource.Factory(this)
        val mediaSource = when {
            channel.playbackUrl.endsWith(".mpd") -> {
                if (channel.key.isNotEmpty()) {
                    createDashDrmMediaSource(channel, dataSourceFactory)
                } else {
                    DashMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(channel.playbackUrl))
                }
            }
            channel.playbackUrl.endsWith(".m3u8") -> {
                HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(channel.playbackUrl))
            }
            channel.playbackUrl.endsWith(".ism") -> {
                SsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(channel.playbackUrl))
            }
            else -> {
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(channel.playbackUrl))
            }
        }

        player?.setMediaSource(mediaSource)
        player?.prepare()
        player?.playWhenReady = true
        isVLCPlayer = false
    }

    private fun releaseVLCPlayer() {
        findViewById<View>(R.id.vlc_container).visibility = View.GONE
        vlcPlayer?.apply {
            stop()
            detachViews()
            release()
        }
        libVLC?.release()
        vlcPlayer = null
        libVLC = null
    }

    private fun createDashDrmMediaSource(
        channel: Channel,
        dataSourceFactory: DefaultDataSource.Factory
    ): MediaSource {
        val (k, kid) = channel.key.split(":")
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
            .createMediaSource(MediaItem.fromUri(channel.playbackUrl))
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.release()
        }
        player = null
    }

    private fun initializePlayer() {
        if (player == null) {
            // Configure renderer factory with fallback decoders
            val renderersFactory = DefaultRenderersFactory(this)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)

            // Configure track selector
            trackSelector = DefaultTrackSelector(this)
            val trackSelectorParameters = trackSelector.buildUponParameters()
                .setMaxVideoSizeSd() // Limit to SD to prevent codec issues
                .build()
            trackSelector.parameters = trackSelectorParameters

            // Create player with configurations
            player = ExoPlayer.Builder(this)
                .setRenderersFactory(renderersFactory)
                .setTrackSelector(trackSelector)
                .build()
                .apply {
                    playerView.player = this
                    addListener(this@MainActivity)  // Add this line to register the listener
                }
        }
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        player?.play()
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onStop() {
        super.onStop()
        if (isVLCPlayer) {
            releaseVLCPlayer()
        } else {
            releasePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseVLCPlayer()
        releasePlayer()
    }

    data class Channel(
        val name: String,
        val logo: String,
        val playbackUrl: String,
        val key: String
    )

    class ChannelAdapter(
        private val channels: List<Channel>,
        private val onChannelSelected: (Channel) -> Unit
    ) : RecyclerView.Adapter<ChannelAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val logo: ImageView = itemView.findViewById(R.id.channel_logo)
            val name: TextView = itemView.findViewById(R.id.channel_name)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.channel_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val channel = channels[position]
            holder.name.text = channel.name
            Glide.with(holder.logo)
                .load(channel.logo)
                .into(holder.logo)

            holder.itemView.setOnClickListener {
                onChannelSelected(channel)
            }

            holder.itemView.setOnKeyListener { v, keyCode, event ->
                when {
                    event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_CENTER -> {
                        onChannelSelected(channel)
                        true
                    }
                    event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        // Cerrar la lista al navegar hacia la derecha
                        (v.context as? MainActivity)?.hideChannelList()
                        true
                    }
                    else -> false
                }
            }

            holder.itemView.setOnFocusChangeListener { v, hasFocus ->
                // Animación de escala
                val scale = if (hasFocus) 1.05f else 1.0f
                val alpha = if (hasFocus) 1f else 0.8f

                v.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .alpha(alpha)
                    .setDuration(200)
                    .start()

                // Cambiar color del texto
                holder.name.setTextColor(
                    if (hasFocus) Color.WHITE else Color.parseColor("#CCCCCC")
                )

                // Efecto de elevación
                v.elevation = if (hasFocus) 8f else 0f
            }
        }

        override fun getItemCount() = channels.size
    }

    override fun onPlayerError(error: PlaybackException) {
        when (error.errorCode) {
            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> {
                Log.e("PlayerError", "Source error: ${error.message}")
            }
            PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> {  // Changed this line
                Log.e("PlayerError", "Renderer error: ${error.message}")
                // Try to fall back to software decoder
                val parameters = trackSelector.buildUponParameters()
                    .setPreferredVideoMimeType(MimeTypes.VIDEO_H264)
                    .build()
                trackSelector.parameters = parameters
            }
            else -> {
                Log.e("PlayerError", "Unexpected error: ${error.message}")
            }
        }
    }
}

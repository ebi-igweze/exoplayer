package com.example.cyberacademy.myapplication

import android.content.Context
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var playerHolder: PlayerHolder
    private val state = PlayerState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerHolder = PlayerHolder(this, exoplayerview_activity_video, state)
    }

    override fun onStart() {
        super.onStart()
        playerHolder.start()
    }

    override fun onStop() {
        super.onStop()
        playerHolder.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerHolder.release()
    }
}


data class PlayerState(var window: Int = 0, var position: Long = 0, var whenReady: Boolean = true)

class PlayerHolder(val context: Context, private val playerView: PlayerView, val playerState: PlayerState) {

    private val player: ExoPlayer

    init {
        player = ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector())
        playerView.player = player
    }

    fun start() {
        // parse uri
        val uri = Uri.parse("http://ftp.nluug.nl/pub/graphics/blender/demo/movies/ToS/ToS-4k-1920.mov")

        // user agent string
        val userAgent  = Util.getUserAgent(context, "ExoPlayer")

        // build media source
        val mediaSource = ExtractorMediaSource
                .Factory(DefaultDataSourceFactory(context, userAgent))
                .createMediaSource(uri)

        // Load Media
        player.prepare(mediaSource)

        // setup player to play when ready and seek to previous position if any.
        with(playerState) {
            player.playWhenReady = whenReady
            player.seekTo(window, position)
        }
    }

    fun stop() {
        // save current playback state
        with(playerState) {
            window = player.currentWindowIndex
            position = player.currentPosition
            whenReady = player.playWhenReady
        }

        // stop playback
        player.stop()
    }

    fun release() {
        player.release()
    }
}

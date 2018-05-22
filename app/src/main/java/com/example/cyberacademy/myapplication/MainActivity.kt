package com.example.cyberacademy.myapplication

import android.content.Context
import android.media.AudioManager
import android.media.session.MediaSession
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var playerHolder: PlayerHolder
    private val state = PlayerState()
    val playList = arrayListOf(
            with(MediaDescriptionCompat.Builder()) {
                setDescription("MP4 loaded over HTTP")
                setMediaId("1")
                // License - https://peach.blender.org/download/
                setMediaUri(Uri.parse("http://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4"))
                setTitle("Short film Big Buck Bunny")
                setSubtitle("Streaming video")
                build()
            })

    private val mediaSession: MediaSessionCompat by lazy { createMediaSession() }
    private val mediaSessionConnector: MediaSessionConnector by lazy { createMediaSessionConnector() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // while user is in app, volume controls should adjust video volume
        volumeControlStream = AudioManager.STREAM_MUSIC
        playerHolder = PlayerHolder(this, exoplayerview_activity_video, state, playList)
    }

    override fun onStart() {
        super.onStart()
        playerHolder.start()
        mediaSessionConnector.setPlayer(playerHolder.player, null)
        mediaSession.isActive = true
    }

    override fun onStop() {
        super.onStop()
        playerHolder.stop()
        mediaSessionConnector.setPlayer(null, null)
        mediaSession.isActive = false
    }

    override fun onDestroy() {
        super.onDestroy()
        playerHolder.release()
        mediaSession.release()
    }

    private fun createMediaSession(): MediaSessionCompat {
        return MediaSessionCompat(this, packageName)
    }

    private fun createMediaSessionConnector(): MediaSessionConnector {
        val mediaConnector = MediaSessionConnector(mediaSession)
        mediaConnector.setQueueNavigator(object : TimelineQueueNavigator(mediaSession) {
            override fun getMediaDescription(windowIndex: Int): MediaDescriptionCompat {
                return playList[windowIndex]
            }
        })

        return mediaConnector
    }
}


data class PlayerState(var window: Int = 0, var position: Long = 0, var whenReady: Boolean = true)

class PlayerHolder(val context: Context, private val playerView: PlayerView, val playerState: PlayerState, val playList: List<MediaDescriptionCompat>) {

    val player: ExoPlayer

    init {
        player = ExoPlayerFactory.newSimpleInstance(context, DefaultTrackSelector())
        playerView.player = player
    }

    fun start() {
        val urilist = playList.map {

            // user agent string
            val userAgent  = Util.getUserAgent(context, "ExoPlayer")

            // build media source
            val mediaSource = ExtractorMediaSource
                    .Factory(DefaultDataSourceFactory(context, userAgent))
                    .createMediaSource(it.mediaUri)
            mediaSource
        }

        // create playlist media source
        val mediaSources = ConcatenatingMediaSource(*urilist.toTypedArray())

        // Load Media
        player.prepare(mediaSources)

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

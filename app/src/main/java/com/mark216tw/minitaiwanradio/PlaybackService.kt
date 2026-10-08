package com.mark216tw.minitaiwanradio

import android.app.PendingIntent
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlaybackService : MediaSessionService() {
    private lateinit var player: ExoPlayer
    private var mediaSession: MediaSession? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val sleepTimerRunnable = Runnable {
        sleepTimerEndElapsedRealtimeMutable.value = null
        player.stop()
        stopSelf()
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "Radio stream playback failed", error)
                cancelSleepTimer()
                player.pause()
            }
        })
        val sessionActivity = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivity)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val streamUrl = intent.getStringExtra(EXTRA_STREAM_URL).orEmpty()
                val stationName = intent.getStringExtra(EXTRA_STATION_NAME).orEmpty()
                val stationId = intent.getStringExtra(EXTRA_STATION_ID).orEmpty()
                if (streamUrl.isBlank()) return super.onStartCommand(intent, flags, startId)
                runCatching {
                    player.setMediaItem(
                        MediaItem.Builder()
                            .setMediaId(stationId)
                            .setUri(streamUrl)
                            .setMediaMetadata(
                                androidx.media3.common.MediaMetadata.Builder()
                                    .setTitle(stationName)
                                    .setArtist("mini台灣電台")
                                    .build(),
                            )
                            .build(),
                    )
                    player.prepare()
                    player.play()
                }.onFailure { Log.e(TAG, "Unable to start radio stream", it) }
            }

            ACTION_STOP -> {
                cancelSleepTimer()
                player.stop()
                stopSelf()
            }

            ACTION_SET_SLEEP_TIMER -> {
                val durationMillis = intent.getLongExtra(EXTRA_SLEEP_TIMER_DURATION_MILLIS, 0L)
                if (durationMillis > 0L) setSleepTimer(durationMillis)
            }

            ACTION_CANCEL_SLEEP_TIMER -> cancelSleepTimer()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        cancelSleepTimer()
        player.stop()
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        cancelSleepTimer()
        mediaSession?.release()
        player.release()
        super.onDestroy()
    }

    private fun setSleepTimer(durationMillis: Long) {
        mainHandler.removeCallbacks(sleepTimerRunnable)
        sleepTimerEndElapsedRealtimeMutable.value = SystemClock.elapsedRealtime() + durationMillis
        mainHandler.postDelayed(sleepTimerRunnable, durationMillis)
    }

    private fun cancelSleepTimer() {
        mainHandler.removeCallbacks(sleepTimerRunnable)
        sleepTimerEndElapsedRealtimeMutable.value = null
    }

    companion object {
        private const val TAG = "PlaybackService"
        const val ACTION_PLAY = "com.mark216tw.minitaiwanradio.PLAY"
        const val ACTION_STOP = "com.mark216tw.minitaiwanradio.STOP"
        const val ACTION_SET_SLEEP_TIMER = "com.mark216tw.minitaiwanradio.SET_SLEEP_TIMER"
        const val ACTION_CANCEL_SLEEP_TIMER = "com.mark216tw.minitaiwanradio.CANCEL_SLEEP_TIMER"
        const val EXTRA_STREAM_URL = "streamUrl"
        const val EXTRA_STATION_NAME = "stationName"
        const val EXTRA_STATION_ID = "stationId"
        const val EXTRA_SLEEP_TIMER_DURATION_MILLIS = "sleepTimerDurationMillis"

        private val sleepTimerEndElapsedRealtimeMutable = MutableStateFlow<Long?>(null)
        val sleepTimerEndElapsedRealtime = sleepTimerEndElapsedRealtimeMutable.asStateFlow()
    }
}

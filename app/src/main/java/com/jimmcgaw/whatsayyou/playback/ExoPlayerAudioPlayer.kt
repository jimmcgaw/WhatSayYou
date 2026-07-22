package com.jimmcgaw.whatsayyou.playback

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * ExoPlayer doesn't push continuous playback-position updates, so this polls
 * player.currentPosition on a timer while playing.
 */
class ExoPlayerAudioPlayer(context: Context) : AudioPlayer {

    private val player = ExoPlayer.Builder(context).build()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var positionPollingJob: Job? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) startPositionPolling() else positionPollingJob?.cancel()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _playbackState.update { it.copy(durationMs = player.duration.coerceAtLeast(0)) }
                }
            }
        })
    }

    override fun prepare(filePath: String) {
        player.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(filePath))))
        player.prepare()
    }

    override fun play() {
        player.play()
    }

    override fun pause() {
        player.pause()
    }

    override fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        _playbackState.update { it.copy(positionMs = positionMs) }
    }

    override fun release() {
        positionPollingJob?.cancel()
        player.release()
    }

    private fun startPositionPolling() {
        positionPollingJob?.cancel()
        positionPollingJob = scope.launch {
            while (isActive) {
                _playbackState.update { it.copy(positionMs = player.currentPosition) }
                delay(POSITION_POLL_INTERVAL_MS)
            }
        }
    }

    companion object {
        private const val POSITION_POLL_INTERVAL_MS = 200L
    }
}

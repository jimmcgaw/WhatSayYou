package com.jimmcgaw.whatsayyou.playback

import kotlinx.coroutines.flow.StateFlow

data class PlaybackState(
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
)

interface AudioPlayer {
    val playbackState: StateFlow<PlaybackState>

    fun prepare(filePath: String)
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun release()
}

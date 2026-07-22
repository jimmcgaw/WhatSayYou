package com.jimmcgaw.whatsayyou.playback

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeAudioPlayer : AudioPlayer {
    private val _playbackState = MutableStateFlow(PlaybackState())
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    var preparedFilePath: String? = null
        private set
    var released = false
        private set

    override fun prepare(filePath: String) {
        preparedFilePath = filePath
    }

    override fun play() {
        _playbackState.value = _playbackState.value.copy(isPlaying = true)
    }

    override fun pause() {
        _playbackState.value = _playbackState.value.copy(isPlaying = false)
    }

    override fun seekTo(positionMs: Long) {
        _playbackState.value = _playbackState.value.copy(positionMs = positionMs)
    }

    override fun release() {
        released = true
    }

    fun emit(state: PlaybackState) {
        _playbackState.value = state
    }
}

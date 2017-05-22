package com.austinv11.d4j.bot.audio

import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import sx.blah.discord.handle.audio.AudioEncodingType
import sx.blah.discord.handle.audio.IAudioProvider

//Adapted from https://github.com/sedmelluq/lavaplayer/blob/master/demo-d4j/src/main/java/com/sedmelluq/discord/lavaplayer/demo/d4j/AudioProvider.java
class LavaplayerProvider(val manager: LavaplayerManager) : IAudioProvider {
    
    val audioPlayer = manager.player
    private var lastFrame: AudioFrame? = null

    override fun isReady(): Boolean {
        if (lastFrame == null) {
            lastFrame = audioPlayer.provide()
        }

        return lastFrame != null
    }

    override fun provide(): ByteArray {
        if (lastFrame == null) {
            lastFrame = audioPlayer.provide()
        }

        val data = if (lastFrame != null) lastFrame!!.data else null
        lastFrame = null

        return data ?: byteArrayOf()
    }

    override fun getChannels(): Int {
        return 2
    }

    override fun getAudioEncodingType(): AudioEncodingType {
        return AudioEncodingType.OPUS
    }
}

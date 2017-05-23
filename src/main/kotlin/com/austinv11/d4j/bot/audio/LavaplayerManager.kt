package com.austinv11.d4j.bot.audio

import com.austinv11.d4j.bot.extensions.msToTimestamp
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.util.EmbedBuilder

private val managerPool = mutableMapOf<IGuild, LavaplayerManager>()

val IGuild.playerManager
    get() = managerPool.computeIfAbsent(this, { LavaplayerManager() })

private val manager = DefaultAudioPlayerManager().also { 
    AudioSourceManagers.registerLocalSource(it)
    AudioSourceManagers.registerRemoteSources(it) 
}

fun EmbedBuilder.appendQueueInfo(manager: LavaplayerManager): EmbedBuilder {
    withTitle("Now Playing")
    val track = manager.player.playingTrack
    if (track != null) {
        val info = track.info
        withAuthorName(info.author)
        withAuthorUrl(info.uri)
        appendField(info.title, buildString {
            val pos = ((track.position.toDouble() / track.duration.toDouble()) * 10.0).toInt()
            appendln("➖".repeat(10).replaceRange(pos, pos+1, "🔘") + " ${track.position.msToTimestamp()}/${track.duration.msToTimestamp()}")
        }, false)
    } else {
        withDesc("None.")
    }

    appendField("Queue", buildString {
        manager.queue.tracks.forEachIndexed { index, audioTrack ->
            appendln("${index+1}. [${audioTrack.info.author} - ${audioTrack.info.title}](${audioTrack.info.uri})")
        }
    }, false)
    
    return this
}

class LavaplayerManager(val playerManager: DefaultAudioPlayerManager = manager) {
    
    val player = manager.createPlayer()
    val queue = TrackQueue(player, this)
    
    init {
        player.addListener(queue)
    }

    fun resume() {
        player.isPaused = false
    }

    fun pause() {
        player.isPaused = true
    }
}


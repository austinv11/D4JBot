package com.austinv11.d4j.bot.audio

import com.austinv11.d4j.bot.command.Command
import com.austinv11.d4j.bot.extensions.buffer
import com.austinv11.d4j.bot.extensions.embed
import com.austinv11.d4j.bot.extensions.embedFor
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import sx.blah.discord.handle.obj.IMessage
import java.util.concurrent.LinkedBlockingDeque

class TrackQueue(val player: AudioPlayer, val manager: LavaplayerManager) : AudioEventAdapter() {

    val tracks = LinkedBlockingDeque<AudioTrack>()
    @Volatile var currTrack: AudioTrack? = null

    override fun onTrackEnd(player: AudioPlayer?, track: AudioTrack?, endReason: AudioTrackEndReason?) {
        if (endReason!!.mayStartNext)
            next()
    }

    override fun onTrackStuck(player: AudioPlayer?, track: AudioTrack?, thresholdMs: Long) {
        next()
    }

    override fun onTrackException(player: AudioPlayer?, track: AudioTrack?, exception: FriendlyException?) {
        next()
        exception?.printStackTrace()
    }

    fun add(context: Pair<IMessage, Command>, track: String) {
        manager.playerManager.loadItemOrdered(manager, track, object: AudioLoadResultHandler {
            
            override fun loadFailed(exception: FriendlyException?) {
                if (exception?.severity == FriendlyException.Severity.COMMON) {
                    buffer { context.first.edit(context.second.embed.withTitle("Could not load `$track`").build()) }
                } else {
                    buffer { context.first.edit(exception?.embedFor(context.second)) }
                }
            }

            override fun trackLoaded(track: AudioTrack?) {
                add(track!!)
                buffer { context.first.edit(context.second.embed.withTitle("Loaded `${track?.info?.title}`").build()) }
            }

            override fun noMatches() {
                buffer { context.first.edit(context.second.embed.withTitle("No matches found for `$track`").build()) }
            }

            override fun playlistLoaded(playlist: AudioPlaylist?) {
                playlist!!.tracks.forEach(this@TrackQueue::add)
                buffer { context.first.edit(context.second.embed.withTitle("Loaded `${playlist?.name}`").build()) }
            }

        }).get()
    }

    fun clear(): Int {
        val size = size()
        tracks.clear()
        currTrack = null
        player.stopTrack()
        return size
    }

    fun size(): Int = tracks.size + (if (currTrack == null) 0 else 1)

    fun add(track: AudioTrack) {
        tracks.add(track)
        if (currTrack == null) {
            currTrack = tracks.poll()
            player.playTrack(currTrack)
        }
    }
    
    fun next() {
        if (!tracks.isEmpty())
            currTrack = tracks.poll()
        player.playTrack(currTrack)
    }
}

package com.austinv11.d4j.bot.command.impl

import com.austinv11.d4j.bot.audio.appendQueueInfo
import com.austinv11.d4j.bot.audio.playerManager
import com.austinv11.d4j.bot.command.CommandExecutor
import com.austinv11.d4j.bot.command.Executor
import com.austinv11.d4j.bot.command.Parameter
import com.austinv11.d4j.bot.command.context
import com.austinv11.d4j.bot.extensions.buffer
import com.austinv11.d4j.bot.extensions.embed
import sx.blah.discord.util.EmbedBuilder

class QueueCommand : CommandExecutor() {

    override val name: String = "queue"
    override val aliases: Array<String> = arrayOf("enqueue")
    override val guildOnly: Boolean = true
    
    @Executor("Gets the current queue information.")
    fun execute(): EmbedBuilder = context.embed.appendQueueInfo(context.guild!!.playerManager)
    
    @Executor("Queues a track.")
    fun execute(@Parameter("The track to queue.") track: String) {
        val channel = context.channel
        channel.typingStatus = true
        val context = context
        val msg = buffer { channel.sendMessage(context.embed.withTitle("Processing...").build()) }
        context.guild!!.playerManager.queue.add(msg to context, track.removePrefix("<").removeSuffix(">"))
    }
    
    @Executor("Performs an action on the queue.")
    fun execute(@Parameter("The action to perform.") action: NoArgQueueAction): EmbedBuilder = context.embed.apply {
        when(action) {
            NoArgQueueAction.INFO -> {
                return@execute execute()
            }
            NoArgQueueAction.CLEAR -> {
                val cleared = context.guild!!.playerManager.queue.clear()
                withTitle("Cleared $cleared tracks")
            }
        }
    }

    @Executor("Performs an action on the queue.")
    fun execute(@Parameter("The action to perform.") action: OneArgQueueAction,
                @Parameter("The track to effect.") track: String): EmbedBuilder? = context.embed.apply {
        when(action) {
            OneArgQueueAction.ADD -> {
                execute(track)
                return@execute null
            }
            OneArgQueueAction.REMOVE -> {
                val did = context.guild!!.playerManager.queue.tracks.removeIf { it.info.title == track }
                if (did) {
                    withTitle("Removed")
                } else {
                    withTitle("Cannot find track `$track`")
                }
            }
        }
    }

    @Executor("Performs an action on the queue.")
    fun execute(@Parameter("The action to perform.") action: OneArgQueueAction,
                @Parameter("The track to effect.") track: Int): EmbedBuilder = context.embed.apply {
        when(action) {
            OneArgQueueAction.ADD -> {
                context.guild!!.playerManager.queue.add(context.guild!!.playerManager.queue.tracks.filterIndexed { index, audioTrack -> index == track }.first().makeClone())
                return execute()
            }
            OneArgQueueAction.REMOVE -> {
                val did = context.guild!!.playerManager.queue.size() > track
                if (did) {
                    context.guild!!.playerManager.queue.tracks.remove(context.guild!!.playerManager.queue.tracks.filterIndexed { index, audioTrack -> index == track }.first())
                    withTitle("Removed")
                } else {
                    withTitle("Cannot find track `$track`")
                }
            }
        }
    }
    
    enum class NoArgQueueAction {
        INFO, CLEAR
    }
    
    enum class OneArgQueueAction {
        ADD, REMOVE
    }
}

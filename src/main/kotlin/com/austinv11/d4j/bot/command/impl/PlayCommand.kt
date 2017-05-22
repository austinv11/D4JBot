package com.austinv11.d4j.bot.command.impl

import com.austinv11.d4j.bot.audio.appendQueueInfo
import com.austinv11.d4j.bot.audio.playerManager
import com.austinv11.d4j.bot.command.CommandExecutor
import com.austinv11.d4j.bot.command.Executor
import com.austinv11.d4j.bot.command.context
import com.austinv11.d4j.bot.extensions.embed
import sx.blah.discord.util.EmbedBuilder

class PlayCommand : CommandExecutor() {
    
    override val name: String = "play"
    override val aliases: Array<String> = arrayOf("resume")
    override val guildOnly: Boolean = true
    
    @Executor("Makes the bot play queued music.")
    fun execute(): EmbedBuilder = context.embed.appendQueueInfo(context.guild!!.playerManager).also {
        context.guild!!.playerManager.resume()
    }
}

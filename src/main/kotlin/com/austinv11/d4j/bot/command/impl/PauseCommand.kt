package com.austinv11.d4j.bot.command.impl

import com.austinv11.d4j.bot.audio.playerManager
import com.austinv11.d4j.bot.command.CommandExecutor
import com.austinv11.d4j.bot.command.Executor
import com.austinv11.d4j.bot.command.context

class PauseCommand : CommandExecutor() {
    
    override val name: String = "pause"
    override val aliases: Array<String> = arrayOf("stop")
    override val guildOnly: Boolean = true

    @Executor("Makes the bot play queued music.")
    fun execute(): Boolean = true.also { 
        context.guild!!.playerManager.pause()
    }
}

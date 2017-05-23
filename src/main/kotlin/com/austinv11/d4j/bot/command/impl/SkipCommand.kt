package com.austinv11.d4j.bot.command.impl

import com.austinv11.d4j.bot.audio.playerManager
import com.austinv11.d4j.bot.command.CommandExecutor
import com.austinv11.d4j.bot.command.Executor
import com.austinv11.d4j.bot.command.context

class SkipCommand : CommandExecutor() {
    
    override val name: String = "skip"
    override val aliases: Array<String> = arrayOf("next")
    override val guildOnly: Boolean = true
    
    @Executor("Skips the current song.")
    fun execute(): Boolean {
        context.guild!!.playerManager.queue.next()
        return true
    }
}

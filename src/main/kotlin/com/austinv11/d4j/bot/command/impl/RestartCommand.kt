package com.austinv11.d4j.bot.command.impl

import com.austinv11.d4j.bot.command.CommandExecutor
import com.austinv11.d4j.bot.command.Executor
import com.austinv11.d4j.bot.command.context
import com.austinv11.d4j.bot.extensions.buffer
import com.austinv11.d4j.bot.extensions.embed
import com.austinv11.d4j.bot.restart

class RestartCommand() : CommandExecutor() {

    override val name: String = "restart"
    override val aliases: Array<String> = arrayOf("reboot", "reset", "r")

    @Executor("Restarts down the bot.", requiresOwner = true)
    fun execute() {
        val cmd = context
        buffer { cmd.channel.sendMessage(cmd.embed.withDesc("Restarting...").build()) }
        restart()
    }
}
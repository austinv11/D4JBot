package com.austinv11.d4j.bot.command.impl

import com.austinv11.d4j.bot.command.CommandExecutor
import com.austinv11.d4j.bot.command.Executor
import com.austinv11.d4j.bot.command.context
import com.austinv11.d4j.bot.exit
import com.austinv11.d4j.bot.extensions.buffer
import com.austinv11.d4j.bot.extensions.embed

class ShutdownCommand() : CommandExecutor() {

    override val name: String = "shutdown"
    override val aliases: Array<String> = arrayOf("quit", "exit", "stop")

    @Executor("Shuts down the bot.", requiresOwner = true)
    fun execute() {
        val cmd = context
        buffer { cmd.channel.sendMessage(cmd.embed.withDesc("Shutting down...").build()) }
        exit()
    }
}
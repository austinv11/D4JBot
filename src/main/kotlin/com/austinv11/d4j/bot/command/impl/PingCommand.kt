package com.austinv11.d4j.bot.command.impl

import com.austinv11.d4j.bot.command.CommandExecutor
import com.austinv11.d4j.bot.command.Executor
import com.austinv11.d4j.bot.command.context
import java.time.ZoneId

class PingCommand : CommandExecutor() {
    override val name: String = "ping"
    override val aliases: Array<String> = arrayOf()
    
    @Executor("Checks the response time of the bot to the Discord API.")
    fun execute(): String = "Pong! (Took ${System.currentTimeMillis() - context.message.timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()}ms)"
}

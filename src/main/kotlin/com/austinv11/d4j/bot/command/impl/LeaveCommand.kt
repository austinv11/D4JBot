package com.austinv11.d4j.bot.command.impl

import com.austinv11.d4j.bot.CLIENT
import com.austinv11.d4j.bot.command.CommandExecutor
import com.austinv11.d4j.bot.command.Executor
import com.austinv11.d4j.bot.command.context

class LeaveCommand : CommandExecutor() {
    override val name: String = "leave"
    override val aliases: Array<String> = emptyArray()
    override val guildOnly: Boolean = true

    @Executor("Makes the bot leave its current voice channel.")
    fun execute(): Boolean {
        CLIENT.ourUser.getVoiceStateForGuild(context.guild).channel.leave()
        return true
    }
}

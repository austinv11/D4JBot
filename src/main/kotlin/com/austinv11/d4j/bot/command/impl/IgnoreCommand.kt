package com.austinv11.d4j.bot.command.impl

import com.austinv11.d4j.bot.CONFIG
import com.austinv11.d4j.bot.command.CommandExecutor
import com.austinv11.d4j.bot.command.Executor
import com.austinv11.d4j.bot.extensions.isIgnored
import sx.blah.discord.handle.obj.IUser

class IgnoreCommand : CommandExecutor() {

    override val name: String = "ignore"
    override val aliases: Array<String> = arrayOf()

    @Executor("Makes this bot ignore the specified user.", requiresOwner = true)
    fun execute(user: IUser): Boolean {
        user.isIgnored = true
        return true
    }
}
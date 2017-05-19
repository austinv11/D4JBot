package com.austinv11.d4j.bot.command.impl

import com.austinv11.d4j.bot.CONFIG
import com.austinv11.d4j.bot.command.CommandExecutor
import com.austinv11.d4j.bot.command.Executor
import com.austinv11.d4j.bot.extensions.isIgnored
import sx.blah.discord.handle.obj.IUser

class UnignoreCommand : CommandExecutor() {

    override val name: String = "unignore"
    override val aliases: Array<String> = arrayOf()

    @Executor("Makes this bot unignore the specified user.", requiresOwner = true)
    fun execute(user: IUser): Boolean {
        user.isIgnored = false
        return true
    }
}
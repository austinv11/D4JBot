package com.austinv11.d4j.bot.command.impl

import com.austinv11.d4j.bot.command.*
import sx.blah.discord.handle.obj.IVoiceChannel
import sx.blah.discord.handle.obj.Permissions

class JoinCommand : CommandExecutor() {
    override val name: String = "join"
    override val aliases: Array<String> = emptyArray()
    override val guildOnly: Boolean = true
    override val permissions: Array<Permissions> = super.permissions + arrayOf(Permissions.VOICE_CONNECT)
    
    @Executor("Makes the bot join your voice channel.")
    fun execute(): Boolean = execute(context.author.getVoiceStateForGuild(context.guild)?.channel ?: throw CommandException("You're not in a voice channel!"))
    
    @Executor("Makes the bot join a voice channel.")
    fun execute(@Parameter("The channel to join.") channel: IVoiceChannel): Boolean {
        if (!channel.isConnected)
            channel.join()
        
        return true
    }
}

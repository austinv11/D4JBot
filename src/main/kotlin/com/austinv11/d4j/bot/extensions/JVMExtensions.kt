package com.austinv11.d4j.bot.extensions

import com.austinv11.d4j.bot.CLIENT
import com.austinv11.d4j.bot.command.Command
import sx.blah.discord.handle.obj.*
import kotlin.reflect.KClass

fun String.coerceTo(`class`: KClass<*>, command: Command): Any? = when(`class`) {
    Float::class -> java.lang.Float.valueOf(this)
    Double::class -> java.lang.Double.valueOf(this)
    Byte::class -> java.lang.Byte.valueOf(this)
    Short::class -> java.lang.Short.valueOf(this)
    Int::class -> java.lang.Integer.valueOf(this)
    Long::class -> if (this.startsWith("-")) java.lang.Long.valueOf(this) else java.lang.Long.parseUnsignedLong(this)
    Boolean::class -> if (java.lang.Boolean.valueOf(this)) true else if (!this.equals("false", true)) null else true
    Char::class -> if (this.length > 1) null else this.first() 
    String::class -> this
    IMessage::class -> buffer {
        try {
            val id = java.lang.Long.parseUnsignedLong(this)
            return@buffer command.channel.getMessageByID(id) ?: buffer { command.guild?.getMessageByID(id) ?: buffer { CLIENT.getMessageByID(id) } }
        } catch (e: Throwable) {
            return@buffer null
        }
    }
    IVoiceChannel::class -> {
        val idString: String
        if (this.startsWith("<")) {
            idString = this.removePrefix("<#").removeSuffix(">")
        } else {
            idString = this
        }
        try {
            val id = java.lang.Long.parseUnsignedLong(idString)
            command.guild?.getVoiceChannelByID(id) ?: CLIENT.getVoiceChannelByID(id)
        } catch (e: Throwable) {
            command.guild?.getVoiceChannelsByName(this)?.firstOrNull() ?: CLIENT.voiceChannels.find { it.name == this }
        }
    }
    IChannel::class -> {
        val idString: String
        if (this.startsWith("<")) {
            idString = this.removePrefix("<#").removeSuffix(">")
        } else {
            idString = this
        }
        try {
            val id = java.lang.Long.parseUnsignedLong(idString)
            command.guild?.getChannelByID(id) ?: CLIENT.getChannelByID(id)
        } catch (e: Throwable) {
            command.guild?.getChannelsByName(this)?.firstOrNull() ?: CLIENT.channels.find { it.name == this }
        }
    }
    IGuild::class -> {
        try {
            val id = java.lang.Long.parseUnsignedLong(this)
            CLIENT.getGuildByID(id)
        } catch (e: Throwable) {
            CLIENT.guilds.find { it.name == this }
        }
    }
    IUser::class -> {
        val idString: String
        if (this.startsWith("<")) {
            idString = this.removePrefix("<@").removeSuffix(">").removePrefix("!")
        } else {
            idString = this
        }
        try {
            val id = java.lang.Long.parseUnsignedLong(idString)
            command.guild?.getUserByID(id) ?: buffer { CLIENT.getUserByID(id) }
        } catch (e: Throwable) {
            command.guild?.getUsersByName(this)?.firstOrNull() ?: CLIENT.users.find { it.name == this }
        }
    }
    IRole::class -> {
        if (this == "@everyone")
            command.guild?.everyoneRole
        else {
            val idString: String
            if (this.startsWith("<")) {
                idString = this.removePrefix("<&").removeSuffix(">")
            } else {
                idString = this
            }
            try {
                val id = java.lang.Long.parseUnsignedLong(idString)
                command.guild?.getRoleByID(id) ?: buffer { CLIENT.getRoleByID(id) }
            } catch (e: Throwable) {
                command.guild?.getRolesByName(this)?.firstOrNull() ?: CLIENT.roles.find { it.name == this }
            }
        }
    }
    StatusType::class -> StatusType.get(this)
    Permissions::class -> try { Permissions.valueOf(this) } catch (e: Throwable) { null }
    VerificationLevel::class -> VerificationLevel.valueOf(this)
    else -> null
}

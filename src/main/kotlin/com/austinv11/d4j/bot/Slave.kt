package com.austinv11.d4j.bot

import com.austinv11.d4j.bot.command.Command
import com.austinv11.d4j.bot.command.createCommand
import com.austinv11.d4j.bot.command.isCommand
import com.austinv11.d4j.bot.extensions.stream
import sx.blah.discord.Discord4J
import sx.blah.discord.api.ClientBuilder
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.impl.events.ReadyEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.impl.events.shard.ReconnectFailureEvent
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.handle.obj.IUser
import kotlin.system.exitProcess

val EXIT_CODE = 0

val CLIENT: IDiscordClient
    get() = _client!!

private var _client: IDiscordClient? = null

val OWNER: IUser
    get() = CLIENT.getUserByID(_owner)

private val _owner: Long by lazy {
    CLIENT.applicationOwner.longID
}

val LOGGER = Discord4J.Discord4JLogger("D4J Bot").apply { this.setLevel(Discord4J.Discord4JLogger.Level.DEBUG) }

fun err(msg: String) = LOGGER.error(msg)

fun warn(msg: String) = LOGGER.warn(msg)

fun info(msg: String) = LOGGER.info(msg)

fun debug(msg: String) = LOGGER.debug(msg)

fun trace(msg: String) = LOGGER.trace(msg)

fun restart() {
    LOGGER.warn("Restarting...")
    exitProcess(-1)
}

fun exit() {
    LOGGER.warn("Shutting down...")
    exitProcess(EXIT_CODE)
}

fun main(args: Array<String>) {
    (Discord4J.LOGGER as Discord4J.Discord4JLogger).setLevel(Discord4J.Discord4JLogger.Level.DEBUG)
    
    _client = with(ClientBuilder()) {
        withToken(args[0])
        withRecommendedShardCount()
    }.login()
    
    CLIENT.stream<ReconnectFailureEvent>()
            .filter { it.isShardAbandoned }
            .doOnNext { warn("Shard abandoned!") }
            .subscribe { restart() }
    
    CLIENT.stream<ReadyEvent>()
            .subscribe { info("Logged in!") }
    
    CLIENT.stream<MessageReceivedEvent>()
            .map { it.message }
            .filter(IMessage::isCommand)
            .map(IMessage::createCommand)
            .subscribe(Command::execute)
}

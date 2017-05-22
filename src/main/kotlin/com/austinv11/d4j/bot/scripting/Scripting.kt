package com.austinv11.d4j.bot.scripting

import java.io.InputStream
import java.io.OutputStream

val JAVA_IMPORTS = arrayOf("java.io", "java.lang", "java.lang.reflect", "java.math", "java.nio", "java.nio.file", 
        "java.time", "java.net", "java.time.format", "java.util", "java.util.concurrent", "java.util.concurrent.atomic", 
        "java.util.function", "java.util.regex", "java.util.stream")

val KOTLIN_IMPORTS = arrayOf("kotlin", "kotlin.annotation", "kotlin.collections", "kotlin.comparisons",
        "kotlin.concurrent", "kotlin.coroutines", "kotlin.io", "kotlin.jvm", "kotlin.jvm.functions",
        "kotlin.properties", "kotlin.ranges", "kotlin.reflect", "kotlin.reflect.jvm", "kotlin.system", "kotlin.text")

val DISCORD4J_IMPORTS = arrayOf("sx.blah.discord", "sx.blah.discord.util", "sx.blah.discord.util.audio",
        "sx.blah.discord.util.audio.events", "sx.blah.discord.util.audio.processors", "sx.blah.discord.util.audio.providers",
        "sx.blah.discord.modules", "sx.blah.discord.handle.obj", "sx.blah.discord.handle.impl.events.user",
        "sx.blah.discord.handle.impl.events.shard", "sx.blah.discord.handle.impl.events.module",
        "sx.blah.discord.handle.impl.events.guild", "sx.blah.discord.handle.impl.events.guild.voice",
        "sx.blah.discord.handle.impl.events.guild.voice.user", "sx.blah.discord.handle.impl.events.guild.role",
        "sx.blah.discord.handle.impl.events.guild.member", "sx.blah.discord.handle.impl.events.guild.channel",
        "sx.blah.discord.handle.impl.events.guild.channel.message",
        "sx.blah.discord.handle.impl.events.guild.channel.message.reaction",
        "sx.blah.discord.handle.impl.events.guild.channel.webhook", "sx.blah.discord.api", "sx.blah.discord.api.events",
        "sx.blah.discord.util.cache", "com.austinv11.rx")

val REACTOR_IMPORTS = arrayOf("reactor.core", "reactor.core.publisher", "reactor.core.scheduler")

val FUEL_IMPORTS = arrayOf("com.github.kittinunf.fuel", "com.github.kittinunf.fuel",
        "com.github.kittinunf.fuel.core")

val BOT_IMPORTS = arrayOf("com.austinv11.d4j.bot", "com.austinv11.d4j.bot.scripting", "com.austinv11.d4j.bot.extensions",
        "com.austinv11.d4j.bot.command", "com.austinv11.d4j.bot.util", "com.austinv11.d4j.bot.db", "com.austinv11.d4j.bot.audio")

interface IScriptCompiler {
    
    fun compile(script: String): ICompiledScript
}

interface ICompiledScript {
    
    fun execute(): Any?
    
    fun bind(key: String, value: Any?)
    
    fun setIn(inputStream: InputStream)
    
    fun setErr(outputStream: OutputStream)
    
    fun setOut(outputStream: OutputStream)
}

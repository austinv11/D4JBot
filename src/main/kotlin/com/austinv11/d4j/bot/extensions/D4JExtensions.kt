package com.austinv11.d4j.bot.extensions

import com.austinv11.d4j.bot.CONFIG
import com.austinv11.d4j.bot.command.Command
import reactor.core.Disposable
import reactor.core.publisher.Mono
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.obj.IGuild
import sx.blah.discord.handle.obj.IUser
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import java.io.PrintWriter
import java.io.StringWriter

fun <T> buffer(function: () -> T): T = RequestBuffer.request(RequestBuffer.IRequest<T> { function() }).get()

fun <T> async(function: () -> T): Disposable = Mono.create<T> { 
    val request = RequestBuffer.request(RequestBuffer.IRequest<T> { function() }).get()
    it.success(request)
}.subscribe()

fun IUser.formattedName(guild: IGuild?): String {
    val name = if (guild == null) this.name else this.getDisplayName(guild)
    
    return "$name#${this.discriminator}"
}

fun IUser.embed(guild: IGuild?): EmbedBuilder = EmbedBuilder()
        .setLenient(true)
        .withFooterIcon(this.avatarURL)
        .withFooterText("Requested by ${this.formattedName(guild)}")
        .withTimestamp(System.currentTimeMillis())

val Command.embed : EmbedBuilder
    get() = this.author.embed(this.guild)

fun Throwable.embedFor(cmd: Command): EmbedObject = cmd.embed
        .withTitle("Error caught!")
        .withDesc("```\n${with(StringWriter()) { this@embedFor.printStackTrace(PrintWriter(this)) }}```")
        .build()

fun String.embedFor(cmd: Command): EmbedObject = cmd.embed
        .withDesc(this)
        .build()

var IUser.isIgnored: Boolean
    get() = CONFIG.ignored.contains(this.stringID)
    set(value) {
        if (value && !this.isIgnored) {
            CONFIG.ignored += this.stringID
        } else if (!value && this.isIgnored) {
            CONFIG.ignored = (CONFIG.ignored.asList() - this.stringID).toTypedArray()
        }
        CONFIG.save()
    }

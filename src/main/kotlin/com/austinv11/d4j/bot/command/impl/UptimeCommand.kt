package com.austinv11.d4j.bot.command.impl

import com.austinv11.d4j.bot.command.CommandExecutor
import com.austinv11.d4j.bot.command.Executor
import com.austinv11.d4j.bot.command.context
import com.austinv11.d4j.bot.extensions.embed
import sx.blah.discord.Discord4J
import sx.blah.discord.util.EmbedBuilder
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class UptimeCommand : CommandExecutor() {
    
    override val name: String = "uptime"
    override val aliases: Array<String> = arrayOf()
    
    @Executor("Gets the uptime of this instance of the bot.")
    fun execute(): EmbedBuilder = context.embed.apply {
        var uptime = System.currentTimeMillis() - Discord4J.getLaunchTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val days = TimeUnit.MILLISECONDS.toDays(uptime)
        uptime -= TimeUnit.DAYS.toMillis(days)
        val hours = TimeUnit.MILLISECONDS.toHours(uptime)
        uptime -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(uptime)
        uptime -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(uptime)
        uptime -= TimeUnit.SECONDS.toMillis(seconds)
        withTitle("Uptime")
        withDesc("$days days, $hours hours, $minutes minutes, $seconds.$uptime seconds")
    }
}
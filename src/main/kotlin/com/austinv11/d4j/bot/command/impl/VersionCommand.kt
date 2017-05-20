package com.austinv11.d4j.bot.command.impl

import com.austinv11.d4j.bot.command.CommandExecutor
import com.austinv11.d4j.bot.command.Executor
import com.austinv11.d4j.bot.command.Parameter
import com.austinv11.d4j.bot.command.context
import com.austinv11.d4j.bot.extensions.embed
import sx.blah.discord.util.EmbedBuilder

class VersionCommand : CommandExecutor() {

    override val name: String = "version"
    override val aliases: Array<String> = arrayOf("d4jversion", "gradle", "maven")

    @Executor("Gets the current version of Discord4J available.")
    fun execute(): EmbedBuilder {
        if (context.raw.startsWith("gradle")) {
            return execute(DependencyManager.GRADLE)
        } else if (context.raw.startsWith("maven")) {
            return execute(DependencyManager.MAVEN)
        } else {
            return context.embed.apply {

            }
        }
    }

    @Executor("Builds information to copy and paste into your dependency manager of choice.")
    fun execute(@Parameter("The dependency manager.") manager: DependencyManager): EmbedBuilder {

    }

    fun fetchVersions(): VersionInfo {

    }
}

enum class DependencyManager {
    MAVEN, GRADLE
}

data class VersionInfo(val latestRelease: String,
                       val branches: Array<Pair<String, String>>)

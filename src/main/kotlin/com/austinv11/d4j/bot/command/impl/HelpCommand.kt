package com.austinv11.d4j.bot.command.impl

import com.austinv11.d4j.bot.command.*
import com.austinv11.d4j.bot.extensions.embed
import sx.blah.discord.util.EmbedBuilder
import java.util.*

class HelpCommand() : CommandExecutor() {

    override val name: String = "help"
    override val aliases: Array<String> = arrayOf("?", "h", "man")

    @Executor("Lists available commands.")
    fun execute(): EmbedBuilder = context.embed.apply {
        withTitle("Help Results (${COMMANDS.size} commands)")
        COMMANDS.forEachIndexed { i, cmd ->
            appendDesc("${i+1}. ${cmd.name}\n")
        }
    }

    @Executor("Provides an explanation of a specific command.")
    fun execute(@Parameter("The command number (1-indexed).") command: Int): EmbedBuilder = execute(COMMANDS[command-1])

    @Executor("Provides an explanation of a specific command.")
    fun execute(@Parameter("The command (or alias) name.") command: String): EmbedBuilder = execute(COMMANDS.find { it.checkCommandName(command) }!!)

    fun execute(cmd: CommandExecutor): EmbedBuilder = context.embed.apply {
        withTitle("Help Page for ${cmd.name}")
        withDesc(buildString {
            appendln("__Aliases:__ ${Arrays.toString(cmd.aliases)}")
            appendln("__Is Guild-only Command:__ ${cmd.guildOnly}")
        })
        cmd.wrappers.forEach {
            appendField(it.syntaxString, buildString {
                appendln(it.description)
                appendln("__Is Owner-only Command:__ ${it.requiresOwner}")
                appendln("__Required User Permissions:__ ${Arrays.toString(it.requiredPerms)}")
            }, true)
        }
    }
}
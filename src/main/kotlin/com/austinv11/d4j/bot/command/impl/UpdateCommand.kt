package com.austinv11.d4j.bot.command.impl

import com.austinv11.d4j.bot.JAR_PATH
import com.austinv11.d4j.bot.LOGGER
import com.austinv11.d4j.bot.command.CommandExecutor
import com.austinv11.d4j.bot.command.Executor
import com.austinv11.d4j.bot.command.context
import com.austinv11.d4j.bot.extensions.buffer
import com.austinv11.d4j.bot.extensions.embed
import com.austinv11.d4j.bot.restart
import com.github.kittinunf.fuel.httpDownload
import java.io.File

const val DOWNLOAD_URL = "https://jitpack.io/com/github/austinv11/D4JBot/-SNAPSHOT/D4JBot--SNAPSHOT-all.jar"

class UpdateCommand() : CommandExecutor() {
    
    override val name: String = "update"
    override val aliases: Array<String> = arrayOf("upgrade")
    
    @Executor("Updates the bot to the latest version.", requiresOwner = true)
    fun execute() {
        LOGGER.info("Updating...")
        
        val context = context
        val channel = context.channel
        val message = buffer { 
            val msg = channel.sendMessage(context.embed.withDesc("Updating the bot, please wait...").build())
            return@buffer msg
        }

        channel.typingStatus = true
        
        val temp = File.createTempFile("bot", "jar")
        
        DOWNLOAD_URL.httpDownload().destination { _, _ -> temp }
                .responseString { request, response, result -> 
                    result.fold({ d -> 
                        LOGGER.info("Updated! Restarting...")
                        buffer { message.edit(context.embed.withDesc("Updated!").build()) }
                        temp.copyTo(File(JAR_PATH), true)
                        restart()
                    }, { err ->
                        LOGGER.warn("Unable to update!")
                        err.printStackTrace()
                        throw err
                    })
                    temp.delete()
                    channel.typingStatus = false
                }.timeout(5 * 60 * 1000)
    }
}

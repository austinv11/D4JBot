package com.austinv11.d4j.bot.command.impl

import com.austinv11.d4j.bot.JAR_PATH
import com.austinv11.d4j.bot.LOGGER
import com.austinv11.d4j.bot.command.CommandExecutor
import com.austinv11.d4j.bot.command.Executor
import com.austinv11.d4j.bot.command.context
import com.austinv11.d4j.bot.extensions.buffer
import com.austinv11.d4j.bot.extensions.embed
import com.austinv11.d4j.bot.restart
import reactor.core.publisher.Mono
import java.io.File
import java.util.concurrent.TimeUnit

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

        val currJar = File(JAR_PATH)
        val temp = File.createTempFile("bot", ".jar")
        currJar.renameTo(temp)

        Mono.create<Boolean> {
            try {
                ProcessBuilder("wget", DOWNLOAD_URL).inheritIO().start().waitFor(5, TimeUnit.MINUTES)
                it.success(true)
            } catch (e: Exception) {
                it.error(e)
            }
        }.doOnError({ true }, {
            channel.typingStatus = false
            LOGGER.warn("Unable to update!")
            temp.renameTo(currJar)
            buffer { message.edit(context.embed.withDesc("Update Failed!").build()) }
            it.printStackTrace()
            throw it
        }).subscribe {
            channel.typingStatus = false
            LOGGER.info("Updated! Restarting...")
            buffer { message.edit(context.embed.withDesc("Updated!").build()) }
            temp.delete()
            restart()
        }

//        DOWNLOAD_URL.download(currJar)
//                .doOnError({ true }, {
//                    channel.typingStatus = false
//                    LOGGER.warn("Unable to update!")
//                    temp.renameTo(currJar)
//                    buffer { message.edit(context.embed.withDesc("Update Failed!").build()) }
//                    it.printStackTrace()
//                    throw it
//                }).subscribe {
//                    channel.typingStatus = false
//                    LOGGER.info("Updated! Restarting...")
//                    buffer { message.edit(context.embed.withDesc("Updated!").build()) }
//                    temp.delete()
//                    restart()
//                }

//        DOWNLOAD_URL.httpDownload().destination { _, _ -> currJar }
//                .responseString { request, response, result ->
//                    result.fold({ d ->
//                        LOGGER.info("Updated! Restarting...")
//                        buffer { message.edit(context.embed.withDesc("Updated!").build()) }
//                        temp.delete()
//                        restart()
//                    }, { err ->
//                        LOGGER.warn("Unable to update!")
//                        temp.renameTo(currJar)
//                        err.printStackTrace()
//                        throw err
//                    })
//                    temp.delete()
//                    channel.typingStatus = false
//                }.timeout(5 * 60 * 1000)
//                .timeoutRead(5 * 60 * 1000)
//                .progress { readBytes, totalBytes ->
//                    val percentage = "%.2f".format((readBytes.toDouble()/totalBytes.toDouble()) * 100.toDouble()) + "%"
//                    try { message.edit(context.embed.withDesc("$percentage done").build()) } catch (e: RateLimitException) {}
//                    LOGGER.info("$percentage done")
//                }
    }
}

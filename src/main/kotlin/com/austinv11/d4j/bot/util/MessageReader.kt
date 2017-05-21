package com.austinv11.d4j.bot.util

import com.austinv11.d4j.bot.CLIENT
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.io.IOException
import java.io.Reader
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Predicate

class MessageReader(val channelID: Long): Reader(), IListener<MessageReceivedEvent> {
    val buffer = AtomicReference<StringBuilder>(StringBuilder())

    init {
        CLIENT.dispatcher.registerListener(this)
    }

    override fun handle(event: MessageReceivedEvent) {
        if (event.channel.longID == channelID)
            buffer.get().appendln(event.message.content)
    }

    override fun close() {
        CLIENT.dispatcher.unregisterListener(this)
    }

    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        if (cbuf.size+off < len)
            throw IOException("Buffer length is less than read length!")

        var off = off
        var counter = 0
        val copy = buffer.getAndSet(StringBuilder()).toString()
        for (i in 0..len) {
            if (copy.length < i+1) {
                if (buffer.get().isEmpty()) { //Builder needs more input, we should wait for it
                    CLIENT.dispatcher.waitFor(Predicate<MessageReceivedEvent> { it.message.channel.longID == channelID })
                }

                val read = read(cbuf, off+i, len-(i+1))
                counter += read
                if (counter >= len)
                    break
                else
                    off += read
            } else {
                cbuf[i+off] = copy[i]
            }
        }

        return counter
    }

    override fun ready(): Boolean {
        return true
    }
}

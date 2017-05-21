package com.austinv11.d4j.bot.command.impl

import com.austinv11.d4j.bot.command.CommandExecutor
import com.austinv11.d4j.bot.command.Executor
import com.austinv11.d4j.bot.command.Parameter
import com.austinv11.d4j.bot.command.context
import com.austinv11.d4j.bot.extensions.embed
import com.austinv11.d4j.bot.scripting.JavaRuntimeCompiler
import com.austinv11.d4j.bot.util.MessageReader
import org.apache.commons.io.input.ReaderInputStream
import org.apache.commons.io.output.WriterOutputStream
import sx.blah.discord.util.EmbedBuilder
import java.io.PrintStream
import java.io.PrintWriter
import java.io.Reader
import java.io.StringWriter
import java.nio.charset.Charset

class JavaEvalCommand : CommandExecutor() {
    
    override val name: String = "java"
    override val aliases: Array<String> = arrayOf("compile", "javac")
    
    @Executor("Compiles and runs the provided java snippet.", requiresOwner = true)
    fun execute(@Parameter("The code to attempt to run.") code: String): EmbedBuilder {
        val code = context.raw.substringAfter(' ')
        val context = context
        context.channel.typingStatus = true
        val compiled = JavaRuntimeCompiler.compile(code.removeSurrounding("```").removePrefix("java").removeSurrounding("`"))
        val builder = context.embed.withTitle("Java Evaluation Results").withThumbnail("http://www.vectorsland.com/imgd/l12866-java-eps-logo-99090.png") //Java's icon
        val writer = StringWriter()
        val reader: Reader = MessageReader(context.channel.longID)
        val ris = ReaderInputStream(reader, Charset.defaultCharset())
        val wos = PrintStream(WriterOutputStream(writer, Charset.defaultCharset()))
        
        compiled.setIn(ris)
        compiled.setOut(wos)
        compiled.setErr(wos)
        
        compiled.bind("context", context)
        
        try {
            val result = compiled.execute()
            builder.appendField("Output", "```\n$result```", false)
        } catch (e: Throwable) {
            val stacktraceWriter = StringWriter()
            e.printStackTrace(PrintWriter(stacktraceWriter))
            val stacktraceLines = stacktraceWriter.toString().lines()
            val stacktrace = stacktraceLines.subList(0, Math.min(10, stacktraceLines.size)).joinToString("\n")
            stacktraceWriter.close()
            builder.appendField("Thrown Exception", "```\n$stacktrace```", false)
        } finally {
            wos.close()
            ris.close()
            val log = writer.toString()
            builder.appendField("Log", "```\n$log```", false)
            context.channel.typingStatus = false
        }
        return builder
    }
}

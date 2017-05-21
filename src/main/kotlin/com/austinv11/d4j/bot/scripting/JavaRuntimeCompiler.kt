package com.austinv11.d4j.bot.scripting

import net.openhft.compiler.CompilerUtils
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream

object JavaRuntimeCompiler : IScriptCompiler {

    val defaultImports = JAVA_IMPORTS + DISCORD4J_IMPORTS + REACTOR_IMPORTS + BOT_IMPORTS
    
    @Volatile private var dummyClassCounter = 0  
    
    override fun compile(script: String): ICompiledScript {
        val builtScript = buildString {
            append("package nil;\n")
            defaultImports.forEach { append("import $it.*;\n") }
        }
        
        var script = script
        if (!script.endsWith(";"))
            script += ";"
        if (!script.contains("return "))
            script += "\nreturn null;"
        
        return CompiledJava(builtScript, script, dummyClassCounter++)
    }
    
    class CompiledJava(val boilerplate: String,
                       val script: String,
                       val classCount: Int) : ICompiledScript {

        var input: InputStream = System.`in`
        var output: PrintStream = System.out
        var error: PrintStream = System.err
        val bindings: MutableMap<String, Any?> = mutableMapOf()
        
        override fun execute(): Any? {
            val finalJava = boilerplate + buildString { 
                append("public class Dummy$classCount extends ScriptedRunnable {\n")
                append("    public Dummy$classCount() {\n")
                append("        super();\n")
                append("    }\n")
                append("    public Object run() {\n")
                bindings.forEach { k, v -> 
                    append("        ${v?.javaClass?.simpleName ?: "Object"} $k = (${v?.javaClass?.simpleName ?: "Object"}) bindingsMap.get(\"$k\");\n")
                }
                append("        $script\n")
                append("    }\n")
                append("}\n")
            }.replace("System.in.", "this.input.")
                    .replace("System.out.", "this.output.")
                    .replace("System.err.", "this.error.")
            val compiled = CompilerUtils.CACHED_COMPILER.loadFromJava("nil.Dummy$classCount", finalJava)
            
            val instance = compiled.newInstance() as ScriptedRunnable
            instance.setIn(input)
            instance.setOut(output)
            instance.setErr(error)
            instance.setBindings(bindings)
            
            return instance.run()
        }

        override fun bind(key: String, value: Any?) { bindings[key] = value }

        override fun setIn(inputStream: InputStream) { input = inputStream }

        override fun setErr(outputStream: OutputStream) { error = outputStream as? PrintStream ?: PrintStream(outputStream) }

        override fun setOut(outputStream: OutputStream) { output = outputStream as? PrintStream ?: PrintStream(outputStream) }
    }
}

abstract class ScriptedRunnable {

    @JvmField public var input: InputStream = System.`in`
    @JvmField public var output: PrintStream = System.out
    @JvmField public var error: PrintStream = System.err
    @JvmField public var bindingsMap: Map<String, Any?> = mapOf()

    abstract fun run(): Any?
    
    fun setIn(inputStream: InputStream) {
        input = inputStream
    }
    
    fun setOut(printStream: PrintStream) {
        output = printStream
    }
    
    fun setErr(printStream: PrintStream) {
        error = printStream
    }
    
    fun setBindings(map: Map<String, Any?>) {
        bindingsMap = map
    }
    
} 

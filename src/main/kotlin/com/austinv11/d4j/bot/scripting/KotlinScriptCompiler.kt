package com.austinv11.d4j.bot.scripting

import com.austinv11.d4j.bot.JAR_PATH
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmDaemonCompileScriptEngine
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmDaemonLocalEvalScriptEngineFactory
import java.io.*
import javax.script.ScriptException

object KotlinScriptCompiler : IScriptCompiler {
    
    init {
        System.setProperty("kotlin.compiler.jar", JAR_PATH)
    }

    val defaultImports = JAVA_IMPORTS + KOTLIN_IMPORTS + DISCORD4J_IMPORTS + REACTOR_IMPORTS + FUEL_IMPORTS + BOT_IMPORTS

    val scriptFactory = KotlinJsr223JvmDaemonLocalEvalScriptEngineFactory() //Skipping the indirect invocation from java because the shadow jar breaks the javax service
    val engine: KotlinJsr223JvmDaemonCompileScriptEngine
        get() = scriptFactory.scriptEngine as KotlinJsr223JvmDaemonCompileScriptEngine
    
    override fun compile(script: String): ICompiledScript {
        val builtScript = buildString { 
            defaultImports.forEach { append("import $it.*;") }
            append("\n")
            append("System.setIn(bindings.get(\"_IN\") as InputStream);")
            append("System.setOut(bindings.get(\"_OUT\") as PrintStream);")
            append("System.setErr(bindings.get(\"_ERR\") as PrintStream);\n")
        }
        
        return KotlinCompiledScript(builtScript, script, engine)
    }
    
    class KotlinCompiledScript(val boilerplate: String, 
                               val script: String,
                               val engine: KotlinJsr223JvmDaemonCompileScriptEngine) : ICompiledScript {
        
        var input: InputStream = System.`in`
        var output: PrintStream = System.out
        var error: PrintStream = System.err
        val bindings: MutableMap<String, Any?> = mutableMapOf()
        
        override fun execute(): Any? {
            var finalScript = boilerplate
            
            engine.context.errorWriter = PrintWriter(error)
            engine.context.writer = PrintWriter(output)
            engine.context.reader = InputStreamReader(input)

            engine.put("_IN", input)
            engine.put("_OUT", output)
            engine.put("_ERR", error)
            
            bindings.forEach { k, v -> 
                finalScript += "val $k: ${v?.javaClass?.simpleName ?: "Any?"} = bindings.get(\"_$k\") as ${v?.javaClass?.simpleName ?: "Any?"};\n"
                engine.put("_$k", v)
            }
            
            finalScript += script
            
            finalScript += "\n"
            
            try {
                return engine.eval(finalScript.trim())
            } catch (e: ScriptException) {
                throw e.cause ?: e
            }
        }

        override fun bind(key: String, value: Any?) { bindings[key] = value }

        override fun setIn(inputStream: InputStream) { input = inputStream }

        override fun setErr(outputStream: OutputStream) { error = outputStream as? PrintStream ?: PrintStream(outputStream) }

        override fun setOut(outputStream: OutputStream) { output = outputStream as? PrintStream ?: PrintStream(outputStream) }
    }
}

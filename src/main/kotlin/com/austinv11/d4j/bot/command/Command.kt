package com.austinv11.d4j.bot.command

import com.austinv11.d4j.bot.CONFIG
import com.austinv11.d4j.bot.OWNER
import com.austinv11.d4j.bot.Result
import com.austinv11.d4j.bot.command.impl.*
import com.austinv11.d4j.bot.extensions.async
import com.austinv11.d4j.bot.extensions.coerceTo
import com.austinv11.d4j.bot.extensions.embedFor
import com.austinv11.d4j.bot.extensions.isIgnored
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import sx.blah.discord.api.internal.DiscordUtils
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.obj.*
import sx.blah.discord.util.EmbedBuilder
import java.lang.invoke.MethodHandles
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure
import kotlin.streams.toList

var COMMANDS: Array<CommandExecutor> = arrayOf(PingCommand(), UpdateCommand(), ShutdownCommand(), HelpCommand(),
        RestartCommand())

fun IMessage.isCommand(): Boolean {
    return content.startsWith(CONFIG.prefix) && COMMANDS.filter { it.checkCommandName(this.content.rawArgs()[0]) }.isNotEmpty()
}

fun IMessage.createCommand(): Command = COMMANDS
        .filter { it.checkCommandName(this.content.rawArgs()[0]) }
        .map { Command(it, this) }
        .first()

fun String.rawArgs(): Array<String> {
    var args = arrayOf<String>()
    var lastString: String? = null
    val tokenizer = StringTokenizer(this, " ")
    while (tokenizer.hasMoreTokens()) {
        val next = tokenizer.nextToken()
        if (lastString == null) {
            if (next.startsWith("\"")) {
                lastString = next.removePrefix("\"") + " "
            } else if (next.endsWith("\"")) {
                lastString = ""
                args += next.removeSuffix("\"")
            } else {
                args += next
            }
        } else {
            if (next.startsWith("\"")) {
                args += lastString
                lastString = null
            } else if (next.endsWith("\"")) {
                lastString += next.removeSuffix("\"")
                args += lastString
                lastString = null
            } else {
                lastString += next + " "
            }
        }
    }
    
    if (lastString != null) args += lastString
    
    return args
}

class Command(val executor: CommandExecutor, 
              val message: IMessage, 
              val guild: IGuild? = if (message.channel.isPrivate) null else message.guild,
              val channel: IChannel = message.channel,
              val author: IUser = message.author,
              val raw: String = message.content.removePrefix(CONFIG.prefix),
              val rawArgs: Array<String> = Arrays.copyOfRange(raw.rawArgs(), 1, raw.rawArgs().size)) {

    fun execute() {
        try {
            val result = executor.submit(this)
            result.doOnError { async { it ?: channel.sendMessage((it as? CommandException)?.message?.embedFor(this) ?: it.embedFor(this)) } }
            result.filter { it != Result.NONE }
                    .subscribe { async { channel.sendMessage(if (it == Result.SUCCESS) CONFIG.success_message else CONFIG.error_message) } }
        } catch (e: Throwable) {e.printStackTrace() }
    }
}

abstract class CommandExecutor {

    abstract val name: String
    abstract val aliases: Array<String>
    open val permissions: Array<Permissions> = arrayOf(Permissions.READ_MESSAGES, Permissions.SEND_MESSAGES)
    open val guildOnly: Boolean = false
    
    val wrappers: Array<InvocationWrapper> by lazy { 
        var wrapped = emptyArray<InvocationWrapper>()
        this::class.functions
                .filter { it.findAnnotation<Executor>() != null }
                .forEach { wrapped += InvocationWrapper(this, it) }
        
        return@lazy wrapped
    }
    
    fun checkCommandName(name: String): Boolean {
        val name = name.removePrefix(CONFIG.prefix)
        return this.name.equals(name, true) 
                || Flux.fromArray(aliases)
                    .filter { it.equals(name, true) }
                    .count()
                    .block() > 0
    }
    
    fun submit(cmd: Command): Mono<Result> = wrappers.toFlux().sort { o1, o2 -> 
                if (o1.params.size < o2.params.size) -1 else if (o1.params.size > o2.params.size) 1 else 0
            }.filter { it.paramDescriptions.stream().map { it.third }.filter { it }.count() <= cmd.rawArgs.size }
            .filter { it.shouldBeInvoked(cmd) }
            .next()
            .map {
                val (result, message) = (it.invoke(cmd) ?: return@map Result.NONE)
                if (result != null)
                    return@map if (result) Result.SUCCESS else Result.FAILURE
                else {
                    async { cmd.channel.sendMessage(message!!) }
                    return@map Result.NONE
                }
            }
    
    inner class InvocationWrapper(val executor: CommandExecutor,
                            val method: KFunction<*>) {
        val params: List<KParameter> = method.valueParameters
        val syntaxString: String by lazy {
            CONFIG.prefix + executor.name + " " + StringJoiner(" ").apply {
                params.forEach {
                    val joiner = if (it.isOptional) StringJoiner(": ", "[", "]") else StringJoiner(": ")
                    with(joiner) {
                        this@with.add(it.name)
                        this@with.add(it.type.jvmErasure.simpleName)
                        if (it.type.isMarkedNullable) this@with.add("?")
                    }
                    this@apply.add(joiner.toString())
                } 
            }.toString()
        }
        val paramDescriptions: List<Triple<String, String?, Boolean>> = params
                .stream()
                .filter { !Command::class.java.isAssignableFrom(it.type.jvmErasure.java) }
                .map { 
                    val name = it.name!!
                    val description = it.findAnnotation<Parameter>()?.description
                    val optional = it.isOptional
                    return@map Triple(name, description, optional)
                }.toList()
        val description: String by lazy { method.findAnnotation<Executor>()!!.description }
        val requiredPerms: Array<Permissions> by lazy { 
            val annotation = method.findAnnotation<Executor>()!! 
            return@lazy if (annotation.inheritPermissions) executor.permissions else annotation.permissions
        }
        val requiresOwner: Boolean by lazy { method.findAnnotation<Executor>()!!.requiresOwner }
        val handle = MethodHandles.lookup().unreflect(method.javaMethod).bindTo(executor)!!

        fun Array<String>.mapArgs(cmd: Command): Array<Any?>? {
            val args = arrayOfNulls<Any?>(this.size)
            try {
                this.forEachIndexed { i, arg ->
                    val paramType = params[i].type
                    if (arg == "null") {
                        if (paramType.isMarkedNullable) args[i] = null
                        else if (String::class.java == paramType.jvmErasure.java) args[i] = "null"
                        else throw CommandException("Attempting to pass null to a non-nullable argument!")
                    } else if (paramType.jvmErasure.isSubclassOf(Number::class)) {
                        args[i] = arg.coerceTo(paramType.jvmErasure, cmd) ?: throw CommandException("Expected a number at argument $i!")
                    } else if (paramType.jvmErasure.isSubclassOf(Boolean::class)) {
                        args[i] = arg.coerceTo(paramType.jvmErasure, cmd) ?: throw CommandException("Expected a boolean at argument $i!")
                    } else if (paramType.jvmErasure.isSubclassOf(Char::class)) {
                        args[i] = arg.coerceTo(paramType.jvmErasure, cmd) ?: throw CommandException("Expected a character at argument $i!")
                    } else if (paramType.jvmErasure.isSubclassOf(String::class)) {
                        args[i] = arg
                    } else if (paramType.jvmErasure.java.isArray) {
                        args[i] = arg.split(",( |)".toRegex()).toTypedArray().mapArgs(cmd)
                    } else if (paramType.jvmErasure.isSubclassOf(IMessage::class)) {
                        args[i] = arg.coerceTo(paramType.jvmErasure, cmd) ?: throw CommandException("Expected a message at argument $i!")
                    } else if (paramType.jvmErasure.isSubclassOf(IVoiceChannel::class)) {
                        args[i] = arg.coerceTo(paramType.jvmErasure, cmd) ?: throw CommandException("Expected a voice channel at argument $i!")
                    } else if (paramType.jvmErasure.isSubclassOf(IChannel::class)) {
                        args[i] = arg.coerceTo(paramType.jvmErasure, cmd) ?: throw CommandException("Expected a channel at argument $i!")
                    } else if (paramType.jvmErasure.isSubclassOf(IGuild::class)) {
                        args[i] = arg.coerceTo(paramType.jvmErasure, cmd) ?: throw CommandException("Expected a guild at argument $i!")
                    } else if (paramType.jvmErasure.isSubclassOf(IUser::class)) {
                        args[i] = arg.coerceTo(paramType.jvmErasure, cmd) ?: throw CommandException("Expected a user at argument $i!")
                    } else if (paramType.jvmErasure.isSubclassOf(IRole::class)) {
                        args[i] = arg.coerceTo(paramType.jvmErasure, cmd) ?: throw CommandException("Expected a role at argument $i!")
                    } else if (paramType.jvmErasure.isSubclassOf(IEmoji::class)) {
                        args[i] = arg.coerceTo(paramType.jvmErasure, cmd) ?: throw CommandException("Expected an emoji at argument $i!")
                    } else if (paramType.jvmErasure.isSubclassOf(StatusType::class)) {
                        args[i] = arg.coerceTo(paramType.jvmErasure, cmd) ?: throw CommandException("Expected a status type at argument $i!")
                    } else if (paramType.jvmErasure.isSubclassOf(Permissions::class)) {
                        args[i] = arg.coerceTo(paramType.jvmErasure, cmd) ?: throw CommandException("Expected a permission at argument $i!")
                    } else if (paramType.jvmErasure.isSubclassOf(VerificationLevel::class)) {
                        args[i] = arg.coerceTo(paramType.jvmErasure, cmd) ?: throw CommandException("Expected a verification level at argument $i!")
                    } else if (paramType.jvmErasure.isSubclassOf(Command::class)) {
                        args[i] = cmd
                    } else {
                        throw CommandException("Unable to map argument $i!")
                    }
                }
            } catch (e: Exception) {
                return null
            }
            return args
        }
        
        fun shouldBeInvoked(cmd: Command): Boolean {
            return cmd.rawArgs.mapArgs(cmd) != null
        }
        
        fun invoke(cmd: Command): Pair<Boolean?, EmbedObject?>? {
            if (cmd.channel.isPrivate && guildOnly) throw CommandException("This command cannot be executed through DMs!")
            
            if (cmd.author.isBot) throw CommandException("Sorry, bots can't do that!")
            
            val isOwner = cmd.author.longID == OWNER.longID
            
            if (!isOwner) {

                if (requiresOwner) throw CommandException("Only my owner can do that!")

                if (cmd.author.isIgnored) throw CommandException("Sorry, your command permissions have been revoked.")
                    
                DiscordUtils.checkPermissions(cmd.author, cmd.channel, EnumSet.of(requiredPerms[0], *requiredPerms.copyOfRange(1, requiredPerms.size)))
            }
            
            val args = cmd.rawArgs.mapArgs(cmd)!!
            
            associateContext(this@CommandExecutor, cmd)
            val returned = handle.invokeWithArguments(*args) ?: return null
            disassociateContext(this@CommandExecutor)
            
            if (returned is Boolean) return returned to null
            
            if (returned is EmbedObject) return null to returned
            
            if (returned is EmbedBuilder) return null to returned.build()
            
            if (returned is String) return null to returned.embedFor(cmd)
            
            if (returned is Long) return null to java.lang.Long.toUnsignedString(returned).embedFor(cmd)
            
            if (returned is Number) return null to returned.toString().embedFor(cmd)
            
            if (returned is IGuild) return null to (returned.name + " " + returned.stringID).embedFor(cmd)
            
            if (returned is IChannel) return null to returned.mention().embedFor(cmd)
            
            if (returned is IUser) return null to returned.mention().embedFor(cmd)
            
            if (returned is IMessage) return null to ("From: " + returned.author.mention() + ", " + returned.content).embedFor(cmd)
            
            if (returned is IEmoji) return null to returned.toString().embedFor(cmd)
            
            if (returned is IRole) return null to returned.mention().embedFor(cmd)
            
            if (returned is Throwable) throw returned
            
            return null to returned.toString().embedFor(cmd)
        }
    }
}

enum class BotRole {
    OWNER, USER, NONE
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Executor(val description: String,
                          val inheritPermissions: Boolean = true,
                          val permissions: Array<Permissions> = emptyArray(),
                          val requiresOwner: Boolean = false)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Parameter(val description: String)

class CommandException(override val message: String): Exception(message)

private val contexts: ConcurrentHashMap<Long, Command> = ConcurrentHashMap() 

fun associateContext(executor: CommandExecutor, cmd: Command) {
    contexts[Thread.currentThread().id + executor.name.hashCode().toLong()] = cmd
}

fun disassociateContext(executor: CommandExecutor) {
    contexts.remove(Thread.currentThread().id + executor.name.hashCode().toLong())
}

val CommandExecutor.context: Command
    get() = contexts[Thread.currentThread().id + this.name.hashCode().toLong()]!!

package com.austinv11.d4j.bot.command.impl

import com.austinv11.d4j.bot.command.CommandExecutor
import com.austinv11.d4j.bot.command.Executor
import com.austinv11.d4j.bot.command.Parameter
import com.austinv11.d4j.bot.command.context
import com.austinv11.d4j.bot.extensions.embed
import com.austinv11.d4j.bot.extensions.obj
import com.github.kittinunf.fuel.httpGet
import sx.blah.discord.util.EmbedBuilder

const val BASE_PATTERN = "https://jitpack.io/com/github/austinv11/Discord4J/%s/Discord4J-%s"
const val JAR = BASE_PATTERN + ".jar"
const val SHADED = BASE_PATTERN + "-shaded.jar"
const val JAVADOC_JAR = BASE_PATTERN + "-javadoc.jar"
const val SOURCES = BASE_PATTERN + "-sources.jar"
const val JAVADOC = "https://jitpack.io/com/github/austinv11/Discord4J/%s/javadoc/"

const val GITHUB_BASE = "https://api.github.com/repos/austinv11/Discord4J/"
const val RELEASES = GITHUB_BASE + "releases/latest"
const val BRANCHES = GITHUB_BASE + "branches"
const val COMMIT_LINK = "https://github.com/austinv11/Discord4J/commit/"
const val BRANCH_LINK = "https://github.com/austinv11/Discord4J/tree/"

class VersionCommand : CommandExecutor() {
    
    override val name: String = "version"
    override val aliases: Array<String> = arrayOf("d4jversion", "gradle", "maven", "sbt")

    @Executor("Gets the current version of Discord4J available.")
    fun execute(): EmbedBuilder {
        if (context.raw.startsWith("gradle")) {
            return execute(DependencyManager.GRADLE)
        } else if (context.raw.startsWith("maven")) {
            return execute(DependencyManager.MAVEN)
        } else if (context.raw.startsWith("sbt")) {
            return execute(DependencyManager.SBT)
        } else {
            return context.embed.apply {
                val versions = fetchVersions()
                withTitle("Discord4J Versions")
                withDesc("**Latest Release:** [${versions.latestRelease.tag_name}](${versions.latestRelease.html_url})\n")
                appendField(versions.latestRelease.tag_name + " (Recommended)", buildDownloadString(versions.latestRelease.tag_name), false)
                appendField("Latest Development Build (`${versions.branches.find { it.name == "dev" }!!.commit.short}`)", buildDownloadString("dev-SNAPSHOT"), false)
//                versions.branches.forEach { 
//                    appendField("\u200b", "**Commit [${it.commit.short}](${it.commit.link}) on [${it.name}](${it.link})**\n" + buildDownloadString(it.commit.short), false)
//                }
            }
        }
    }

    @Executor("Builds information to copy and paste into your dependency manager of choice.")
    fun execute(@Parameter("The dependency manager.") manager: DependencyManager): EmbedBuilder = execute(manager, fetchVersions().latestRelease.tag_name)

    @Executor("Builds information to copy and paste into your dependency manager of choice.")
    fun execute(@Parameter("The version to generate snippets for.") version: String): EmbedBuilder = execute(DependencyManager.valueOf(context.raw.split(" ")[0].toUpperCase()), version)
    
    @Executor("Builds information to copy and paste into your dependency manager of choice.")
    fun execute(@Parameter("The dependency manager.") manager: DependencyManager,
                @Parameter("The version to generate snippets for.") version: String): EmbedBuilder = context.embed.apply {
        withTitle("Snippet for `$version` on ${manager.name.toLowerCase().capitalize()}")
        when (manager) {
            DependencyManager.MAVEN -> {
                withDesc(buildString { 
                    appendln("```xml")
                    appendln("<properties>")
                    append("\t")
                    appendln("<discord4j.version>$version</discord4j.version>")
                    appendln("</properties>")
                    appendln()
                })
                appendDesc(buildString {
                    appendln("<repositories>")
                    append("\t")
                    appendln("<repository>")
                    append("\t\t")
                    appendln("<id>jcenter</id>")
                    append("\t\t")
                    appendln("<url>http://jcenter.bintray.com</url>")
                    append("\t")
                    appendln("</repository>")
                    append("\t")
                    appendln("<repository>")
                    append("\t\t")
                    appendln("<id>jitpack.io</id>")
                    append("\t\t")
                    appendln("<url>https://jitpack.io</url>")
                    append("\t")
                    appendln("</repository>")
                    appendln("</repositories>")
                    appendln()
                })
                appendDesc(buildString { 
                    appendln("<dependencies>")
                    append("\t")
                    appendln("<dependency>")
                    append("\t\t")
                    appendln("<groupId>com.github.austinv11</groupId>")
                    append("\t\t")
                    appendln("<artifactId>Discord4J</artifactId>")
                    append("\t\t")
                    appendln("<version>\${discord4j.version}</version>")
                    append("\t")
                    appendln("</dependency>")
                    appendln("</dependencies>")
                    append("```")
                })
            }
            DependencyManager.GRADLE -> {
                withDesc(buildString { 
                    appendln("```groovy")
                    appendln("buildscript {")
                    append("\t")
                    appendln("ext.discord4j_version = '$version'")
                    appendln("}")
                    appendln()
                })
                appendDesc(buildString {
                    appendln("repositories {")
                    append("\t")
                    appendln("jcenter()")
                    append("\t")
                    appendln("maven {")
                    append("\t\t")
                    appendln("url 'https://jitpack.io'")
                    append("\t")
                    appendln("}")
                    appendln("}")
                    appendln()
                })
                appendDesc(buildString {
                    appendln("dependencies {")
                    append("\t")
                    appendln("compile 'com.github.austinv11:Discord4J:\$discord4j_version'")
                    appendln("}")
                    append("```")
                })
            }
            DependencyManager.SBT -> {
                withDesc(buildString { 
                    appendln("```scala")
                    appendln("val discord4jVersion = \"$version\"")
                    appendln()
                })
                appendDesc(buildString {
                    appendln("libraryDependencies ++= Seq(")
                    append("\t")
                    appendln("\"com.github.austinv11\" % \"Discord4J\" % discord4jVersion")
                    appendln(")")
                    appendln()
                })
                appendDesc(buildString { 
                    appendln("resolvers += \"jcenter\" at \"http://jcenter.bintray.com\"")
                    appendln("resolvers += \"jitpack.io\" at \"https://jitpack.io\"")
                    append("```")
                })
            }
        }
    }
    
    fun buildDownloadString(version: String): String = "[jar](${JAR.format(version, version)}) (**[shaded](${SHADED.format(version, version)})**) [javadocs](${JAVADOC.format(version)}) ([download](${JAVADOC_JAR.format(version, version)})) [source](${SOURCES.format(version, version)})"
    
    fun fetchVersions(): VersionInfo {
        val (_, _, result) = RELEASES.httpGet().responseString()
        var release: Release? = null
        result.fold({ string ->
            release = string.obj()
        }, { ex -> 
            throw ex
        })
        
        val (_, _, result2) = BRANCHES.httpGet().responseString()
        var branches: Array<Branch>? = null
        result2.fold({ string -> 
            branches = string.obj()
        }, { ex ->
            throw ex
        })
        
        return VersionInfo(release!!, branches!!)
    }
}

enum class DependencyManager {
    MAVEN, GRADLE, SBT
}

data class VersionInfo(val latestRelease: Release,
                       val branches: Array<Branch>)

data class Commit(val sha: String) {
    val short: String
        get() = sha.substring(0, 7)
    val link: String
        get() = COMMIT_LINK + sha
}

data class Branch(val name: String, 
                  val commit: Commit) {
    val link: String
        get() = BRANCH_LINK + name
}

data class Release(val html_url: String,
                   val tag_name: String,
                   val body: String)

package com.austinv11.d4j.bot.command.impl

import com.austinv11.d4j.bot.CLIENT
import com.austinv11.d4j.bot.command.*
import com.austinv11.d4j.bot.db.Tag
import com.austinv11.d4j.bot.db.TagTable
import com.austinv11.d4j.bot.extensions.embed
import com.austinv11.d4j.bot.extensions.formattedName
import com.austinv11.d4j.bot.extensions.isBotOwner

class TagCommand : CommandExecutor() {
    
    override val name: String = "tag"
    override val aliases: Array<String> = arrayOf("tags")
    
    @Executor("Gets a list of tags.")
    fun execute() = context.embed.apply {
        withTitle("Available tags:")
        withDesc(buildString { 
            TagTable.tags.forEachIndexed { i, tag -> 
                appendln("${i+1}. ${tag.name}")
            }
        })
    }
    
    @Executor("Gets a specific tag.")
    fun execute(@Parameter("The tag to get.") tag: String) = context.embed.apply {
        val tag = TagTable.getTag(tag)
        if (tag == null) {
            throw CommandException("Tag not found!")
        } else {
            withTitle(tag.name)
            withDesc(tag.content)
            val user = CLIENT.getUserByID(tag.author)
            if (user != null) {
                withAuthorIcon(user.avatarURL)
                withAuthorName(user.formattedName(context.guild))
                withTimestamp(tag.timestamp)
            }
        }
    }
    
    @Executor("Performs a an action on the tag list.")
    fun execute(@Parameter("The action to perform.") action: OneArgActions, 
                @Parameter("The tag to perform the action on.") tag: String): Any {
        when (action) {
            OneArgActions.REMOVE -> {
                val currTag = TagTable.getTag(tag)
                if (currTag != null) {
                    if (!!context.author.isBotOwner && context.author.longID != currTag.author)
                        throw CommandException("You can't modify another user's tag!")
                }
                
                TagTable.removeTag(tag)
                return true
            }
            OneArgActions.GET -> {
                return@execute execute(tag)
            }
        }
    }

    @Executor("Sets a tag's content.")
    fun execute(@Parameter("The action to perform.") action: TwoArgActions,
                @Parameter("The tag to perform the action on.") tag: String, 
                @Parameter("The content to associate with the tag.") content: String): Boolean {
        when (action) {
            TwoArgActions.PUT -> {
                val currTag = TagTable.getTag(tag)
                if (currTag != null) {
                    if (!!context.author.isBotOwner && context.author.longID != currTag.author)
                        throw CommandException("You can't modify another user's tag!")
                }
                TagTable.addTag(Tag(tag, context.author.longID, content, System.currentTimeMillis()))
            }
        }
        return true
    }
    
    enum class OneArgActions {
        REMOVE, GET
    }
    
    enum class TwoArgActions {
        PUT
    }
}

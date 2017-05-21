package com.austinv11.d4j.bot.db

import com.austinv11.d4j.bot.extensions.quote
import com.austinv11.d4j.bot.extensions.unsignedString

object TagTable {
    
    const val NAME = "tags"
    
    init {
        Database.open {
            defineTable(NAME, "id" to "TEXT PRIMARY KEY NOT NULL", "author" to "INTEGER NOT NULL" , "content" to "TEXT NOT NULL", "time" to "INTEGER NOT NULL")
        }
    }
    
    val tags: List<Tag>
        get() {
            val tags = mutableListOf<Tag>()
            Database.open { 
                if (getRowCount(NAME) > 0) {
                    getRows(NAME).closing {
                        while (next()) {
                            tags += Tag(getString("id"), getLong("author"), getString("content"), getLong("time"))
                        }
                    }
                }
            }
            return tags
        }
    
    fun addTag(tag: Tag) {
        Database.open {
            if (!rowExists(NAME, "id", tag.name.quote)) {
                insert(NAME, "id" to tag.name.quote, "author" to tag.author.unsignedString, "content" to tag.content.quote, "time" to tag.timestamp.toString())
            } else {
                updateColumn(NAME, arrayOf("author" to tag.author.unsignedString, "content" to tag.content.quote, "time" to tag.timestamp.toString()), "id" to tag.name.quote)
            }
        }
    }
    
    fun getTag(name: String): Tag? {
        Database.open {
            if (!rowExists(NAME, "id", name.quote)) return@getTag null

            val tag = getContrainedRows(NAME, "id" to name.quote).closing {
                Tag(getString("id"), getLong("author"), getString("content"), getLong("time"))
            }
            return tag
        }
        
        return null
    }
    
    fun removeTag(name: String) {
        Database.open {
            if (rowExists(NAME, "id", name.quote)) {
                deleteRow(NAME, "id" to name.quote)
            }
        }
    }
}

data class Tag(val name: String, val author: Long, val content: String, val timestamp: Long)

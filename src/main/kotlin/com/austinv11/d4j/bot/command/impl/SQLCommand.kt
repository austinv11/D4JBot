package com.austinv11.d4j.bot.command.impl

import com.austinv11.d4j.bot.CONFIG
import com.austinv11.d4j.bot.command.CommandExecutor
import com.austinv11.d4j.bot.command.Executor
import com.austinv11.d4j.bot.command.Parameter
import com.austinv11.d4j.bot.command.context
import com.austinv11.d4j.bot.db.Database
import com.austinv11.d4j.bot.db.closeAfter
import com.austinv11.d4j.bot.extensions.embed

class SQLCommand : CommandExecutor() {
    
    override val name: String = "sql"
    override val aliases: Array<String> = arrayOf("db", "database")
    
    @Executor("Evaluates an SQL expression.", requiresOwner = true)
    fun execute(@Parameter("The SQL expression.") sql: String) = context.embed.apply {
        withThumbnail("https://www.sqlite.org/images/sqlite370_banner.gif") //SQLite icon
        Database.open { 
            val realSQL = context.raw.removePrefix(context.raw.split(" ")[0] + " ").removeSurrounding("```").removePrefix("sql").removeSurrounding("`")
            queryRaw(realSQL)?.closeAfter { 
                val columns = this.metaData.columnCount
                val columnNames = arrayOfNulls<String>(columns)
                for (i in 1..columns) {
                    columnNames[i-1] = this.metaData.getColumnName(i)
                }
                withTitle("Query Results (may be truncated)")
                var counter = 1
                while (this.next()) {
                    val dataList = mutableListOf<Pair<String, String>>()
                    for (i in 1..columns) {
                        dataList.add(columnNames[i-1]!! to (this.getString(i) ?: "NULL"))
                    }
                    val lengthMap = dataList.map { Math.max(it.first.length, it.second.length) }
                    appendField("Row ${counter++}", buildString { 
                        append("`")
                        appendln(dataList.mapIndexed { index: Int, pair: Pair<String, String> -> 
                            pair.first padTo lengthMap[index]
                        }.joinToString("|", "|", "|"))
                        appendln("-".repeat(1 + lengthMap.sumBy { it + 1 }))
                        appendln(dataList.mapIndexed { index: Int, pair: Pair<String, String> ->
                            pair.second padTo lengthMap[index]
                        }.joinToString("|", "|", "|"))
                        append("`")
                    }, true)
                }
            } ?: withDesc(CONFIG.success_message)
        }
    }
    
    infix fun String.padTo(length: Int): String {
        val toAdd = length - this.length
        
        if (toAdd <= 0) return this
        
        return " ".repeat(toAdd / 2) + this + " ".repeat(Math.ceil(toAdd.toDouble() / 2.0).toInt())
    }
}

package com.austinv11.d4j.bot.db

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

object Database {
    
    const val DEFAULT_FILE = "bot.db"
    
    fun open(database: String = DEFAULT_FILE) : DatabaseHandle = DatabaseHandle(database)
    
    inline fun open(database: String = DEFAULT_FILE, wrapper: DatabaseHandle.() -> Unit) {
        val handle = open(database)
        wrapper(handle)
        if (handle.isOpen)
            handle.close()
    }
}

class DatabaseHandle(database: String) : AutoCloseable {
    
    private val statements = mutableListOf<Statement>() 
    var connection: Connection
    val statement: Statement
        get() = generateStatement().also { statements.add(it) }
    
    init {
        connection = DriverManager.getConnection("jdbc:sqlite:$database")
    }
    
    var isOpen: Boolean = true
    
    inline fun insert(wrapper: DatabaseHandle.() -> Unit) { wrapper(this) }
    
    fun executeRaw(line: String) { 
        statement.executeUpdate(line)
    }

    fun queryRaw(line: String) = try { statement.executeQuery(line) } catch (t: Throwable) { null }
    
    fun defineTable(name: String, vararg types: Pair<String, String>) {
        executeRaw(buildString { 
            append("CREATE TABLE IF NOT EXISTS $name (${types.toSql()});")
        })
    }
    
    fun dropTable(name: String) {
        executeRaw("DROP TABLE IF EXISTS $name;")
    }
    
    fun tableExists(name: String): Boolean = connection.metaData.getTables(null, null, name, null).fetchSize > 0
    
    fun clearTable(name: String) {
        executeRaw(buildString { 
            append("TRUNCATE TABLE IF EXISTS $name;")
        })
    }
    
    fun renameTable(oldName: String, newName: String) {
        executeRaw(buildString { 
            append("ALTER TABLE $oldName RENAME TO $newName;")
        })
    }
    
    fun addColumn(table: String, column: Pair<String, String>) {
        executeRaw(buildString { 
            append("ALTER TABLE $table ADD ${column.first} ${column.second};")
        })
    }

    fun removeColumn(table: String, column: String) {
        executeRaw(buildString {
            append("ALTER TABLE $table DROP COLUMN $column;")
        })
    }

    fun renameColumn(table: String, oldColumn: String, newColumn: String) {
        executeRaw(buildString {
            append("ALTER TABLE $table RENAME $oldColumn TO $newColumn;")
        })
    }
    
    fun columnExists(table: String, column: String): Boolean = getRowCount(table, column to "*") > 0
    
    fun rowExists(table: String, column: String, value: String): Boolean = getRowCount(table, column to value) > 0
    
    fun insert(table: String, vararg columnValuePairs: Pair<String, String>) {
        executeRaw(buildString { 
            append("INSERT INTO $table (${columnValuePairs.joinToString(", ") { it.first }}) VALUES (${columnValuePairs.joinToString(", ") { it.second }});")
        })
    }
    
    fun updateColumns(table: String, vararg columnValuePairs: Pair<String, String>) {
        executeRaw(buildString { 
            append("UPDATE $table SET ${columnValuePairs.joinToString(", ") { it.first + " = " + it.second }};")
        })
    }
    
    fun updateColumn(table: String, columnValuePairs: Array<Pair<String, String>>, vararg selectorValuePairs: Pair<String, String>) {
        executeRaw(buildString {
            append("UPDATE $table SET ${columnValuePairs.joinToString(", ") { it.first + " = " + it.second }} WHERE ${selectorValuePairs.joinToString(", ") { it.first + " = " + it.second }};")
        }.also { println(it) })
    }
    
    fun deleteRow(table: String, vararg selectorValuePairs: Pair<String, String>) {
        executeRaw(buildString { 
            append("DELETE FROM $table WHERE ${selectorValuePairs.joinToString(", ") { it.first + " = " + it.second }};")
        })
    }
    
    fun getRows(table: String) = queryRaw(buildString { 
        append("SELECT * FROM $table;")
    })!!
    
    fun getRowCount(table: String) = queryRaw(buildString { 
        append("SELECT COUNT(*) AS _count FROM $table;")
    })!!.closeAfter { this.getInt("_count") }
    
    fun getRowCount(table: String, vararg selectorValuePairs: Pair<String, String>) = queryRaw(buildString {
        append("SELECT COUNT(*) AS _count FROM $table WHERE ${selectorValuePairs.joinToString(", ") { it.first + " = " + it.second }};")
    })!!.closeAfter { this.getInt("_count") }
    
    fun getLimitedRows(table: String, vararg selectorColumns: String) = queryRaw(buildString { 
        append("SELECT ${selectorColumns.joinToString(", ")} FROM $table;")
    })!!
    
    fun getContrainedRows(table: String, vararg constraints: Pair<String, String>) = queryRaw(buildString { 
        append("SELECT * FROM $table WHERE ${constraints.joinToString(", ") { it.first + " = " + it.second }};")
    })!!
    
    fun getLimitedConstrainedRows(table: String, selectorColumns: Array<String>, vararg constraints: Pair<String, String>) = queryRaw(buildString {
        append("SELECT ${selectorColumns.joinToString(", ")} FROM $table WHERE ${constraints.joinToString(", ") { it.first + " = " + it.second }};")
    })!!
    
    override fun close() {
        statements.filter { !it.isClosed }.forEach { it.close() }
        
        connection.close()
        isOpen = false
    }
    
    private fun generateStatement(): Statement {
        val statement = connection.createStatement()
        statement.queryTimeout = 30 //30 sec timeout
        statement.closeOnCompletion()
        return statement
    }
    
    private fun Array<out Pair<String, String>>.toSql() = buildString { 
        this@toSql.forEach { 
            if (this.isNotEmpty())
                append(", ")
            
            val (name, type) = it
            append("$name $type")
        }
    }
}

inline fun <T> ResultSet.closeAfter(func: ResultSet.() -> T): T {
    val returnVal = func(this)
    this.close()
    return returnVal
}

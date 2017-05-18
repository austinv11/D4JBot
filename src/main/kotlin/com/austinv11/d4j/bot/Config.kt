package com.austinv11.d4j.bot

import com.austinv11.d4j.bot.extensions.json
import com.austinv11.d4j.bot.extensions.obj
import java.io.File

val CONFIG: Config by lazy { 
    val configFile = File("./config.json")
    if (configFile.exists())
        return@lazy configFile.readText().obj<Config>()
    else {
        val returnVal = Config()
        returnVal.save()
        return@lazy returnVal
    }
}

data class Config(var prefix: String = "~",
                  var success_message: String = ":ok_hand:",
                  var error_message: String = ":poop:",
                  var ignored: Array<String> = emptyArray()) {
    
    fun save() {
        File("./config.json").writeText(this.json)
    }
}

package com.austinv11.d4j.bot.extensions

import com.google.gson.GsonBuilder

val GSON = GsonBuilder().setPrettyPrinting().serializeNulls().create()!!

val Any.json : String
    get() = GSON.toJson(this)

inline fun <reified T> String.obj(): T = GSON.fromJson(this, T::class.java)

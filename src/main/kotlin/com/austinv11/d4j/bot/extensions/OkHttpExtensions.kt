package com.austinv11.d4j.bot.extensions

import okhttp3.*
import org.apache.commons.io.IOUtils
import reactor.core.publisher.Mono
import java.io.File
import java.io.IOException

val rest = OkHttpClient()

private val JSON = MediaType.parse("application/json; charset=utf-8")

inline fun <reified T> String.get(): Mono<T> = Mono.create {
    rest.newCall(Request.Builder().url(this).addHeader("Content-Type", "application/json; charset=utf-8").build()).enqueue(object: Callback {
        override fun onFailure(call: Call?, e: IOException?) {
            it.error(e)
        }

        override fun onResponse(call: Call?, response: Response?) {
            it.success(response?.body()?.string()?.obj<T>())
        }
    })
}

fun String.download(to: File): Mono<Unit> = Mono.from {
    try {
        println("PLEASE WORK")
        val response = rest.newCall(Request.Builder().url(this).build()).execute()
        println("EXECUTED")
        IOUtils.copy(response!!.body()!!.byteStream()!!, to.outputStream())
        println("WORKED")
    } catch (e: Throwable) {
        println("DID NOT WORK")
    }
}
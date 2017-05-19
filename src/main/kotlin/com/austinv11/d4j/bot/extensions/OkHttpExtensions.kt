package com.austinv11.d4j.bot.extensions

import okhttp3.*
import org.apache.commons.io.IOUtils
import reactor.core.publisher.Mono
import java.io.File
import java.util.concurrent.TimeUnit

val rest = OkHttpClient.Builder().connectTimeout(5, TimeUnit.MINUTES).readTimeout(5, TimeUnit.MILLISECONDS).writeTimeout(5, TimeUnit.MILLISECONDS).build()

private val JSON = MediaType.parse("application/json; charset=utf-8")

inline fun <reified T> String.get(): Mono<T> = Mono.create {
    try {
        val response = rest.newCall(Request.Builder().url(this).addHeader("Content-Type", "application/json; charset=utf-8").build()).execute()
        it.success(response?.body()?.string()?.obj<T>())
    } catch (e: Throwable) {
        it.error(e)
    }
}

fun String.download(to: File): Mono<Boolean> = Mono.create {
    try {
        val response = rest.newCall(Request.Builder().url(this).build()).execute()
        IOUtils.copy(response!!.body()!!.byteStream()!!, to.outputStream())
        it.success(true)
    } catch (e: Throwable) {
        it.error(e)
    }
}
package com.austinv11.d4j.bot

val JAR_PATH: String = Dummy::class.java.protectionDomain.codeSource.location.toURI().path //Locates jar file (this doesn't work too well when it isn't compiled to a jar)

fun main(args: Array<String>) {
    if (args.isEmpty()) throw IllegalArgumentException("Too foo arguments!")
    
    do {
        val slave = createSlave(args[0])
    } while (slave.waitFor() != EXIT_CODE)
}

fun createSlave(token: String): Process = ProcessBuilder("java", "-cp", JAR_PATH, "com.austinv11.d4j.bot.SlaveKt", token).inheritIO().start()

private class Dummy

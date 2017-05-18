package com.austinv11.d4j.bot.extensions

import com.austinv11.rx.ReactorEventAdapter
import reactor.core.publisher.Flux
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.api.events.Event

inline fun <reified T : Event> ReactorEventAdapter.stream(): Flux<T> {
    return stream(T::class.java)
}

private var _adapter: ReactorEventAdapter? = null

val IDiscordClient.events: ReactorEventAdapter
    get() {
        if (_adapter == null) _adapter = ReactorEventAdapter(dispatcher)
        
        return _adapter!!
    }

inline fun <reified T : Event> IDiscordClient.stream(): Flux<T> {
    return events.stream<T>()
}

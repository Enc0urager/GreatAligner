package dev.enco.greataligner.repository

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import dev.enco.greataligner.config.Config
import java.util.UUID
import java.util.concurrent.TimeUnit

object CooldownRepository {
    lateinit var cache: Cache<UUID, Boolean>

    fun init(config: Config) {
        cache = Caffeine.newBuilder()
            .expireAfterWrite(config.cooldown, TimeUnit.MILLISECONDS)
            .build()
    }

    fun set(uuid: UUID) = cache.put(uuid, true)
    fun has(uuid: UUID) = cache.getIfPresent(uuid) ?: false
}

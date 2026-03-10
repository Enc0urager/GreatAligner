package dev.enco.greataligner.repository

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.UUID
import java.util.concurrent.TimeUnit

object CooldownRepository {
    val cache: Cache<UUID, Boolean> = Caffeine.newBuilder()
        .expireAfterWrite(1000L, TimeUnit.MILLISECONDS)
        .build()

    fun set(uuid: UUID) = cache.put(uuid, true)
    fun has(uuid: UUID) = cache.getIfPresent(uuid) ?: false
}

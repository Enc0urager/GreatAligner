package dev.enco.greataligner

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import de.oliver.fancyholograms.api.FancyHologramsPlugin
import de.oliver.fancyholograms.api.data.TextHologramData
import de.oliver.fancyholograms.api.hologram.Hologram
import dev.enco.greataligner.config.Config
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.abs

object FHoloManager {
    val holoHitboxes = mutableListOf<HoloHitbox>()
    lateinit var playersCache : Cache<UUID, Object2IntOpenHashMap<String>>
    var maxOffset: Int = 40

    fun init(config: Config) {
        playersCache = Caffeine.newBuilder()
            .expireAfterAccess(config.offsetCacheExpireTime, TimeUnit.SECONDS).build()
        setupHoloHitboxes(config)
        maxOffset = config.maxOffset
    }

    fun setupHoloHitboxes(config: Config) {
        val manager = FancyHologramsPlugin.get().hologramManager
        config.holo2LbMap.keys.forEach { key ->
            val holo = manager.getHologram(key).get()
            val data = holo.data; val loc = data.location
            var height = 1.0; var radius = 1.0

            if (data is TextHologramData) {
                val lines = data.text.size.coerceAtLeast(1)
                height = lines * 0.3 * data.scale.y
                val longestLine = data.text
                    .map { line -> strip(line) }
                    .maxByOrNull { it.length }?.length ?: 1
                radius = longestLine * 0.15 * data.scale.x / 2
            }

            val halfHeight = height / 2.0
            val center = loc.clone().toVector().add(Vector(0.0, height / 2.0, 0.0))

            holoHitboxes.add(HoloHitbox(
                center,
                halfHeight,
                radius * radius,
                key
            ))
        }
    }

    private fun strip(line: String): String =
        line.replace(Regex("<[^>]*>"), "")
            .replace(Regex("&#[A-Fa-f0-9]{6}"), "")
            .replace(Regex("[&§][0-9a-fk-orA-FK-OR]"), "")
            .replace(Regex("%[^%]*%"), "")

    private fun map(uuid: UUID) =
        playersCache.get(uuid) { Object2IntOpenHashMap() }

    fun getOffset(uuid: UUID, lb: String): Int =
        map(uuid).getOrDefault(lb, 0)

    fun scroll(uuid: UUID, lb: String, from: Int, to: Int): Int {
        val direction = if ((to - from + 9) % 9 == 1) 1 else -1
        val offsets = map(uuid)
        val next = (offsets.getOrDefault(lb, 0) + direction)
            .coerceIn(0, maxOffset)
        offsets[lb] = next; return next
    }

    fun getLookingHolo(player: Player, maxDistance: Double = 5.0): Hologram? {
        val eyeLoc = player.eyeLocation
        val origin = eyeLoc.toVector()
        val direction = eyeLoc.direction

        var closestHoloName: String? = null
        var minDistance = Double.MAX_VALUE

        for (hitbox in holoHitboxes) {
            val toHolo = hitbox.center.clone().subtract(origin)
            val distanceAlongRay = toHolo.dot(direction)

            if (distanceAlongRay !in 0.0..maxDistance) continue

            val pointOnRay = origin.clone().add(direction.clone().multiply(distanceAlongRay))

            val dy = abs(pointOnRay.y - hitbox.center.y)
            if (dy > hitbox.halfHeight) continue

            val dx = pointOnRay.x - hitbox.center.x
            val dz = pointOnRay.z - hitbox.center.z
            val horizontalDistSq = (dx * dx) + (dz * dz)

            if (horizontalDistSq <= hitbox.radiusSq) {
                if (distanceAlongRay < minDistance) {
                    minDistance = distanceAlongRay
                    closestHoloName = hitbox.holo
                }
            }
        }

        if (closestHoloName == null) return null
        return FancyHologramsPlugin.get().hologramManager.getHologram(closestHoloName).orElse(null)
    }
}
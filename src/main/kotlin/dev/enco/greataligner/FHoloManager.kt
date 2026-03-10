package dev.enco.greataligner

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import de.oliver.fancyholograms.api.FancyHologramsPlugin
import de.oliver.fancyholograms.api.hologram.Hologram
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.TimeUnit

object FHoloManager {
    val playersCache : Cache<UUID, Object2IntOpenHashMap<String>> = Caffeine.newBuilder()
        .expireAfterAccess(180, TimeUnit.SECONDS).build()

    private fun map(uuid: UUID) =
        playersCache.get(uuid) { Object2IntOpenHashMap() }

    fun getOffset(uuid: UUID, lb: String): Int =
        map(uuid).getOrDefault(lb, 0)

    fun scroll(uuid: UUID, lb: String, from: Int, to: Int): Int {
        val direction = if ((to - from + 9) % 9 == 1) 1 else -1
        val offsets = map(uuid)
        val next = (offsets.getOrDefault(lb, 0) + direction)
            .coerceIn(0, 40)
        offsets[lb] = next; return next
    }

    fun getLookingHolo(player: Player, maxDistance: Double = 5.0): Hologram? {
        val hologramManager = FancyHologramsPlugin.get().hologramManager
        val eyeLoc = player.eyeLocation
        val origin = eyeLoc.toVector()
        val direction = eyeLoc.direction

        var closestHolo: Hologram? = null
        var minDistance = Double.MAX_VALUE

        // CURRENT TEMPORARY IMPLEMENTATION: Mathematical Raycasting
        // Currently simulating a circular "hitbox" around the hologram's config location
        // This works, but it's a dirty workaround. It forces server admins
        // to manually configure hitbox sizes for each hologram in the config.
        val hitRadiusSq = 1.5 * 1.5

        for (hologram in hologramManager.holograms) {
            val holoLoc = hologram.data.location
            val holoVec = holoLoc.toVector()

            val toHolo = holoVec.clone().subtract(origin)
            val distanceAlongRay = toHolo.dot(direction)
            if (distanceAlongRay !in 0.0..maxDistance) continue

            val pointOnRay = origin.clone().add(direction.clone().multiply(distanceAlongRay))
            val distSqToCenter = pointOnRay.distanceSquared(holoVec)

            if (distSqToCenter <= hitRadiusSq && distanceAlongRay < minDistance) {
                minDistance = distanceAlongRay
                closestHolo = hologram
            }
        }

        return closestHolo
    }

    /*
    ==========================
         FAILED ATTEMPTS
    ==========================

    ATTEMPT 1: Using hologram.displayEntity?.boundingBox
    Reason for failure: The 'displayEntity' property is deprecated (marked for removal)
    and always returns null in the current API version, so we can't get the bounding box this way.

    fun getLookingHolo(player: Player, maxDistance: Double = 5.0): Hologram? {
        val hologramManager = FancyHologramsPlugin.get().hologramManager
        val eyeLoc = player.eyeLocation
        val start = eyeLoc.toVector()
        val direction = eyeLoc.direction

        return hologramManager.holograms.firstOrNull { hologram ->
            val box = hologram.displayEntity?.boundingBox ?: return@firstOrNull false
            box.rayTrace(start, direction, maxDistance) != null
        }
    }

    ---------------------------------------------------------------------------

    ATTEMPT 2: Using Bukkit's built-in rayTraceEntities
    Reason for failure: FancyHolograms are client-side only (spawned via packets).
    They do not exist as standard Bukkit entities on the server, so the server's
    rayTrace calculation completely ignores them.

    fun getLookingHolo(player: Player): Hologram? {
        val rayTrace = player.world.rayTraceEntities(
            player.eyeLocation,
            player.eyeLocation.direction,
            5.0
        ) { entity -> entity is Interaction || entity is Display } ?: return null

        val hitEntity = rayTrace.hitEntity ?: return null

        val hologramManager = FancyHologramsPlugin.get().hologramManager

        return hologramManager.holograms.firstOrNull { hologram ->
            hologram.entityId == hitEntity.entityId
        }
    }
    */
}
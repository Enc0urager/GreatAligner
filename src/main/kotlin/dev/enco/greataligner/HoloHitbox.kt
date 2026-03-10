package dev.enco.greataligner

import org.bukkit.util.Vector

data class HoloHitbox(
    val center: Vector,
    val halfHeight: Double,
    val radiusSq: Double,
    val holo: String
)

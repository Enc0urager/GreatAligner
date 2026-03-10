package dev.enco.modernleaderboards.utils

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

object Colorizer {
    private val miniMessage = MiniMessage.miniMessage();
    private val legacySection = LegacyComponentSerializer.legacySection();
    fun colorize(text: String): String = legacySection.serialize(miniMessage.deserialize(text))
}
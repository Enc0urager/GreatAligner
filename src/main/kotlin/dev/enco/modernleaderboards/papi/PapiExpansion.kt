package dev.enco.modernleaderboards.papi

import dev.enco.modernleaderboards.FHoloManager
import dev.enco.modernleaderboards.config.Config
import dev.enco.modernleaderboards.math.Calculator
import dev.enco.modernleaderboards.repository.LbRepository
import dev.enco.modernleaderboards.utils.Colorizer
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.ChatColor
import org.bukkit.OfflinePlayer
import java.text.MessageFormat

class PapiExpansion(
    private val config: Config,
) : PlaceholderExpansion() {
    override fun getIdentifier(): String = "greataligner"
    override fun getAuthor(): String = "Encourager"
    override fun getVersion(): String = "1.0"
    override fun canRegister(): Boolean = true;

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        val scrolling = params.startsWith("scrolling_")
        val raw = if (scrolling) params.removePrefix("scrolling_") else params
        val (placeStr, lb) = raw.split("_", limit = 2).takeIf { it.size == 2 } ?: return null
        var place = placeStr.toIntOrNull() ?: return "Illegal lb pattern"
        if (scrolling && player != null)
            place += FHoloManager.getOffset(player.uniqueId, lb)
        val base = "%ajlb_lb_${lb}_${place}_alltime"
        fun ph(suffix: String) = PlaceholderAPI.setPlaceholders(player, "${base}_$suffix%")
        return formatEntry(player, place, lb, ph("prefix"), ph("name"), ph("value"))
    }

    private fun formatEntry(player: OfflinePlayer?, place: Int, lb: String, prefix: String, name: String, value: String): String {
        val format = config.getLbFormat(lb) ?: return "Format not found"
        val placeText = config.getPlaceChar(place)

        val totalW =
            Calculator.getStringWidth(placeText, config.cachePlace) +
                    Calculator.getStringWidth(ChatColor.stripColor(prefix).orEmpty(), config.cachePrefix) +
                    Calculator.getStringWidth(name, config.cacheName) +
                    Calculator.getStringWidth(value, config.cacheValue)

        val maxW = LbRepository.get(lb).let { current ->
            (current + config.spaceExtensionStep)
                .takeIf { totalW + config.minSpaceWidth > current }
                ?: current
        }.also { LbRepository.put(lb, it) }

        val pixelsNeeded = maxW - totalW
        val spaces = config.spaceSymbol.repeat((pixelsNeeded / config.spaceSymbolWidth).coerceAtLeast(0))

        val result = MessageFormat.format(format, placeText, name, prefix, value, spaces, pixelsNeeded)
        return PlaceholderAPI.setPlaceholders(player, Colorizer.colorize(result))
    }
}
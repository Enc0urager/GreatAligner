package dev.enco.greataligner

import dev.enco.greataligner.config.Config
import dev.enco.greataligner.repository.CooldownRepository
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemHeldEvent

class ScrollingListener(
    private val config : Config
) : Listener {
    @EventHandler fun onScroll(e: PlayerItemHeldEvent) {
        val player = e.player
        val uuid = player.uniqueId

        if (CooldownRepository.has(uuid)) return

        val holo = FHoloManager.getLookingHolo(player)

        if (holo == null) {
            CooldownRepository.set(uuid)
            return
        }

        val lb = config.getLbFromHolo(holo.name) ?: return

        e.isCancelled = true

        FHoloManager.scroll(
            uuid,
            lb,
            e.previousSlot,
            e.newSlot
        )
        holo.refreshHologram(player)
    }
}
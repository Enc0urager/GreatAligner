package dev.enco.greataligner

import dev.enco.greataligner.command.ReloadCommand
import dev.enco.greataligner.config.Config
import dev.enco.greataligner.papi.PapiExpansion
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin() {
    override fun onEnable() {
        val config = Config(this)
        PapiExpansion(config).also { it.register() }
        getCommand("greataligner")?.setExecutor(ReloadCommand(config))
        server.pluginManager.registerEvents(ScrollingListener(config), this)
        Metrics(this, 29887)
    }
}
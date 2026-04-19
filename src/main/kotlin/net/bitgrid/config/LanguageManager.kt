package net.bitgrid.config

import net.bitgrid.Bitgrid
import org.bukkit.ChatColor
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File


class LanguageManager(private val plugin: Bitgrid) {

    companion object {
        lateinit var instance: LanguageManager
            private set
    }

    private lateinit var langConfig: FileConfiguration

    fun load() {
        instance = this
        val lang = plugin.config.getString("language") ?: "en"

        val langFile = File(plugin.dataFolder, "lang/$lang.yml")

        if(!langFile.exists()) {
            langFile.parentFile.mkdirs()
            plugin.saveResource("lang/$lang.yml", false)
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile)
        plugin.logger.info("Loaded language file: $lang.yml")
    }

    fun get(path: String, vararg replacements: Pair<String, String>): String {
        var message = langConfig.getString(path) ?: return "&c[$path] is missing"

        replacements.forEach { (key, value) ->
            message = message.replace("{$key}", value)
        }

        return ChatColor.translateAlternateColorCodes('&', message)
    }
}

fun lang(path: String, vararg replacements: Pair<String, String>): String {
    return LanguageManager.instance.get(path, *replacements)
}
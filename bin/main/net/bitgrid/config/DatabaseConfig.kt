package net.bitgrid.config

import org.bukkit.configuration.file.FileConfiguration


data class DatabaseConfig(
    val type: String,
    val host: String,
    val port: Int,
    val name: String,
    val username: String,
    val password: String
) {
    companion object {
        fun fromConfig(config: FileConfiguration): DatabaseConfig {
            val section = config.getConfigurationSection("database")
                ?: error("database section not found in config.yml")

            return DatabaseConfig(
                type = section.getString("type", "sqlite")!!.lowercase(),
                host = section.getString("host", "localhost")!!,
                port = section.getInt("port", 3306),
                name = section.getString("name", "bitgrid")!!,
                username = section.getString("username", "root")!!,
                password = section.getString("password", "")!!
            )
        }
    }
}
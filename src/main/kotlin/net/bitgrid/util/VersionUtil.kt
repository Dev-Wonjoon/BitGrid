package net.bitgrid.util

import org.bukkit.Bukkit

object VersionUtil {
    val version: String by lazy {
        Bukkit.getBukkitVersion().split("-")[0]
    }

    private val parts by lazy { version.split(".").map  { it.toIntOrNull() ?: 0 }}

    val major: Int by lazy { parts[0] }
    val minor: Int by lazy { if(parts.size >= 2) parts[1] else 0 }

    fun isAtLeast(major: Int, minor: Int): Boolean {
        if(this.major > major) return true
        if(this.major == major && this.minor >= minor) return true
        return false
    }
}
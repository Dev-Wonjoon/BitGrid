package net.bitgrid.util

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin


class ChatInputUtil(private val plugin: JavaPlugin) : Listener {
    private val callbacks = mutableMapOf<Player, (String) -> Unit>()

    fun init() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    fun open(player: Player, callback: (String) -> Unit) {
        callbacks[player] = callback
        player.sendMessage("§e§l[BitGrid] §f검색어를 채팅에 입력하세요. §7(취소: \"cancel\")")
    }

    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        val player = event.player
        val callback = callbacks.remove(player) ?: return
        event.isCancelled = true

        val input = if(event.message.equals("cancel", ignoreCase = true)) "" else event.message.trim()

        Bukkit.getScheduler().runTask(plugin, Runnable {
            callback(input)
        })
    }

    fun isOpen(player: Player): Boolean = callbacks.containsKey(player)

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        callbacks.remove(event.player)
    }

    fun cleanup(player: Player) {
        callbacks.remove(player)
    }
}
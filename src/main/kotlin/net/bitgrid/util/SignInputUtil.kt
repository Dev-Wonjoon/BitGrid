package net.bitgrid.util

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.block.sign.Side
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin

class SignInputUtil(private val plugin: JavaPlugin): Listener {
    private val callbacks = mutableMapOf<Player, (String) -> Unit>()

    fun init() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    fun open(player: Player, callback: (String) -> Unit) {
        callbacks[player] = callback

        val loc = player.location.clone()
        // 플레이어 UUID 기반으로 X, Z 오프셋을 줘서 겹침 방지
        val hash = player.uniqueId.hashCode()
        loc.x = (hash % 10000).toDouble()
        loc.z = ((hash / 10000) % 10000).toDouble()
        loc.y = loc.world!!.minHeight.toDouble()

        loc.chunk.load()

        val block = loc.block
        block.type = Material.OAK_SIGN

        val sign = block.state as Sign
        val side = sign.getSide(Side.FRONT)
        side.setLine(1, "^^^^^^^^")
        side.setLine(2, "")
        side.setLine(3, "^^^^^^^^")
        sign.update()

        player.openSign(sign, Side.FRONT)
    }

    @EventHandler
    fun onSignChange(event: SignChangeEvent) {
        val player = event.player
        val callback = callbacks.remove(player) ?: return
        event.isCancelled = true

        val input = (event.getLine(0) ?: "").trim()

        event.block.type = Material.AIR

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            Bukkit.getScheduler().runTask(plugin, Runnable {
                callback(input)
            })
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
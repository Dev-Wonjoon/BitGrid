package net.R1_21_1.listener

import net.R1_21_1.gui.StorageGui
import net.bitgrid.Bitgrid
import net.bitgrid.config.lang
import net.bitgrid.service.StorageService
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

class DepositHandler(
    private val plugin: Bitgrid,
    private val storageGui: StorageGui,
    private val storageService: StorageService
) {
    fun handle(event: InventoryClickEvent, player: Player, gridId: String) {
        val clicked = event.currentItem ?: return
        if(clicked.type.isAir) return

        if(event.click == ClickType.NUMBER_KEY || event.click == ClickType.DOUBLE_CLICK) return

        if(event.isShiftClick) {
            val itemToDeposit = clicked.clone()

            Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                val success = kotlinx.coroutines.runBlocking {
                    storageService.deposit(gridId, itemToDeposit)
                }

                Bukkit.getScheduler().runTask(plugin, Runnable {
                    if(success) {
                        event.currentItem = null

                        storageGui.open(player, gridId, storageGui.getPage(player))
                    } else {
                        player.sendMessage(lang("message.storage.overflow"))
                    }
                })
            })
        }
    }
}
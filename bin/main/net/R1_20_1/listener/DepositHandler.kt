package net.R1_20_1.listener

import net.R1_20_1.gui.StorageGui
import net.bitgrid.service.StorageService
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class DepositHandler(
    private val storageGui: StorageGui,
    private val storageService: StorageService
) {
    fun handle(event: InventoryClickEvent, player: Player, gridId: String) {
        val clicked = event.currentItem ?: return

        if(event.isShiftClick) {
            if(storageService.deposit(gridId, clicked)) {
                event.currentItem = null
                storageGui.open(player, gridId, storageGui.getPage(player))
            }
        }
    }
}
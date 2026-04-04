package net.R1_20_1.listener

import net.R1_20_1.gui.StorageGui
import net.bitgrid.storage.StorageService
import net.bitgrid.util.SignInputUtil
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class NavigationHandler(
    private val storageGui: StorageGui,
    private val storageService: StorageService,
    private val signInputUtil: SignInputUtil
) {
    fun handle(event: InventoryClickEvent, player: Player, gridId: String) {
        when(event.rawSlot) {
            48 -> onPrevious(player, gridId)
            49 -> onSearch(player, gridId)
            50 -> onNext(player, gridId)
        }
    }

    private fun onPrevious(player: Player, gridId: String) {
        val page = storageGui.getPage(player)
        if(page > 0) storageGui.open(player, gridId, page - 1)
    }

    private fun onNext(player: Player, gridId: String) {
        val page = storageGui.getPage(player)
        storageGui.open(player, gridId, page + 1)
    }

    private fun onSearch(player: Player, gridId: String) {
        player.closeInventory()

        signInputUtil.open(player) { input ->
            if(input.isBlank()) {
                storageGui.setSearch(player, null)
            } else {
                storageGui.setSearch(player, input)
            }
            storageGui.open(player, gridId, 0)
        }
    }

    private fun onSort(player: Player, gridId: String) {
        storageGui.sortManager.nextSort(player)
        storageGui.open(player, gridId, 0)
    }
}
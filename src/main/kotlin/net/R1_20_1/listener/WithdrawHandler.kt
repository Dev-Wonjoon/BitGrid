package net.R1_20_1.listener

import net.R1_20_1.gui.StorageGui
import net.bitgrid.storage.StorageService
import net.bitgrid.storage.StoredItem
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class WithdrawHandler(
    private val storageGui: StorageGui,
    private val storageService: StorageService
) {
    fun handle(event: InventoryClickEvent, player: Player, gridId: String) {
        val sort = storageGui.sortManager.getSort(player)
        val items = if(storageGui.getSearch(player) != null) {
            storageService.searchItems(gridId, storageGui.getSearch(player)!!, sort)
        } else {
            storageService.getItems(gridId, sort)
        }

        val index = storageGui.getPage(player) * StorageGui.ITEMS_PER_PAGE + event.rawSlot

        if(index >= items.size) return

        val storedItem = items[index]

        when {
            event.isShiftClick && event.isLeftClick ->
                shiftLeftClick(player, gridId, storedItem)
            event.isLeftClick -> leftClick(event, player, gridId, storedItem)
            event.isRightClick -> rightClick(event, player, gridId, storedItem)
        }
    }

    // 좌클릭
    private fun leftClick(event: InventoryClickEvent, player: Player, gridId: String, storedItem: StoredItem) {
        val withdrawn = storageService.withdraw(gridId, storedItem.itemHash, 1) ?: return
        event.view.cursor = withdrawn
        storageGui.open(player, gridId, storageGui.getPage(player))
    }

    // Shift + 좌클릭
    private fun shiftLeftClick(player: Player, gridId: String, storedItem: StoredItem) {
        if(player.inventory.firstEmpty() == -1) return

        val stackSize = storedItem.item.maxStackSize.coerceAtMost((storedItem.amount))
        val withdrawn = storageService.withdraw(gridId, storedItem.itemHash, stackSize) ?: return
        player.inventory.addItem(withdrawn)
        storageGui.open(player, gridId, storageGui.getPage(player))
    }

    // 우클릭
    private fun rightClick(event: InventoryClickEvent, player: Player, gridId: String, storedItem: StoredItem) {
        val half = (storedItem.amount + 1) / 2
        val stackSize = storedItem.item.maxStackSize.coerceAtMost(half)
        val withdrawn = storageService.withdraw(gridId, storedItem.itemHash, stackSize) ?: return
        event.view.cursor = withdrawn
        storageGui.open(player, gridId, storageGui.getPage(player))
    }
}
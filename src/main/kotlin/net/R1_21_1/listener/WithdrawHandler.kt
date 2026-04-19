package net.R1_21_1.listener

import net.R1_21_1.gui.StorageGui
import net.bitgrid.config.lang
import net.bitgrid.service.StorageService
import net.bitgrid.service.StoredItem
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent

class WithdrawHandler(
    private val storageGui: StorageGui,
    private val storageService: StorageService
) {
    fun handle(event: InventoryClickEvent, player: Player, gridId: String) {
        if(event.click == ClickType.NUMBER_KEY || event.click == ClickType.DOUBLE_CLICK) return

        val sort = storageGui.sortManager.getSort(player)
        val search = storageGui.getSearch(player)
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

        if(event.view.cursor != null && !event.view.cursor!!.type.isAir) return

        val withdrawn = storageService.withdraw(gridId, storedItem.itemHash, 1) ?: return

        event.view.cursor = withdrawn
        storageGui.open(player, gridId, storageGui.getPage(player))
    }

    // Shift + 좌클릭
    private fun shiftLeftClick(player: Player, gridId: String, storedItem: StoredItem) {

        if(player.inventory.firstEmpty() == -1) return

        val stackSize = storedItem.item.maxStackSize.coerceAtMost((storedItem.amount))

        val withdrawn = storageService.withdraw(gridId, storedItem.itemHash, stackSize) ?: return

        val leftovers = player.inventory.addItem(withdrawn)

        if(leftovers.isNotEmpty()) {
            leftovers.values.forEach { leftoverItem ->
                storageService.deposit(gridId, leftoverItem)
            }
            player.sendMessage(lang("message.storage.inventory_full"))
        }
        storageGui.open(player, gridId, storageGui.getPage(player))
    }

    // 우클릭
    private fun rightClick(event: InventoryClickEvent, player: Player, gridId: String, storedItem: StoredItem) {
        if(event.view.cursor != null && !event.view.cursor!!.type.isAir) return

        val half = (storedItem.amount + 1) / 2
        val stackSize = storedItem.item.maxStackSize.coerceAtMost(half)

        val withdrawn = storageService.withdraw(gridId, storedItem.itemHash, stackSize) ?: return

        event.view.cursor = withdrawn
        storageGui.open(player, gridId, storageGui.getPage(player))
    }
}
package net.R1_20_1.gui

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

class NavigationBarBuilder(private val  sortManager: SortManager) {

    fun fill(inventory: Inventory, currentPage: Int, maxPage: Int, player: Player) {
        val filler = GuiItemFactory.createFillerItem()

        inventory.setItem(45, GuiItemFactory.createUpgradeItem())
        inventory.setItem(46, filler)
        inventory.setItem(47, filler)

        if(currentPage > 0) {
            inventory.setItem(48, GuiItemFactory.createNavItem("§a◀ 이전", currentPage))
        } else {
            inventory.setItem(48, filler)
        }

        inventory.setItem(49, GuiItemFactory.createSearchItem())

        if(currentPage < maxPage) {
            inventory.setItem(50, GuiItemFactory.createNavItem("§a다음 ▶", currentPage + 2))
        } else {
            inventory.setItem(50, filler)
        }

        inventory.setItem(51, filler)
        inventory.setItem(52, filler)
        inventory.setItem(53, sortManager.createSortIem(sortManager.getSort(player)))
    }
}
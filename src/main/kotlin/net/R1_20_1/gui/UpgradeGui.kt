package net.R1_20_1.gui

import net.bitgrid.storage.UpgradeService
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class UpgradeGui {
    companion object {
        const val TITLE = "Bit Grid §7- §b업그레이드"
        const val SIZE = 27
    }

    private val upgradeService = UpgradeService()

    fun open(player: Player, gridId: String) {
        val inventory = org.bukkit.Bukkit.createInventory(null, SIZE, TITLE)

        val stackLevel = upgradeService.getStackLevel(gridId)
        val pageLevel = upgradeService.getPageLevel(gridId)

        // 스택 업그레이드 아이템
        val stackItem = createUpgradeItem("스택 업그레이드", stackLevel, "stack")
        inventory.setItem(10, stackItem)

        // 페이지 업그레이드 아이템
        val pageItem = createUpgradeItem("페이지 업그레이드", pageLevel, "page")
        inventory.setItem(16, pageItem)

        player.openInventory(inventory)
    }

    private fun createUpgradeItem(name: String, currentLevel: Int, type: String): ItemStack {
        val item = ItemStack(Material.ENCHANTED_BOOK)
        val meta = item.itemMeta!!

        meta.setDisplayName("§b$name")

        val lore = mutableListOf<String>()
        lore.add("§7현재 레벨: §f$currentLevel")
        lore.add("§7최대 레벨: §f${UpgradeService.MAX_LEVEL}")

        if(currentLevel < UpgradeService.MAX_LEVEL) {
            lore.add("§a업그레이드 가능")
        } else {
            lore.add("§c최대 레벨 도달")
        }

        meta.lore = lore
        item.itemMeta = meta

        return item
    }
}
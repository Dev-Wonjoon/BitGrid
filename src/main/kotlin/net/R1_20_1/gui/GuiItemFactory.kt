package net.R1_20_1.gui

import net.bitgrid.storage.StoredItem
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object GuiItemFactory {

    fun createDisplayItem(storedItem: StoredItem): ItemStack {
        val display = storedItem.item.clone()
        val meta = display.itemMeta!!
        val lore = meta.lore?.toMutableList() ?: mutableListOf()
        lore.add("")
        lore.add("§7수량: §f${String.format("%,d", storedItem.amount)}")
        display.itemMeta = meta
        display.amount = 1
        return display
    }

    fun createFillerItem(): ItemStack {
        val item = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        val meta = item.itemMeta!!
        meta.setDisplayName(" ")
        item.itemMeta = meta
        return item
    }

    fun createNavItem(name: String, pageDisplay: Int): ItemStack {
        val item = ItemStack(Material.ARROW)
        val meta = item.itemMeta!!
        meta.setDisplayName(name)
        meta.lore = listOf("§7페이지 $pageDisplay")
        item.itemMeta = meta
        return item
    }

    fun createSearchItem(): ItemStack {
        val item = ItemStack(Material.OAK_SIGN)
        val meta = item.itemMeta!!
        meta.setDisplayName("§7검색")
        meta.lore = listOf("§7클릭하여 아이템을 검색합니다.")
        item.itemMeta = meta
        return item
    }
}
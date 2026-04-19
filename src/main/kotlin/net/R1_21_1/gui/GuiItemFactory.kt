package net.R1_21_1.gui

import net.bitgrid.config.lang
import net.bitgrid.service.StoredItem
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object GuiItemFactory {

    fun createDisplayItem(storedItem: StoredItem): ItemStack {
        val display = storedItem.item.clone()
        val meta = display.itemMeta!!
        val lore = meta.lore?.toMutableList() ?: mutableListOf()
        lore.add("")
        lore.add(lang("gui.quantity", "amount" to String.format("%,d", storedItem.amount)))
        meta.lore = lore
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
        meta.lore = listOf(lang("gui.page", "page" to "$pageDisplay"))
        item.itemMeta = meta
        return item
    }

    fun createSearchItem(): ItemStack {
        val item = ItemStack(Material.OAK_SIGN)
        val meta = item.itemMeta!!
        meta.setDisplayName(lang("gui.search_name"))
        meta.lore = listOf(lang("gui.search_lore"))
        item.itemMeta = meta
        return item
    }

    fun createUpgradeItem(): ItemStack {
        val item = ItemStack(Material.ANVIL)
        val meta = item.itemMeta!!
        meta.setDisplayName(lang("gui.upgrade_name"))
        meta.lore = listOf(lang("gui.upgrade_lore"))
        item.itemMeta = meta
        return item
    }
}
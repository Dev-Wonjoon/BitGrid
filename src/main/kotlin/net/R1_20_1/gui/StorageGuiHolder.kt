package net.R1_20_1.gui

import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class StorageGuiHolder(val gridId: String) : InventoryHolder {
    private lateinit var inventory: Inventory
    override fun getInventory(): Inventory = inventory

    fun createInventory(size: Int, title: String): Inventory {
        inventory = Bukkit.createInventory(this, size, title)
        return inventory
    }


}
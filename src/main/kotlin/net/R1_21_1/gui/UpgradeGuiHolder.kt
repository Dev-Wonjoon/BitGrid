package net.R1_21_1.gui

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class UpgradeGuiHolder(val gridId: String) : InventoryHolder {
    private lateinit var inventoryHolder: InventoryHolder
    override fun getInventory() : Inventory = inventory
}
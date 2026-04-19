package net.R1_21_1.holder

import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

open class BitGridHolder(val gridId: String) : InventoryHolder {
    private lateinit var inventory: Inventory
    override fun getInventory(): Inventory = inventory

    fun createInventory(size: Int, title: String): Inventory {
        inventory = Bukkit.createInventory(this, size, title)
        return inventory
    }
}
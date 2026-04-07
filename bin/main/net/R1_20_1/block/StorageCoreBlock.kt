package net.R1_20_1.block

import net.bitgrid.config.lang
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

class StorageCoreBlock(private val plugin: JavaPlugin) {

    val coreKey = NamespacedKey(plugin, "storage_core")

    // StorageCore 아이템 생성
    fun createCoreItem(): ItemStack {
        val item = ItemStack(Material.BARREL)
        val meta = item.itemMeta!!
        meta.setDisplayName(lang("block.core_name"))
        meta.lore = listOf(lang("block.core_lore"))
        meta.persistentDataContainer.set(coreKey, PersistentDataType.BYTE, 1)
        item.itemMeta = meta
        return item
    }

    // 해당 아이템이 StorageCore인지 확인
    fun isStorageCore(item: ItemStack?): Boolean {
        if(item == null || item.type != Material.BARREL) return false
        val meta = item.itemMeta ?: return false
        return meta.persistentDataContainer.has(coreKey, PersistentDataType.BYTE)
    }
}
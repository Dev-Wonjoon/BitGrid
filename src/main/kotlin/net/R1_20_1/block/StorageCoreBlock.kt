package net.R1_20_1.block

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
        meta.setDisplayName("§bStorage Core")
        meta.lore = listOf("§7아이템을 무제한으로 저장합니다.")
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
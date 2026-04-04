package net.R1_20_1.recipe

import net.R1_20_1.block.StorageCoreBlock
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin

class StorageCoreRecipe(
    private val plugin: JavaPlugin,
    private val storageCoreBlock: StorageCoreBlock
) {
    fun register(config: FileConfiguration) {
        val enabled = config.getBoolean("storage-core.crafting.enabled", true)
        if(!enabled) return

        val key = NamespacedKey(plugin, "storage_core_recipe")
        val recipe = ShapedRecipe(key, storageCoreBlock.createCoreItem())

        val row1 = config.getStringList("storage-core.crafting.recipe.row1")
        val row2 = config.getStringList("storage-core.crafting.recipe.row2")
        val row3 = config.getStringList("storage-core.crafting.recipe.row3")

        val materialMap = mutableMapOf<Material, Char>()
        var charIndex = 'A'

        fun getChar(material: Material): Char {
            return materialMap.getOrPut(material) { charIndex++ }
        }

        val allRow = listOf(row1, row2, row3)
        val shape = allRow.map { row ->
            row.joinToString("") { getChar(Material.valueOf(it)).toString() }
        }

        recipe.shape(shape[0], shape[1], shape[2])

        for((material, char) in materialMap) {
            recipe.setIngredient(char, material)
        }

        plugin.server.addRecipe(recipe)

    }
}
package net.R1_20_1.gui

import net.bitgrid.database.PlayerSettings
import net.bitgrid.storage.SortType
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class SortManager {
    private val sortCache = mutableMapOf<Player, SortType>()

    fun getSort(player: Player): SortType {
        return sortCache.getOrPut(player) {
            transaction {
                PlayerSettings.selectAll()
                    .where { PlayerSettings.playerUuid eq player.uniqueId.toString() }
                    .singleOrNull()
                    ?.get(PlayerSettings.sortType)
                    ?.let { SortType.valueOf(it) }
            } ?: SortType.TIME
        }
    }

    fun nextSort(player: Player) {
        val current = getSort(player)
        val next = SortType.entries[(current.ordinal + 1) % SortType.entries.size]
        sortCache[player] = next
        transaction {
            val exists = PlayerSettings.selectAll()
                .where { PlayerSettings.playerUuid eq player.uniqueId.toString() }
                .singleOrNull()

            if(exists != null) {
                PlayerSettings.update({ PlayerSettings.playerUuid eq player.uniqueId.toString() }) {
                    it[sortType] = next.name
                }
            } else {
                PlayerSettings.insert {
                    it[playerUuid] = player.uniqueId.toString()
                    it[sortType] = next.name
                }
            }
        }
    }

    fun createSortIem(current: SortType): ItemStack {
        val item = ItemStack(Material.HOPPER)
        val meta = item.itemMeta!!
        meta.setDisplayName("§e정렬")
        meta.lore = listOf(
            if(current == SortType.TIME) "§a▶ 시간순" else "§7시간순",
            if(current == SortType.AMOUNT) "§a▶ 개수순" else "§7개수순",
            if(current == SortType.ID)  "§a▶ ID순" else "§7ID순"
            )
        item.itemMeta = meta
        return item
    }

    fun cleanup(player: Player) {
        sortCache.remove(player)
    }

}
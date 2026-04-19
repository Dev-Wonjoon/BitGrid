package net.R1_21_1.gui

import net.bitgrid.config.lang
import net.bitgrid.database.PlayerSettings
import net.bitgrid.service.SortType
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
        meta.setDisplayName(lang("gui.sort_name"))
        meta.lore = listOf(
            if(current == SortType.TIME) "§a▶ ${lang("gui.sort_time")}" else "§7${lang("gui.sort_time")}",
            if(current == SortType.AMOUNT) "§a▶ ${lang("gui.sort_amount")}" else "§7${lang("gui.sort_amount")}",
            if(current == SortType.ID) "§a▶ ${lang("gui.sort_id")}" else "§7${lang("gui.sort_id")}"
        )
        item.itemMeta = meta
        return item
    }

    fun cleanup(player: Player) {
        sortCache.remove(player)
    }

}
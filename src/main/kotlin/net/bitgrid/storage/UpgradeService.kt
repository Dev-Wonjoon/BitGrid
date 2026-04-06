package net.bitgrid.storage

import net.bitgrid.database.GridUpgrades
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class UpgradeService {

    companion object {
        val STACK_LIMITS = listOf(1024, 4096, 16384, 65536, 131072, 262144)
        val PAGE_LIMITS = listOf(3, 5, 7, 9, 11, 13)
        val MAX_LEVEL = 6
    }

    fun getStackLevel(gridId: String): Int = transaction {
        GridUpgrades.selectAll()
            .where { GridUpgrades.gridId eq gridId }
            .singleOrNull()?.get(GridUpgrades.stackLevel) ?: 0
    }

    fun getPageLevel(gridId: String): Int = transaction {
        GridUpgrades.selectAll()
            .where { GridUpgrades.gridId eq gridId }
            .singleOrNull()?.get(GridUpgrades.pageLevel) ?: 0
    }

    fun getMaxStack(gridId: String): Int = STACK_LIMITS[getStackLevel(gridId)]
    fun getMaxPages(gridId: String): Int = PAGE_LIMITS[getPageLevel(gridId)]

    fun upgradeStack(gridId: String): Boolean = transaction {
        ensureRow(gridId)
        val current = getStackLevel(gridId)
        if(current >= MAX_LEVEL) return@transaction false
        GridUpgrades.update({ GridUpgrades.gridId eq gridId }) {
            it[stackLevel] = current + 1
        }
        true
    }

    fun ensureRow(gridId: String) {
        val exists = GridUpgrades.selectAll()
            .where { GridUpgrades.gridId eq gridId }.count() > 0
        if(!exists) {
            GridUpgrades.insert { it[GridUpgrades.gridId] = gridId }
        }
    }


}
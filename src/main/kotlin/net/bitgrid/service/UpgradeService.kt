package net.bitgrid.service

import net.bitgrid.Bitgrid
import net.bitgrid.config.lang
import net.bitgrid.database.GridUpgrades
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert

class UpgradeService(private val plugin: Bitgrid) {

    private fun getPriceType(path: String) = plugin.config.getString("storage-core.upgrades.$path.price-type") ?: "EXP"
    private fun getCosts(path: String) = plugin.config.getDoubleList("storage-core.upgrades.$path.costs")
    private fun getMaxLevel(path: String) = plugin.config.getInt("storage-core.upgrades.$path.max-level")
    private fun getLimits(path: String) = plugin.config.getIntegerList("storage-core.upgrades.$path.limits")


    // -- 비즈니스 로직 ---
    fun getMaxStack(gridId: String): Int {
        val limits = getLimits("stack")
        val level = getStackLevel(gridId).coerceIn(0, limits.size - 1)
        return limits.getOrNull(level) ?: 1024
    }

    fun getMaxPages(gridId: String): Int {
        val limits = getLimits("page")
        val level = getPageLevel(gridId).coerceIn(0, limits.size - 1)
        return limits.getOrNull(level) ?: 3
    }

    // --- 업그레이드 시도 로직 ---
    fun tryUpgradeStack(player: Player, gridId: String): Boolean {
        val currentLevel = getStackLevel(gridId)
        if(currentLevel >= getMaxLevel("stack")) {
            player.sendMessage(lang("message.upgrade.already_max"))
            return false
        }

        val cost = getCosts("stack").getOrNull(currentLevel) ?: return false
        val type = getPriceType("stack")

        if(consumeCost(player, type, cost)) {
            upgradeStack(gridId)
            player.sendMessage(lang("message.upgrade.success",
                "type" to lang("gui.upgrade.stack_name"),
                "level" to "${currentLevel + 1}"
            ))
            return true
        }

        return false
    }

    fun tryUpgradePage(player: Player, gridId: String): Boolean {
        val currentLevel = getPageLevel(gridId)
        if(currentLevel >= getMaxLevel("page")) {
            player.sendMessage(lang("message.upgrade.already_max"))
            return false
        }

        val cost = getCosts("page").getOrNull(currentLevel) ?: return false
        val type = getPriceType("page")

        if(consumeCost(player, type, cost)) {
            upgradePage(gridId)
            player.sendMessage(lang("message.upgrade.success",
                "type" to lang("gui.upgrade.page_name"),
                "level" to "${currentLevel + 1}"
            ))
            return true
        }

        return false
    }

    // --- 비용 차감 로직 ---
    private fun consumeCost(player: Player, type: String, amount: Double): Boolean {
        return when(type.uppercase()) {
            "EXP" -> {
                val levelAmount = amount.toInt()
                if(player.level >= levelAmount) {
                    player.level -= levelAmount
                    true
                } else {
                    player.sendMessage(lang("message.upgrade.not_enough_exp", "amount" to "$levelAmount"))
                    false
                }
            }
            "VAULT" -> {
                val economy = plugin.economy
                if(economy == null) {
                    player.sendMessage(lang("message.upgrade.no_vault"))
                    return false
                }
                if(economy.has(player, amount)) {
                    economy.withdrawPlayer(player, amount)
                    true
                } else {
                    player.sendMessage(lang("message.upgrade.not_enough_money", "amount" to "$amount"))
                    false
                }
            } else -> false
        }
    }


    // --- 데이터 베이스 처리 ---
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

    private fun upgradeStack(gridId: String) = transaction {
        GridUpgrades.upsert(
            keys = arrayOf(GridUpgrades.gridId),
            onUpdate = {
                it[GridUpgrades.stackLevel] = GridUpgrades.stackLevel + 1
            }
        ) {
            it[GridUpgrades.gridId] = gridId
            it[stackLevel] = 1
            it[pageLevel] = 0
        }
    }

    private fun upgradePage(gridId: String) = transaction {
        ensureRow(gridId)
        val current = getPageLevel(gridId)
        GridUpgrades.update({ GridUpgrades.gridId eq gridId }) {
            it[pageLevel] = current + 1
        }
    }

    fun ensureRow(gridId: String) = transaction {
        GridUpgrades.upsert(
            keys = arrayOf(GridUpgrades.gridId),
            onUpdate = {
                it[GridUpgrades.pageLevel] = GridUpgrades.pageLevel + 1
            }
        ) {
            it[GridUpgrades.gridId] = gridId
            it[stackLevel] = 0
            it[pageLevel] = 1
        }
    }


}
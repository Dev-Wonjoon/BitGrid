package net.bitgrid.storage

import net.bitgrid.database.StorageItems
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.MessageDigest
import java.util.UUID

enum class SortType {
    TIME, AMOUNT, ID
}

class StorageService {

    // ItemStack -> JSON
    private fun serialize(item: ItemStack): String {
        val yaml = YamlConfiguration();
        yaml.set("item", item)
        return yaml.saveToString()
    }

    // JSON -> ItemStack
    private fun deserialize(data: String): ItemStack {
        val yaml = YamlConfiguration()
        yaml.loadFromString(data)
        return yaml.getItemStack("item")!!
    }

    // ItemStack -> SHA-256
    private fun hash(item: ItemStack): String {
        val single = item.clone().apply { amount = 1 }
        val data = serialize(single)
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(data.toByteArray()).joinToString("") {
            "%02x".format(it)
        }
    }

    // 아이템 입고
    fun deposit(gridId: String, item: ItemStack): Boolean {
        val itemHash = hash(item)
        val itemData = serialize(item.clone().apply { amount = 1 })
        val count = item.amount

        return transaction {
            val existing = StorageItems.selectAll()
                .where { (StorageItems.gridId eq  gridId) and
                    (StorageItems.itemHash eq itemHash) }
                .singleOrNull()

            if(existing != null) {
                val newAmount = existing[StorageItems.amount].toLong() + count
                if(newAmount > Int.MAX_VALUE) return@transaction false

                StorageItems.update({
                    (StorageItems.gridId eq gridId) and
                            (StorageItems.itemHash eq itemHash)
                }) {
                    it[amount] = newAmount.toInt()
                }
            } else {
                StorageItems.insert {
                    it[id] = UUID.randomUUID().toString()
                    it[StorageItems.gridId] = gridId
                    it[StorageItems.itemHash] = itemHash
                    it[StorageItems.itemData] = itemData
                    it[amount] = count
                    it[createdAt] = System.currentTimeMillis()
                }
            }
            true
        }
    }

    // 아이템 출고
    fun withdraw(gridId: String, itemHash: String, count: Int): ItemStack? {
        return transaction {
            val row = StorageItems.selectAll()
                .where { (StorageItems.gridId eq gridId) and
                        (StorageItems.itemHash eq itemHash) }
                .singleOrNull() ?: return@transaction null

            val stored = row[StorageItems.amount]
            val actual = count.coerceAtMost(stored)

            if(stored - actual <= 0) {
                StorageItems.deleteWhere {
                    (StorageItems.gridId eq gridId) and
                            (StorageItems.itemHash eq itemHash)
                }
            } else {
                StorageItems.update({
                    (StorageItems.gridId eq gridId) and
                            (StorageItems.itemHash eq itemHash)
                }) {
                    it[amount] = stored - actual
                }
            }
        deserialize(row[StorageItems.itemData]).apply { amount = actual }
        }
    }

    // 플레이어의 전체 아이템 목록 조회
    fun getItems(gridId: String, sort: SortType): List<StoredItem> {
        return transaction {
            val query = StorageItems.selectAll()
                .where { StorageItems.gridId eq gridId }

            val sorted = when (sort) {
                SortType.TIME -> query.orderBy(StorageItems.createdAt to SortOrder.DESC)
                SortType.AMOUNT -> query.orderBy(StorageItems.amount to SortOrder.DESC)
                SortType.ID -> query.orderBy(StorageItems.itemHash to SortOrder.ASC)
            }

            sorted.map { row ->
                StoredItem(
                    itemHash = row[StorageItems.itemHash],
                    item = deserialize(row[StorageItems.itemData]),
                    amount = row[StorageItems.amount]
                )}
        }
    }

    // 검색 (아이템 이름 기준)
    fun searchItems(gridId: String, query: String, sort: SortType = SortType.TIME): List<StoredItem> {
        return getItems(gridId, sort).filter { stored ->
            val name = stored.item.itemMeta?.displayName
                ?: stored.item.type.name.replace("_", " ")
            name.contains(query, ignoreCase = true)
        }
    }
}

data class StoredItem(
    val itemHash: String,
    val item: ItemStack,
    val amount: Int
)
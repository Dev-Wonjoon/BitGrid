package net.bitgrid.service

import net.bitgrid.database.StorageItems
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.Base64
import java.util.UUID

sealed class DepositResult {
    object Success : DepositResult()
    object Overflow : DepositResult()
}

sealed class WithdrawResult {
    data class Success(val item: ItemStack, val actualAmount: Int) : WithdrawResult()
    object NotFound : WithdrawResult()
    object InsufficientStock : WithdrawResult()
}

enum class SortType {
    TIME, AMOUNT, ID
}

open class StorageService {

    // --- 직렬화 (ItemStack -> Base64 String) ---
    open fun serialize(item: ItemStack): String {
        ByteArrayOutputStream().use { outputStream ->
            BukkitObjectOutputStream(outputStream).use { dataOutput ->
                dataOutput.writeObject(item)

                return Base64.getEncoder().encodeToString(outputStream.toByteArray())
            }
        }
    }

    // --- 역직렬화 (Base64 String -> ItemStack) ---
    open fun deserialize(data: String): ItemStack {
        ByteArrayInputStream(Base64.getDecoder().decode(data)).use { inputStream ->
            BukkitObjectInputStream(inputStream).use { dataInput ->
                return dataInput.readObject() as ItemStack
            }
        }
    }
    open fun hash(item: ItemStack): String {
        val clone = item.clone().apply { amount = 1  }
        val serializedData = serialize(clone)

        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(serializedData.toByteArray(Charsets.UTF_8))

        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    // 아이템 입고
    fun deposit(gridId: String, item: ItemStack): Boolean {
        val itemHash = hash(item)
        val itemData = serialize(item.clone().apply { amount = 1 })
        val count = item.amount

        return depositRow(gridId, itemHash, itemData, count) is DepositResult.Success
    }

    fun depositRow(gridId: String, itemHash: String, itemData: String, count: Int): DepositResult {
        require(count > 0) { "count must be positive, was $count" }

        return transaction {
            val currentAmount = StorageItems
                .select(StorageItems.amount)
                .where {
                    (StorageItems.gridId eq gridId) and (StorageItems.itemHash eq itemHash)
                }
                .singleOrNull()?.get(StorageItems.amount) ?: 0

            if(currentAmount.toLong() + count > Int.MAX_VALUE) {
                return@transaction DepositResult.Overflow
            }

            StorageItems.upsert(
                keys = arrayOf(StorageItems.gridId, StorageItems.itemHash),
                onUpdate = {
                    it[StorageItems.amount] = StorageItems.amount + count
                }
            ) {
                it[id] = UUID.randomUUID().toString()
                it[StorageItems.gridId] = gridId
                it[StorageItems.itemHash] = itemHash
                it[StorageItems.itemData] = itemData
                it[amount] = count
                it[createdAt] = System.currentTimeMillis()
            }

            DepositResult.Success
        }
    }

    // 아이템 출고
    fun withdraw(gridId: String, itemHash: String, count: Int): ItemStack? {
        require(count > 0) { "count must be positive, was $count" }

        return transaction {
            val itemData = StorageItems
                .select(StorageItems.itemData, StorageItems.amount)
                .where {
                    (StorageItems.gridId eq gridId) and (StorageItems.itemHash eq itemHash)
                }
                .singleOrNull() ?: return@transaction null

            val stored = itemData[StorageItems.amount]
            val actual = count.coerceAtMost(stored)

            val updated = StorageItems.update({
                (StorageItems.gridId eq gridId) and
                        (StorageItems.itemHash eq itemHash) and
                        (StorageItems.amount greaterEq actual)
            }) {
                with(SqlExpressionBuilder) {
                    it.update(StorageItems.amount, StorageItems.amount - actual)
                }
            }

            if(updated == 0) {
                return@transaction null
            }

            StorageItems.deleteWhere {
                (StorageItems.gridId eq gridId) and
                        (StorageItems.itemHash eq itemHash) and
                        (StorageItems.amount eq 0)
            }

            deserialize(itemData[StorageItems.itemData]).apply { amount = actual }
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
            val displayName = stored.item.itemMeta?.displayName
            val materialName = stored.item.type.name.replace("_", " ")
            val matchTarget = if (!displayName.isNullOrEmpty()) {
                "$displayName $materialName"
            } else {
                materialName
            }
            matchTarget.contains(query, ignoreCase = true)
        }

    }
}

data class StoredItem(
    val itemHash: String,
    val item: ItemStack,
    val amount: Int
)
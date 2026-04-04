package net.bitgrid.database

import org.jetbrains.exposed.sql.Table

object StorageItems : Table("storage_items") {
    val id = varchar("id", 36)
    val gridId = varchar("grid_id", 36).index()
    val itemHash = varchar("item_hash", 64)
    val itemData = text("item_data")
    val amount = integer("amount")
    val createdAt = long("created_at").default(0)

    override  val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(gridId, itemHash)
    }
}
package net.bitgrid.database

import org.jetbrains.exposed.sql.Table

object GridUpgrades : Table("grid_upgrade") {
    val gridId = varchar("grid_id", 36).uniqueIndex()
    val stackLevel = integer("stack_level").default(1)
    val pageLevel = integer("page_level")

    override val primaryKey = PrimaryKey(gridId)
}
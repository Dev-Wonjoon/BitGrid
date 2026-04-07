package net.bitgrid.database

import org.jetbrains.exposed.sql.Table

object GridMembers : Table("grid_members") {
    val id = varchar("id", 36)
    val gridId = varchar("grid_id", 36).index()
    val playerUuid = varchar("player_uuid", 36).uniqueIndex()

    override val primaryKey = PrimaryKey(id)
}
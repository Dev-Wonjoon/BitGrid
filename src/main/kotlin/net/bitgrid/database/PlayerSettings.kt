package net.bitgrid.database

import org.jetbrains.exposed.sql.Table

object PlayerSettings : Table("player_settings") {
    val playerUuid = varchar("player_uuid", 36)
    val sortType = varchar("sort_type", 16).default("TIME")

    override val primaryKey = PrimaryKey(playerUuid)

}
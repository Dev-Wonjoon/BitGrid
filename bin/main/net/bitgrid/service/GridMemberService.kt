package net.bitgrid.service

import net.bitgrid.database.GridMembers
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class GridMemberService {

    fun getGridId(playerUuid: UUID): String {
        return transaction {
            GridMembers.selectAll()
                .where { GridMembers.playerUuid eq playerUuid.toString() }
                .singleOrNull()
                ?.get(GridMembers.gridId)
        } ?: playerUuid.toString()
    }

    fun isMember(gridId: String, playerUuid: UUID): Boolean {
        return transaction {
            !GridMembers.selectAll()
                .where { (GridMembers.gridId eq gridId) and (GridMembers.playerUuid eq playerUuid.toString()) }.empty()
        }
    }

    fun addMember(gridId: String, playerUuid: UUID) {
        transaction {
            val exists = !GridMembers.selectAll()
                .where { (GridMembers.gridId eq gridId) and (GridMembers.playerUuid eq playerUuid.toString()) }.empty()

            if(!exists) {
                GridMembers.insert {
                    it[id] = UUID.randomUUID().toString()
                    it[GridMembers.gridId] = gridId
                    it[GridMembers.playerUuid] = playerUuid.toString()
                }
            }
        }
    }

    fun removeMember(gridId: String, playerUuid: UUID): Boolean {
        return transaction {
            GridMembers.deleteWhere {
                (GridMembers.gridId eq gridId) and (GridMembers.playerUuid eq playerUuid.toString())
            } > 0
        }
    }

    fun getMember(gridId: String): List<UUID> {
        return transaction {
            GridMembers.selectAll().where { GridMembers.gridId eq gridId }.map{ UUID.fromString(it[GridMembers.playerUuid]) }
        }
    }
}
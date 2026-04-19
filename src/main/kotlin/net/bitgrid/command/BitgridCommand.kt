package net.bitgrid.command

import net.R1_21_1.block.StorageCoreBlock
import net.R1_21_1.gui.StorageGui
import net.bitgrid.config.lang
import net.bitgrid.service.GridMemberService
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.UUID

class BitgridCommand(
    private val storageCoreBlock: StorageCoreBlock,
    private val gridMemberService: GridMemberService,
    private val storageGui: StorageGui
) : CommandExecutor, TabCompleter{

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(lang("command.help_open"))
            sender.sendMessage(lang("command.help_give"))
            sender.sendMessage(lang("command.help_invite"))
            sender.sendMessage(lang("command.help_kick"))
            sender.sendMessage(lang("command.help_members"))
            sender.sendMessage(lang("command.help_leave"))

            return true
        }

        when (args[0].lowercase()) {
            "give" -> {
                if (!sender.hasPermission("bitgrid.admin")) {
                    sender.sendMessage(lang("message.no_permission"))
                    return true
                }
                if (args.size < 2) {
                    sender.sendMessage(lang("command.usage_give"))
                    return true
                }
                val target = Bukkit.getPlayer(args[1])
                if (target == null) {
                    sender.sendMessage(lang("command.player_not_found"))
                    return true
                }
                target.inventory.addItem(storageCoreBlock.createCoreItem())
                sender.sendMessage(lang("command.give_sender", "player" to target.name))
                target.sendMessage(lang("command.give_receiver"))
                return true
            }

            "invite" -> {
                if (sender !is Player) return true
                if (args.size < 2) {
                    sender.sendMessage(lang("command.usage_invite"))
                    return true
                }
                val target = Bukkit.getPlayer(args[1])
                if (target == null) {
                    sender.sendMessage(lang("command.player_not_found"))
                    return true
                }

                val gridId = gridMemberService.getGridId(sender.uniqueId)

                // 오직 그리드 소유자(자신의 UUID)만 초대 가능
                if (gridId != sender.uniqueId.toString()) {
                    sender.sendMessage(lang("command.not_owner"))
                    return true
                }

                if (gridMemberService.isMember(gridId, target.uniqueId) || target.uniqueId.toString() == gridId) {
                    sender.sendMessage(lang("command.invite_already"))
                    return true
                }

                gridMemberService.addMember(gridId, target.uniqueId)
                sender.sendMessage(lang("command.invite_success", "player" to target.name))
                target.sendMessage(lang("command.invite_received", "player" to sender.name))
                return true
            }
            "open" -> {
                if(sender !is Player) {
                    sender.sendMessage(lang("message.only_player"))
                    return true
                }

                val gridId = gridMemberService.getGridId(sender.uniqueId)
                storageGui.open(sender, gridId, 0)
                return true
            }

            "kick" -> {
                if (sender !is Player) return true
                if (args.size < 2) {
                    sender.sendMessage(lang("command.usage_kick"))
                    return true
                }
                val targetName = args[1]
                val targetUuid = Bukkit.getPlayer(targetName)?.uniqueId ?: Bukkit.getOfflinePlayer(targetName).uniqueId

                val gridId = gridMemberService.getGridId(sender.uniqueId)
                if (gridId != sender.uniqueId.toString()) {
                    sender.sendMessage(lang("command.not_owner"))
                    return true
                }

                if (!gridMemberService.removeMember(gridId, targetUuid)) {
                    sender.sendMessage(lang("command.kick_not_found", "player" to targetName))
                    return true
                }

                sender.sendMessage(lang("command.kick_success", "player" to targetName))
            }

            "members" -> {
                if (sender !is Player) return true
                val gridId = gridMemberService.getGridId(sender.uniqueId)
                val ownerName = Bukkit.getOfflinePlayer(UUID.fromString(gridId)).name ?: "Unknown"

                val memberList = gridMemberService.getMember((gridId))
                    .mapNotNull { Bukkit.getOfflinePlayer(it).name }

                val allMembers = if (memberList.isEmpty()) ownerName else "$ownerName, ${memberList.joinToString(", ")}"

                sender.sendMessage(lang("command.member_title", "members" to allMembers))
                return true
            }

            "leave" -> {
                if (sender !is Player) return true
                val gridId = gridMemberService.getGridId(sender.uniqueId)

                if (gridId == sender.uniqueId.toString()) {
                    sender.sendMessage(lang("command.leave_success"))
                    return true
                }

                gridMemberService.removeMember(gridId, sender.uniqueId)
                sender.sendMessage(lang("command.leave_success"))
            }

            else -> {
                sender.sendMessage(lang("command.unknown"))
                return true
            }
        }
        return true
    }


    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String?>? {
        return when (args.size) {
            1 -> listOf("give", "invite", "kick", "members", "leave")
                .filter { it.startsWith(args[0].lowercase()) }
            2 -> when (args[0].lowercase()) {
                "give", "invite", "kick" -> Bukkit.getOnlinePlayers()
                    .map { it.name }
                    .filter { it.lowercase().startsWith(args[1].lowercase()) }
                else -> emptyList()
            }
            else -> emptyList()
        }
    }
}
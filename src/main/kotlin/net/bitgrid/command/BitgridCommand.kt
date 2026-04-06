package net.bitgrid.command

import net.R1_20_1.block.StorageCoreBlock
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class BitgridCommand(
    private val storageCoreBlock: StorageCoreBlock
) : CommandExecutor, TabCompleter{

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(args.isEmpty()) {
            sender.sendMessage("§e/bitgrid give <플레이어> §7- Storage Core 지급")
            sender.sendMessage("§e/bitgrid invite <플레이어> §7- 그리드에 멤버 초대")
            sender.sendMessage("§e/bitgrid kick <플레이어> §7- 그리드에서 멤버 제거")
            sender.sendMessage("§e/bitgrid members §7- 그리드 멤버 목록")
            sender.sendMessage("§e/bitgrid leave §7- 그리드 탈퇴")
            return true
        }

        when(args[0].lowercase()) {
            "give" -> {
                if(!sender.hasPermission("bitgrid.admin")) {
                    sender.sendMessage("§c권한이 없습니다.")
                }
                if(args.size < 2) {
                    sender.sendMessage("§c사용법: /gitgrid give <player>")
                    return true
                }
                val target = Bukkit.getPlayer(args[1])
                if(target == null) {
                    sender.sendMessage("§c플레이어를 찾을 수 없습니다.")
                    return true
                }
                target.inventory.addItem(storageCoreBlock.createCoreItem())
                sender.sendMessage("§a${target.name}에게 Storage Core를 지급했습니다.")
                target.sendMessage("§aStorage Core를 지급받았습니다.")
                return true
            }
            "invite" -> {
                // TODO: Task 그리드 멤버 관리에서 구현
                sender.sendMessage("§e준비 중입니다.")
                return true
            }
            "kick" -> {
                sender.sendMessage("§e준비 중입니다.")
                return true
            }
            "members" -> {
                sender.sendMessage("§e준비 중입니다.")
                return true
            }
            "leave" -> {
                sender.sendMessage("§e준비 중입니다.")
                return true
            }
            else -> {
                sender.sendMessage("§c알 수 없는 명령어입니다.")
                return true
            }
        }
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
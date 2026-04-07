package net.bitgrid.command

import net.R1_20_1.block.StorageCoreBlock
import net.bitgrid.config.lang
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
            sender.sendMessage(lang("command.help_give"))
            sender.sendMessage(lang("command.help_invite"))
            sender.sendMessage(lang("command.help_kick"))
            sender.sendMessage(lang("command.help_members"))
            sender.sendMessage(lang("command.help_leave"))
            return true
        }

        when(args[0].lowercase()) {
            "give" -> {
                if(!sender.hasPermission("bitgrid.admin")) {
                    sender.sendMessage(lang("message.no_permission"))
                    return true
                }
                if(args.size < 2) {
                    sender.sendMessage(lang("command.usage_give"))
                    return true
                }
                val target = Bukkit.getPlayer(args[1])
                if(target == null) {
                    sender.sendMessage(lang("command.player_not_found"))
                    return true
                }
                target.inventory.addItem(storageCoreBlock.createCoreItem())
                sender.sendMessage(lang("command.give_sender", "player" to target.name))
                target.sendMessage(lang("command.give_receiver"))
                return true
            }
            "invite" -> {
                // TODO: Task 그리드 멤버 관리에서 구현
                sender.sendMessage(lang("message.not_ready"))
                return true
            }
            "kick" -> {
                sender.sendMessage(lang("message.not_ready"))
                return true
            }
            "members" -> {
                sender.sendMessage(lang("message.not_ready"))
                return true
            }
            "leave" -> {
                sender.sendMessage(lang("message.not_ready"))
                return true
            }
            else -> {
                sender.sendMessage(lang("command.unknown"))
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
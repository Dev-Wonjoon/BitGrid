package net.R1_20_1.listener

import net.R1_20_1.block.StorageCoreBlock
import net.R1_20_1.gui.StorageGui
import net.R1_20_1.gui.UpgradeGui
import net.bitgrid.Bitgrid
import net.bitgrid.config.lang
import net.bitgrid.database.GridMembers
import net.bitgrid.service.StorageService
import net.bitgrid.util.ChatInputUtil
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Barrel
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class StorageCoreListener(
    private val plugin: JavaPlugin,
    private val storageCoreBlock: StorageCoreBlock,
    private val storageGui: StorageGui,
    private val storageService: StorageService,
    private val chatInputUtil: ChatInputUtil
): Listener {

    private val ownerKey = NamespacedKey(plugin, "storage_core_owner")
    private val depositHandler = DepositHandler(storageGui, storageService)
    private val withdrawHandler = WithdrawHandler(storageGui, storageService)
    private val playerUpgradeGuis = mutableMapOf<Player, UpgradeGui>()
    private val navigationHandler = NavigationHandler(storageGui, storageService, chatInputUtil) { player, gridId ->
        val upgradeGui = UpgradeGui(plugin as Bitgrid, gridId)
        upgradeGui.open(player)
        playerUpgradeGuis[player] = upgradeGui
    }


    private fun getGridId(playerUuid: String): String {
        return transaction {
            GridMembers.selectAll()
                .where { GridMembers.playerUuid eq playerUuid }
                .singleOrNull()
                ?.get(GridMembers.gridId)
        } ?: playerUuid
    }

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        val item = event.itemInHand
        if(!storageCoreBlock.isStorageCore(item)) return

        if(!event.player.hasPermission("bitgrid.use")) {
            event.player.sendMessage(lang("message.no_permission"))
            event.isCancelled = true
            return
        }

        val barrel = event.block.state as Barrel
        val container = barrel.persistentDataContainer
        container.set(storageCoreBlock.coreKey, PersistentDataType.BYTE, 1)
        container.set(ownerKey, PersistentDataType.STRING, event.player.uniqueId.toString())
        barrel.update()

        event.player.sendMessage(lang("block.placed"))
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return
        if(event.action != Action.RIGHT_CLICK_BLOCK) return
        if(block.type != Material.BARREL) return

        val barrel = block.state as Barrel
        val container = barrel.persistentDataContainer
        if(!container.has(storageCoreBlock.coreKey, PersistentDataType.BYTE)) return

        // 배럴 열림 방지
        event.isCancelled = true

        if(!event.player.hasPermission("bitgrid.use")) {
            event.player.sendMessage(lang("message.no_permission"))
            return
        }

        val gridId = getGridId(event.player.uniqueId.toString())
        storageGui.open(event.player, gridId)
    }

    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        val block = event.block
        if(block.type != Material.BARREL) return

        val barrel = block.state as Barrel
        val container = barrel.persistentDataContainer
        if(!container.has(storageCoreBlock.coreKey, PersistentDataType.BYTE)) return

        if(!event.player.hasPermission("bitgrid.use")) {
            event.player.sendMessage(lang("message.no_permission"))
            event.isCancelled = true
            return
        }

        event.isDropItems = false
        if(event.player.gameMode != GameMode.CREATIVE) {
            block.world.dropItemNaturally(block.location, storageCoreBlock.createCoreItem())
        }
        event.player.sendMessage(lang("block.recovered"))
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val title = event.view.title


        when {
            title == UpgradeGui.getTitle() -> {
                event.isCancelled = true
                playerUpgradeGuis[player]?.handleClick(event)
            }
        }
        when {
            title.startsWith(StorageGui.TITLE_PREFIX) -> {
                event.isCancelled = true
                val slot = event.rawSlot
                if (slot < 0) return
                val gridId = getGridId(player.uniqueId.toString())
                when {
                    slot >= StorageGui.SIZE -> depositHandler.handle(event, player, gridId)
                    slot < StorageGui.ITEMS_PER_PAGE -> withdrawHandler.handle(event, player, gridId)
                    slot in 45..53 -> navigationHandler.handle(event, player, gridId)
                }
            }
            title == UpgradeGui.getTitle() -> {
                event.isCancelled = true
                playerUpgradeGuis[player]?.handleClick(event)
            }

        }
    }

    @EventHandler
    fun onDrag(event: InventoryDragEvent) {
        val player = event.whoClicked as? Player ?: return
        val title = event.view.title
        if(!title.startsWith(StorageGui.TITLE_PREFIX)) {
            if(event.rawSlots.any { it < StorageGui.SIZE })
                event.isCancelled = true
        } else if(title == UpgradeGui.getTitle()) {
            if(event.rawSlots.any { it < UpgradeGui.SIZE })
                event.isCancelled = true
        }
    }

    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val title = event.view.title

        if(title == UpgradeGui.getTitle()) {
            playerUpgradeGuis.remove(player)
            return
        }

        if(!title.startsWith(StorageGui.TITLE_PREFIX))

        if(chatInputUtil.isOpen((player))) return
        val cursor = event.view.cursor
        if(cursor != null && !cursor.type.isAir) {
            player.inventory.addItem(cursor)
            event.view.cursor = null
        }

        storageGui.cleanup(player)
    }
}
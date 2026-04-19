package net.R1_21_1.gui

import net.bitgrid.Bitgrid
import net.bitgrid.config.lang
import net.bitgrid.service.UpgradeService
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class UpgradeGui(private val plugin: Bitgrid, private val gridId: String) {
    companion object {
        const val SIZE = 27
        fun getTitle(): String = lang("gui.upgrade.title")
    }

    private val upgradeService = UpgradeService(plugin)
    private lateinit var inventory: Inventory

    fun open(player: Player) {
        inventory = org.bukkit.Bukkit.createInventory(null, SIZE, getTitle())

        // 아이템 배치를 refreshGui()에 몰아넣고, 처음 열 때 한 번 호출해 줍니다.
        refreshGui()

        player.openInventory(inventory)
    }

    private fun createUpgradeItem(name: String, currentLevel: Int, type: String): ItemStack {
        val item = ItemStack(Material.ENCHANTED_BOOK)
        val meta = item.itemMeta!!

        meta.setDisplayName("§b$name")

        // UpgradeService에서 최대 레벨, 비용 종류, 필요 비용 리스트를 가져옵니다.
        val maxLevel = upgradeService.getMaxLevel(type)
        val priceType = upgradeService.getPriceType(type)
        val costs = upgradeService.getCosts(type)
        val nextCost = costs.getOrNull(currentLevel)

        val lore = mutableListOf<String>()

        lore.add(lang("gui.upgrade.current_level",
            "current" to "$currentLevel", "max" to "$maxLevel"
        ))
        lore.add("")

        // 아직 만렙이 아니고, 다음 레벨업 비용이 설정되어 있다면
        if (currentLevel < maxLevel && nextCost != null) {
            val typeName = if (priceType.uppercase() == "EXP") lang("gui.upgrade.price_type_exp") else lang("gui.upgrade.price_type_vault")

            lore.add(lang("gui.upgrade.next_cost",
                "cost" to nextCost.toInt().toString(),
                "type" to typeName
            ))
            lore.add(lang("gui.upgrade.click_to_upgrade"))
        } else {
            lore.add(lang("gui.upgrade.max_level_reached"))
        }

        meta.lore = lore
        item.itemMeta = meta
        return item
    }

    fun handleClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val clickedItem = event.currentItem ?: return // 빈 공간 클릭 무시

        event.isCancelled = true // 아이템을 인벤토리로 가져가는 것 방지

        when (event.rawSlot) {
            10 -> handleStackUpgradeClick(player)
            16 -> handlePageUpgradeClick(player)
        }
    }

    private fun handleStackUpgradeClick(player: Player) {
        val success = upgradeService.tryUpgradeStack(player, gridId)
        if(success) {
            player.playSound(player.location, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
            refreshGui() // 성공 시 바로 화면 아이템 갱신
        } else {
            player.playSound(player.location, org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f)
        }
    }

    private fun handlePageUpgradeClick(player: Player) {
        val success = upgradeService.tryUpgradePage(player, gridId)
        if (success) {
            player.playSound(player.location, org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f)
            refreshGui() // 성공 시 바로 화면 아이템 갱신
        } else {
            player.playSound(player.location, org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f)
        }
    }

    private fun refreshGui() {
        // 인벤토리가 아직 안 만들어졌다면 실행하지 않음 (안전 장치)
        if (!this::inventory.isInitialized) return

        val stackLevel = upgradeService.getStackLevel(gridId)
        val pageLevel = upgradeService.getPageLevel(gridId)

        // 최신 레벨로 아이템을 다시 생성해서 슬롯에 덮어씌우기!
        val stackItem = createUpgradeItem(lang("gui.upgrade.stack_name"), stackLevel, "stack")
        inventory.setItem(10, stackItem)

        val pageItem = createUpgradeItem(lang("gui.upgrade.page_name"), pageLevel, "page")
        inventory.setItem(16, pageItem)
    }
}
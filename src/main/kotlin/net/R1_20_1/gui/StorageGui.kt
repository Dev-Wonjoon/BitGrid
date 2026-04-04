package net.R1_20_1.gui

import net.bitgrid.storage.StorageService
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class StorageGui(
    private val storageService: StorageService
) {
    companion object {
        const val ROWS = 6
        const val SIZE = ROWS * 9
        const val ITEMS_PER_PAGE = 45
        const val TITLE_PREFIX = "8Bit Grid"
    }

    val sortManager = SortManager()
    private val navBarBuilder = NavigationBarBuilder(sortManager)
    private val playerPages = mutableMapOf<Player, Int>()
    private val playerSearches = mutableMapOf<Player, String>()

    fun open(player: Player, gridId: String, page: Int = 0) {
        val sort = sortManager.getSort(player)
        val query = playerSearches[player]
        val items = if (query != null) {
            storageService.searchItems(gridId, query, sort)
        } else {
            storageService.getItems(gridId, sort)
        }
        val maxPage = maxOf(0, (items.size - 1) / ITEMS_PER_PAGE)
        val currentPage = page.coerceIn(0, maxPage)

        val title = if (query != null) {
            "$TITLE_PREFIX §7- §f$query §7(${currentPage + 1}/${maxPage + 1})"
        } else {
            "$TITLE_PREFIX §7(${currentPage + 1}/${maxPage + 1})"
        }

        val inventory = Bukkit.createInventory(null, SIZE, title)

        val startIndex = currentPage * ITEMS_PER_PAGE
        val pageItems = items.drop(startIndex).take(ITEMS_PER_PAGE)

        for ((i, storedItem) in pageItems.withIndex()) {
            inventory.setItem(i, GuiItemFactory.createDisplayItem(storedItem))
        }

        navBarBuilder.fill(inventory, currentPage, maxPage, player)
        player.openInventory(inventory)

        playerPages[player] = currentPage
    }

    fun getPage(player: Player): Int = playerPages[player] ?: 0

    fun setSearch(player: Player, query: String?) {
        if(query.isNullOrBlank()) {
            playerSearches.remove(player)
        } else {
            playerSearches[player] = query
        }
    }

    fun getSearch(player: Player): String? = playerSearches[player]

    fun cleanup(player: Player) {
        playerPages.remove(player)
        playerSearches.remove(player)
        sortManager.cleanup(player)
    }
}
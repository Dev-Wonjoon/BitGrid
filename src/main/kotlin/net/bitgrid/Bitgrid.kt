package net.bitgrid

import net.R1_20_1.block.StorageCoreBlock
import net.R1_20_1.gui.StorageGui
import net.R1_20_1.listener.StorageCoreListenerV2
import net.R1_20_1.recipe.StorageCoreRecipe
import net.bitgrid.command.BitgridCommand
import net.bitgrid.config.DatabaseConfig
import net.bitgrid.config.LanguageManager
import net.bitgrid.database.DatabaseManager
import net.bitgrid.service.GridMemberService
import net.bitgrid.service.StorageService
import net.bitgrid.util.ChatInputUtil
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.java.JavaPlugin

class Bitgrid : JavaPlugin() {

    private lateinit var databaseManager: DatabaseManager
    lateinit var languageManager: LanguageManager

    public var economy: Economy? = null

    override fun onEnable() {
        // 설정 로드
        saveDefaultConfig()

        languageManager = LanguageManager(this)
        languageManager.load()
        if(!setupEconomy()) {
            logger.warning("Vault not found. Economy features will be limited.")
        }
        val dbConfig = DatabaseConfig.fromConfig(config)

        // DB 연결
        databaseManager = DatabaseManager(dbConfig, dataFolder)
        databaseManager.connect()

        // 컴포넌트 초기화
        val storageService = StorageService()
        val storageCoreBlock = StorageCoreBlock(this)
        val storageGui = StorageGui(storageService)
        val chatInputUtil = ChatInputUtil(this)
        val gridMemberService = GridMemberService()
        chatInputUtil.init()

        server.pluginManager.registerEvents(
            net.R1_20_1.listener.StorageCoreListenerV2(
                this,
                storageCoreBlock,
                storageGui,
                storageService,
                chatInputUtil,
                gridMemberService
            ), this
        )

        //커맨드 등록
        val bitgridCommand = BitgridCommand(storageCoreBlock, gridMemberService)
        getCommand("bitgrid")?.setExecutor(bitgridCommand)
        getCommand("bitgrid")?.tabCompleter = bitgridCommand


        StorageCoreRecipe(this, storageCoreBlock).register(config)

        logger.info("Enabled BitGrid.")

    }

    override fun onDisable() {
        databaseManager.disconnect()
        logger.info("disabled bitgrid.")
    }

    private fun setupEconomy(): Boolean {
        val vault = server.pluginManager.getPlugin("Vault")
        if(vault == null) {
            logger.severe("Vault plugin not found! Economy features will be limited.")
            return false
        }

        val rsp = server.servicesManager.getRegistration(net.milkbowl.vault.economy.Economy::class.java) ?: return false

        economy = rsp.provider
        logger.info("Vault economy successfully hooked: ${economy?.name}")
        return true
    }
}

package net.bitgrid

import net.R1_20_1.block.StorageCoreBlock
import net.R1_20_1.gui.StorageGui
import net.R1_20_1.listener.StorageCoreListener
import net.R1_20_1.recipe.StorageCoreRecipe
import net.bitgrid.command.BitgridCommand
import net.bitgrid.config.DatabaseConfig
import net.bitgrid.database.DatabaseManager
import net.bitgrid.storage.StorageService
import net.bitgrid.util.SignInputUtil
import org.bukkit.plugin.java.JavaPlugin

class Bitgrid : JavaPlugin() {

    private lateinit var databaseManager: DatabaseManager

    override fun onEnable() {
        // 설정 로드
        saveDefaultConfig()
        val dbConfig = DatabaseConfig.fromConfig(config)

        // DB 연결
        databaseManager = DatabaseManager(dbConfig, dataFolder)
        databaseManager.connect()

        // 컴포넌트 초기화
        val storageService = StorageService()
        val storageCoreBlock = StorageCoreBlock(this)
        val storageGui = StorageGui(storageService)
        val signInputUtil = SignInputUtil(this)
        signInputUtil.init()

        server.pluginManager.registerEvents(StorageCoreListener(this, storageCoreBlock, storageGui, storageService, signInputUtil), this)

        //커맨드 등록
        val bitgridCommand = BitgridCommand(storageCoreBlock)
        getCommand("bitgrid")?.setExecutor(bitgridCommand)
        getCommand("bitgrid")?.tabCompleter = bitgridCommand

        StorageCoreRecipe(this, storageCoreBlock).register(config)

        logger.info("Enabled BitGrid.")

    }

    override fun onDisable() {
        databaseManager.disconnect()
        logger.info("disabled bitgrid.")
    }
}

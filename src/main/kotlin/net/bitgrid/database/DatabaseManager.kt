package net.bitgrid.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.bitgrid.config.DatabaseConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File


class DatabaseManager(private val config: DatabaseConfig, private val dataFolder: File) {
    private lateinit var dataSource: HikariDataSource

    fun connect() {
        val hikariConfig = HikariConfig().apply {
            poolName = "BitGridHikariPool"

            when (config.type) {
                "sqlite" -> {
                    jdbcUrl = "jdbc:sqlite:${File(dataFolder, "bitgrid.db").absoluteFile}" +
                            "?journal_mode=WAL&synchronous=NORMAL&cache_size=-20000&foreign_keys=true&busy_timeout=5000"
                    driverClassName = "org.sqlite.JDBC"
                    maximumPoolSize = 1
                }
                "mysql" -> {
                    jdbcUrl = "jdbc:mysql://${config.host}:${config.port}/${config.name}"
                    driverClassName = "com.mysql.cj.jdbc.Driver"
                    username = config.username
                    password = config.password

                    maximumPoolSize = 10
                    minimumIdle = 10
                    maxLifetime = 1_800_000
                    keepaliveTime = 0
                    connectionTimeout = 5_000

                    applyMySqlDataSourceProperties(this)
                }
                "postgresql" -> {
                    jdbcUrl = "jdbc:postgresql://${config.host}:${config.port}/${config.name}"
                    driverClassName = "org.postgresql.Driver"
                    username = config.username
                    password = config.password

                    maximumPoolSize = 10
                    minimumIdle = 10
                    maxLifetime = 1_800_000
                    connectionTimeout = 5_000

                    addDataSourceProperty("prepareThreshold", "3")
                }
                else -> error("Unsupported database type: ${config.type}")
            }
        }

        dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(StorageItems, GridMembers, PlayerSettings, GridUpgrades)
        }
    }

    private fun applyMySqlDataSourceProperties(config: HikariConfig) {
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "100");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("useLocalTransactionState", "true")

        config.addDataSourceProperty("rewriteBatchedStatements", "true")

        config.addDataSourceProperty("cacheResultSetMetadata", "true")
        config.addDataSourceProperty("cacheServerConfiguration", "true")

        config.addDataSourceProperty("elideSetAutoCommits", "true")
        config.addDataSourceProperty("maintainTimeStats", "false")
    }

    fun disconnect() {
        if(::dataSource.isInitialized) {
            dataSource.close()
        }
    }


}
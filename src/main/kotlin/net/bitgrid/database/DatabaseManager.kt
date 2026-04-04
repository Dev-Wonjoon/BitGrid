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
            when (config.type) {
                "sqlite" -> {
                    jdbcUrl = "jdbc:sqlite:${File(dataFolder, "bitgrid.db").absoluteFile}"
                    driverClassName = "org.sqlite.JDBC"
                    maximumPoolSize = 1
                }
                "mysql" -> {
                    jdbcUrl = "jdbc:mysql://${config.host}:${config.port}/${config.name}"
                    driverClassName = "com.mysql.cj.jdbc.Driver"
                    username = config.username
                    password = config.password
                    maximumPoolSize = 10
                }
                "postgresql" -> {
                    jdbcUrl = "jdbc:postgresql://${config.host}:${config.port}/${config.name}"
                    driverClassName = "com.postgresql.Driver"
                    username = config.username
                    password = config.password
                    maximumPoolSize = 10
                }
                else -> error("Unsupported database type: ${config.type}")
            }
        }

        dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(StorageItems, GridMembers, PlayerSettings)
        }
    }
    fun disconnect() {
        if(::dataSource.isInitialized) {
            dataSource.close()
        }
    }


}
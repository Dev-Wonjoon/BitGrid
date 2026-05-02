import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.bitgrid.database.StorageItems
import net.bitgrid.service.StorageService
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StorageServiceLoadTest {
    @BeforeAll
    fun setup() {
        val dbFile = File("test_load.db")
        if(dbFile.exists()) {
            dbFile.delete()
        }

        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:sqlite:test_load.db"
            driverClassName = "org.sqlite.JDBC"
            maximumPoolSize = 1
            connectionTimeout = 5000
        }
        val dataSource = HikariDataSource(config)

        Database.connect(dataSource)
        transaction {
            SchemaUtils.create(StorageItems)
        }


    }

    @Test
    fun `동시 deposit 부하 테스트`() = runBlocking {
        val service = StorageService()
        val gridId = "test-grid"
        val iteration = 1000

        val elapsed = measureTimeMillis {
            val jobs = List(iteration) { i ->
                launch(Dispatchers.IO) {
                    service.depositRow(gridId, "hash-${i % 50}", "dummyData", 1)
                }
            }
            jobs.joinAll()
        }
        val totalAmount = transaction {
            StorageItems.selectAll().sumOf { it[StorageItems.amount] }
        }
        assertEquals(1000 as Any, totalAmount, "동시성 테스트: 동시 접근 시에도 아이템이 유실되지 않아야 합니다.")
    }
}
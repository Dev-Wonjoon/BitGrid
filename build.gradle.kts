plugins {
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.serialization") version "2.3.20"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "net"
version = "0.3-ALPHA"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "spigotmc-repo"
    }
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit", module = "bukkit")
    }

    // DB 드라이버
    implementation("com.mysql:mysql-connector-j:8.3.0")
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("org.xerial:sqlite-jdbc:3.47.1.0")

    // Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:0.56.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.56.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.56.0")

    // 버킷 테스트
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.108.0")

    // HikariCP 커넥션 풀
    implementation("com.zaxxer:HikariCP:6.2.1")

    // JSON 직렬화
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // 비동기 처리
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    compileOnly("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.22.0")
    compileOnly("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.22.0")
}

tasks {
    runServer {
        minecraftVersion("1.21")
    }
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    archiveClassifier.set("")

    // 1. Relocate 시 겹치는 파일 정리
    mergeServiceFiles()

    // 2. 라이브러리 리로케이션 (충돌 방지)
    relocate("org.jetbrains.exposed", "net.bitgrid.libs.exposed")
    relocate("com.zaxxer.hikari", "net.bitgrid.libs.hikari")
    relocate("org.jetbrains.kotlinx.serialization", "net.bitgrid.libs.serialization")

    // 3. SQLite의 거대한 네이티브 라이브러리들 중 안 쓰는 OS 제거
    exclude("org/xerial/snappy/native/**")

    // 4. Exposed ORM에서 사용하지 않는 타 DB 지원 기능 제거
    exclude("org/jetbrains/exposed/sql/vendors/h2/**")
    exclude("org/jetbrains/exposed/sql/vendors/oracle/**")
    exclude("org/jetbrains/exposed/sql/vendors/sqlserver/**")
    exclude("org/jetbrains/exposed/sql/vendors/mariadb/**")

    // 5. 기타 불필요한 메타데이터 제거
    exclude("META-INF/maven/**")
    exclude("META-INF/versions/**")
    exclude("about.html")
    exclude("jetbrains/**")

    minimize {
        // JDBC 드라이버는 문자열로 로드되므로 삭제 방지를 위해 제외 설정 유지
        exclude(dependency("org.xerial:sqlite-jdbc:.*"))
        exclude(dependency("com.mysql:mysql-connector-j:.*"))
        exclude(dependency("org.postgresql:postgresql:.*"))
        
        // Exposed 패키지들은 서비스 로더와 리플렉션을 많이 사용하므로 전체 보존 권장
        exclude(dependency("org.jetbrains.exposed:.*"))
    }
}

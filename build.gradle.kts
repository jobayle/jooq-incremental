import org.jooq.codegen.gradle.CodegenTask
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.jdbc.JdbcDatabaseDelegate

plugins {
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.jooq)
    alias(libs.plugins.liquibase)
}

group = "org.example"
version = "1.0-SNAPSHOT"

// Properties to invoke liquibase on any database
// eg: `./gradlew update -Pdb.db01.url=jdbc:postgresql://localhost:5432/db01 ...`
val dbUsername = properties["db.username"] as? String ?: "db_user"
val dbPassword = properties["db.password"] as? String ?: "db_pass"
val db01Url    = properties["db.db01.url"] as? String
val db02Url    = properties["db.db02.url"] as? String

repositories {
    mavenCentral()
}

kotlin.jvmToolchain(17)

dependencies {
    implementation(libs.jooq.core)
    implementation(libs.jooq.kotlin)
    implementation(libs.jooq.jackson)

    // Build
    liquibaseRuntime(libs.liquibase.core)
    liquibaseRuntime(libs.postgresql)
    liquibaseRuntime(libs.liquibase.picoli)
    jooqCodegen(libs.postgresql)
    jooqCodegen(libs.jooq.meta)

    // Test
    testImplementation(kotlin("test"))
}

buildscript {
    dependencies {
        classpath(libs.testContainers.core)
        classpath(libs.testContainers.postgresql)
        classpath(libs.postgresql)
    }
}

abstract class Containers : BuildService<Containers.Params>, AutoCloseable {

    interface Params : BuildServiceParameters {
        fun getUsername(): Property<String>
        fun getPassword(): Property<String>
    }

    val instance: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:14-alpine")
        .withUsername(parameters.getUsername().get())
        .withPassword(parameters.getPassword().get())

    init {
        println("Starting containers...")
        instance.start()
        JdbcDatabaseDelegate(instance, "").use {
            it.execute("CREATE DATABASE db01;", null, 0, false, false)
            it.execute("CREATE DATABASE db02;", null, 0, false, false)
        }
    }

    override fun close() {
        println("Stopping containers...")
        instance.close()
    }
}

val containerProvider = project.gradle.sharedServices
    .registerIfAbsent("containers", Containers::class.java) {
        parameters.getUsername().set(dbUsername)
        parameters.getPassword().set(dbPassword)
    }

tasks.register("configureTestContainers", DefaultTask::class) {
    group = "build"
    description = "Configure Liquibase & JOOQ to run on Test Containers"
    usesService(containerProvider)
    @Suppress("UNCHECKED_CAST")
    doFirst {
        var url = containerProvider.get().instance.jdbcUrl
        println("url = $url")
        // Prune URL to remove dbname
        url = url.substring(0, url.lastIndexOf('/') + 1)
        println("Pruned url = $url")

        // Configure Liquibase
        val argumentsDb01 = project.liquibase.activities["db01"].arguments as MutableMap<String, String>
        argumentsDb01["url"] = url + "db01"
        val argumentsDb02 = project.liquibase.activities["db02"].arguments as MutableMap<String, String>
        argumentsDb02["url"] = url + "db02"

        // Configure JOOQ
        project.jooq.executions.named("db01").get().configuration.jdbc.url = url + "db01"
        project.jooq.executions.named("db02").get().configuration.jdbc.url = url + "db02"
    }
}

tasks.withType<CodegenTask>().configureEach {
    // Rerun only if liquibase changesets are modified (not working... why?)
    // It seems something cleans dir build/generated-sources/jooq ...
    inputs.files(fileTree("src/main/resources/liquibase"))
}

liquibase {
    activities {
        register("db01") {
            arguments = mapOf(
                "changelogFile" to "src/main/resources/liquibase/db01/init.json",
                "username" to dbUsername,
                "password" to dbPassword,
                "url" to db01Url,
                "driver" to "org.postgresql.Driver"
            )
        }
        register("db02") {
            arguments = mapOf(
                "changelogFile" to "src/main/resources/liquibase/db02/init.json",
                "username" to dbUsername,
                "password" to dbPassword,
                "url" to db02Url,
                "driver" to "org.postgresql.Driver"
            )
        }
    }
}

jooq {
    executions {
        create("db01") {
            configuration {
                jdbc {
                    username = dbUsername
                    password = dbPassword
                    url = "lateinit"
                }
                generator {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        excludes = "databasechangelog|databasechangeloglock"
                    }
                    generate {
                        isRelations = true
                        isRoutines = true
                    }
                    target {
                        packageName = "gh.db01.db"
                    }
                }
            }
        }
        create("db02") {
            configuration {
                jdbc {
                    username = dbUsername
                    password = dbPassword
                    url = "lateinit"
                }
                generator {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        excludes = "databasechangelog|databasechangeloglock"
                    }
                    generate {
                        isRelations = true
                        isRoutines = true
                    }
                    target {
                        packageName = "gh.db02.db"
                    }
                }
            }
        }
    }
}

tasks.jooqCodegen {
    dependsOn(tasks.update, tasks.named("configureTestContainers"))
    mustRunAfter(tasks.update, tasks.named("configureTestContainers"))
}

tasks.compileKotlin {
    dependsOn(tasks.jooqCodegen)
}

tasks.test {
    useJUnitPlatform()
}

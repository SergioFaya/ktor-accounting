package nomad.digital.infrastructure.storage.exposed.table

import org.jetbrains.exposed.dao.id.LongIdTable

object AccountTable : LongIdTable("ACCOUNT") {
    val title = varchar("title", 2048)
}
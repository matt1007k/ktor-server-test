package example.com.products.data.model

import example.com.users.data.domain.models.enumColumnCustom
import example.com.users.domain.models.Status
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.jodatime.CurrentDate
import org.jetbrains.exposed.sql.jodatime.date
import org.jetbrains.exposed.sql.jodatime.datetime
import org.joda.time.DateTime

object ProductsTable : UUIDTable("products") {
    val name = varchar("name", 100)
    val description = varchar("description", 100)
    val password = varchar("password", 100)
    val imageUrl = varchar("imageUrl", 100).nullable()

    val documentNumber = varchar("documentNumber", 11)

    val status = enumColumnCustom<Status>(
        column = "status",
        enumTypeDb = "Status",
        defaultValue = Status.ACTIVE
    )
    val createdAt = datetime("createdAt").default(DateTime.now())
    val updatedAt = datetime("updatedAt").default(DateTime.now())
// https://www.youtube.com/watch?v=YOXWnM_8vz8
//    val arrivalDate = date("arrival_date").defaultExpression(CurrentDate)
}

//fun toUserModel(row: ResultRow) = Product(
//    id = row[ProductsTable.id].toString(),
//    name = row[ProductsTable.name],
//    description = row[ProductsTable.description],
//    imageUrl = row[ProductsTable.imageUrl],
//    providerId = row[ProductsTable.providerId],
//    documentNumber = row[ProductsTable.documentNumber],
//    role = row[ProductsTable.role],
//    status = row[ProductsTable.status],
//    createdAt = row[ProductsTable.createdAt].toString(),
//    updatedAt = row[ProductsTable.updatedAt].toString(),
//    arrivalDate = row[ProductsTable.arrivalDate].toString(),
//)
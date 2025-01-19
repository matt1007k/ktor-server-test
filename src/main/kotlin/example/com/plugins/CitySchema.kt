package example.com.plugins

import example.com.core.data.models.suspendTransaction
import example.com.core.domain.PaginatedResult
import example.com.core.domain.Metadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

object Cities : Table() {
    val id = uuid("id").autoGenerate().entityId()
    val name = varchar("name", 50)
    val population = integer("population")
}

@Serializable
data class CreateCityRequest(val name: String, val population: Int)

@Serializable
data class City(val id: String, val name: String, val population: Int)

enum class Priority {
    Low, Medium, High, Vital
}

@Serializable
data class Task(
    val name: String,
    val description: String,
    val priority: Priority
)
fun toTaskModel(it: ResultRow) = Task(
    name = it[TaskTable.name],
    description = it[TaskTable.description],
    priority = Priority.valueOf(it[TaskTable.priority])
)

object TaskTable : IntIdTable("task") {
    val name = varchar("name", 50)
    val description = varchar("description", 50)
    val priority = varchar("priority", 50)
}

class TaskDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TaskDAO>(TaskTable)

    var name by TaskTable.name
    var description by TaskTable.description
    var priority by TaskTable.priority
}

//object Countries : UUIDTable("country", "uuid") {
//    val name = varchar("name", 255)
//    val code = varchar("code", 2)
//    override val primaryKey = PrimaryKey(uuid, name = "country_pk")
//}
//
//object Foo : Table() {
//    val uuid = uuid("my_column").defaultExpression(CustomFunction("gen_random_uuid()", UUIDColumnType()))
//}


fun daoToModel(dao: TaskDAO) = Task(
    dao.name,
    dao.description,
    Priority.valueOf(dao.priority)
)

class CityService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_CITIES =
            "CREATE TABLE CITIES (ID SERIAL PRIMARY KEY, NAME VARCHAR(255), POPULATION INT);"
        private const val SELECT_CITY_BY_ID = "SELECT name, population FROM cities WHERE id = ?"
        private const val INSERT_CITY = "INSERT INTO cities (name, population) VALUES (?, ?)"
        private const val UPDATE_CITY = "UPDATE cities SET name = ?, population = ? WHERE id = ?"
        private const val DELETE_CITY = "DELETE FROM cities WHERE id = ?"

    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_CITIES)
    }

    private var newCityId = 0

    suspend fun allTasks(page: Int, perPage: Int, task: Task): PaginatedResult<Task> =
        suspendTransaction {
            val offset = (page - 1) * perPage
            val tasks = TaskTable.select { TaskTable.priority eq task.priority.toString() }
                .limit(perPage, offset = offset.toLong())
                .map(::toTaskModel)

            val total = TaskTable.selectAll()
                .count()
            val lastPage = if (perPage > 0) {
                Math.ceil(total.toDouble() / perPage).toInt()
            } else {
                1
            }

            PaginatedResult(
                data = tasks,
                metadata = Metadata(total = total.toInt(), lastPage = lastPage, perPage = perPage)
            )
        }

    suspend fun tasksByPriority(priority: Priority, term: String?): List<Task> = suspendTransaction {
        TaskDAO
            .find { (TaskTable.priority eq priority.toString()) and (TaskTable.name like "%$term%")}
            .map(::daoToModel)

    }

    suspend fun createTask(task: Task): Task = suspendTransaction {
        TaskDAO.new {
            name = task.name
            description = task.description
            priority = task.priority.toString()
        }.let(::daoToModel)
    }

    fun getAll(): ArrayList<City> {
        val cities: ArrayList<City> = arrayListOf()
        transaction {
            Cities.selectAll().map {
                cities.add(
                    City(
                        id = it[Cities.id].toString(),
                        name = it[Cities.name],
                        population = it[Cities.population]
                    )
                )
            }
            addLogger(StdOutSqlLogger)
        }

        return cities
    }

    // Create new city
    suspend fun create(city: CreateCityRequest): Int = withContext(Dispatchers.IO) {

        val cityId = transaction {
            Cities.insert {
                it[name] = city.name
                it[population] = city.population
            }
        }

        print("cityId: $cityId")

        val statement = connection.prepareStatement(INSERT_CITY, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, city.name)
        statement.setInt(2, city.population)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted city")
        }
    }

    // Read a city
    suspend fun read(id: String): City? = withContext(Dispatchers.IO) {
        val query = "SELECT * FROM cities WHERE id = ?"
        connection.prepareStatement(query).use { statement ->
            statement.setString(1, id)
            val resultSet = statement.executeQuery()
            return@withContext if (resultSet.next()) {
                City(
                    resultSet.getString("id"),
                    resultSet.getString("name"),
                    resultSet.getInt("population")
                )
            } else null
        }
    }

    // Update a city
    suspend fun update(id: String, city: City): Boolean = withContext(Dispatchers.IO) {
        val query = "UPDATE cities SET name = ?, population = ? WHERE id = ?"
        connection.prepareStatement(query).use { statement ->
            statement.setString(1, city.name)
            statement.setInt(2, city.population)
            statement.setString(3, id)
            return@withContext statement.executeUpdate() > 0
        }

    }

    // Delete a city
    suspend fun delete(id: String): Boolean = withContext(Dispatchers.IO) {
        connection.prepareStatement(DELETE_CITY).use { statement ->
            statement.setString(1, id)
            return@withContext statement.executeUpdate() > 0
        }
    }
}


class CityController(private val connection: Connection) {

    fun getAllCities(page: Int, perPage: Int): PaginatedResult<City> {
        val offset = (page - 1) * perPage
        val cities = mutableListOf<City>()

        // Query to get paginated cities
        val query = "SELECT * FROM cities LIMIT $perPage OFFSET $offset"
        val countQuery = "SELECT COUNT(*) AS total FROM cities"

        var total = 0
        connection.createStatement().use { countStatement ->
            val resultSet: ResultSet = countStatement.executeQuery(countQuery)
            if (resultSet.next()) {
                total = resultSet.getInt("total")
            }
        }

        connection.createStatement().use { statement ->
            val resultSet: ResultSet = statement.executeQuery(query)
            while (resultSet.next()) {
                cities.add(
                    City(
                        resultSet.getString("id"),
                        resultSet.getString("name"),
                        resultSet.getInt("population")
                    )
                )
            }
        }

        val lastPage = if (perPage > 0) {
            Math.ceil(total.toDouble() / perPage).toInt()
        } else {
            1
        }

        return PaginatedResult(
            data = cities,
            metadata = Metadata(total = total, lastPage = lastPage, perPage = perPage)
        )
    }
}
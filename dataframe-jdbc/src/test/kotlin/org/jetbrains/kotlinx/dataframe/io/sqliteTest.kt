package org.jetbrains.kotlinx.dataframe.io

import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import org.jetbrains.kotlinx.dataframe.api.cast
import org.jetbrains.kotlinx.dataframe.api.filter
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import org.junit.Ignore

private const val DATABASE_URL = "jdbc:sqlite:"

@DataSchema
interface CustomerSQLite {
    val id: Int
    val name: String
    val age: Int
    val salary: Double
    val profilePicture: ByteArray
}

@DataSchema
interface OrderSQLite {
    val id: Int
    val customerName: String
    val orderDate: String
    val totalAmount: Double
    val orderDetails: ByteArray
}

@DataSchema
interface CustomerOrderSQLite {
    val customerId: Int
    val customerName: String
    val customerAge: Int
    val customerSalary: Double
    val customerProfilePicture: ByteArray
    val orderId: Int
    val orderDate: String
    val totalAmount: Double
    val orderDetails: ByteArray
}

@Ignore
class SqliteTest {
    companion object {
        private lateinit var connection: Connection

        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            connection = DriverManager.getConnection(DATABASE_URL)

            @Language("SQL")
            val createCustomersTableQuery = """
            CREATE TABLE Customers (
                id INTEGER AUTO_INCREMENT PRIMARY KEY,
                name TEXT,
                age INTEGER,
                salary REAL,
                profilePicture BLOB
            )
            """

            connection.createStatement().execute(
                createCustomersTableQuery
            )

            @Language("SQL")
            val createOrderTableQuery = """
            CREATE TABLE Orders (
                id INTEGER AUTO_INCREMENT PRIMARY KEY,
                customerName TEXT,
                orderDate TEXT,
                totalAmount NUMERIC,
                orderDetails BLOB
            )
            """

            connection.createStatement().execute(
                createOrderTableQuery
            )

            val profilePicture = "SampleProfilePictureData".toByteArray()
            val orderDetails = "OrderDetailsData".toByteArray()

            connection.prepareStatement("INSERT INTO Customers (name, age, salary, profilePicture) VALUES (?, ?, ?, ?)")
                .use {
                    it.setString(1, "John Doe")
                    it.setInt(2, 30)
                    it.setDouble(3, 2500.50)
                    it.setBytes(4, profilePicture)
                    it.executeUpdate()
                }

            connection.prepareStatement("INSERT INTO Customers (name, age, salary, profilePicture) VALUES (?, ?, ?, ?)")
                .use {
                    it.setString(1, "Max Joint")
                    it.setInt(2, 40)
                    it.setDouble(3, 1500.50)
                    it.setBytes(4, profilePicture)
                    it.executeUpdate()
                }

            connection.prepareStatement("INSERT INTO Orders (customerName, orderDate, totalAmount, orderDetails) VALUES (?, ?, ?, ?)")
                .use {
                    it.setString(1, "John Doe")
                    it.setString(2, "2023-07-21")
                    it.setDouble(3, 150.75)
                    it.setBytes(4, orderDetails)
                    it.executeUpdate()
                }

            connection.prepareStatement("INSERT INTO Orders (customerName, orderDate, totalAmount, orderDetails) VALUES (?, ?, ?, ?)")
                .use {
                    it.setString(1, "Max Joint")
                    it.setString(2, "2023-08-21")
                    it.setDouble(3, 250.75)
                    it.setBytes(4, orderDetails)
                    it.executeUpdate()
                }
        }

        @AfterClass
        @JvmStatic
        fun tearDownClass() {
            try {
                connection.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    @Test
    fun `read from tables`() {
        val df = DataFrame.readSqlTable(connection, "Customers").cast<CustomerSQLite>()
        df.rowsCount() shouldBe 2

        val df2 = DataFrame.readSqlTable(connection, "Orders").cast<CustomerSQLite>()
        df2.rowsCount() shouldBe 2
    }

    @Test
    fun `read from sql query`() {
        @Language("SQL")
        val sqlQuery = """
            SELECT
                c.id AS customerId,
                c.name AS customerName,
                c.age AS customerAge,
                c.salary AS customerSalary,
                c.profilePicture AS customerProfilePicture,
                o.id AS orderId,
                o.orderDate AS orderDate,
                o.totalAmount AS totalAmount,
                o.orderDetails AS orderDetails
            FROM Customers c
            INNER JOIN Orders o ON c.name = o.customerName
            """

        val df = DataFrame.readSqlQuery(connection, sqlQuery).cast<CustomerOrderSQLite>()
        df.rowsCount() shouldBe 2

        val schema = DataFrame.getSchemaForSqlQuery(connection, sqlQuery)
        schema.columns.entries.size shouldBe 9
    }

    @Test
    fun `read from all tables`() {
        val dataframes = DataFrame.readAllSqlTables(connection)

        val customerDf = dataframes[0].cast<CustomerSQLite>()

        customerDf.rowsCount() shouldBe 2
        customerDf.filter { it[CustomerSQLite::age] > 30 }.rowsCount() shouldBe 1
        customerDf[0][1] shouldBe "John Doe"

        val orderDf = dataframes[1].cast<OrderSQLite>()

        orderDf.rowsCount() shouldBe 2
        orderDf.filter { it[OrderSQLite::totalAmount] > 200 }.rowsCount() shouldBe 1
        orderDf[0][1] shouldBe "John Doe"
    }
}

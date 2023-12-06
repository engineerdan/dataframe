package org.jetbrains.kotlinx.dataframe.io

import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import org.jetbrains.kotlinx.dataframe.api.cast
import org.jetbrains.kotlinx.dataframe.api.filter
import org.jetbrains.kotlinx.dataframe.api.print
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test
import java.math.BigDecimal
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import org.junit.Ignore

private const val URL = "jdbc:mariadb://localhost:3307"
private const val USER_NAME = "root"
private const val PASSWORD = "pass"
private const val TEST_DATABASE_NAME = "testKDFdatabase"

@DataSchema
interface Table1MariaDb {
    val id: Int
    val bitCol: Boolean
    val tinyintCol: Int
    val smallintCol: Int
    val mediumintCol: Int
    val mediumintUnsignedCol: Long
    val integerCol: Int
    val intCol: Int
    val integerUnsignedCol: Long
    val bigintCol: Long
    val floatCol: Float
    val doubleCol: Double
    val decimalCol: Double
    val dateCol: String
    val datetimeCol: String
    val timestampCol: String
    val timeCol: String
    val yearCol: String
    val varcharCol: String
    val charCol: String
    val binaryCol: ByteArray
    val varbinaryCol: ByteArray
    val tinyblobCol: ByteArray
    val blobCol: ByteArray
    val mediumblobCol: ByteArray
    val longblobCol: ByteArray
    val textCol: String
    val mediumtextCol: String
    val longtextCol: String
    val enumCol: String
    val setCol: String
}

@DataSchema
interface Table2MariaDb {
    val id: Int
    val enumCol: String
    val setCol: String
}

@Ignore
class MariadbTest {
    companion object {
        private lateinit var connection: Connection

        @BeforeClass
        @JvmStatic
        fun setUpClass() {
            connection = DriverManager.getConnection(URL, USER_NAME, PASSWORD)

            connection.createStatement().use { st ->
                // Drop the test database if it exists
                val dropDatabaseQuery = "DROP DATABASE IF EXISTS $TEST_DATABASE_NAME"
                st.executeUpdate(dropDatabaseQuery)

                // Create the test database
                val createDatabaseQuery = "CREATE DATABASE $TEST_DATABASE_NAME"
                st.executeUpdate(createDatabaseQuery)

                // Use the newly created database
                val useDatabaseQuery = "USE $TEST_DATABASE_NAME"
                st.executeUpdate(useDatabaseQuery)
            }

            connection.createStatement().use { st -> st.execute("DROP TABLE IF EXISTS table1") }
            connection.createStatement().use { st -> st.execute("DROP TABLE IF EXISTS table2") }

            @Language("SQL")
            val createTableQuery = """
            CREATE TABLE IF NOT EXISTS table1 (
                id INT AUTO_INCREMENT PRIMARY KEY,
                bitCol BIT,
                tinyintCol TINYINT,
                smallintCol SMALLINT,
                mediumintCol MEDIUMINT,
                mediumintUnsignedCol MEDIUMINT UNSIGNED,
                integerCol INTEGER,
                intCol INT,
                integerUnsignedCol INTEGER UNSIGNED,
                bigintCol BIGINT,
                floatCol FLOAT,
                doubleCol DOUBLE,
                decimalCol DECIMAL,
                dateCol DATE,
                datetimeCol DATETIME,
                timestampCol TIMESTAMP,
                timeCol TIME,
                yearCol YEAR,
                varcharCol VARCHAR(255),
                charCol CHAR(10),
                binaryCol BINARY(64),
                varbinaryCol VARBINARY(128),
                tinyblobCol TINYBLOB,
                blobCol BLOB,
                mediumblobCol MEDIUMBLOB,
                longblobCol LONGBLOB,
                textCol TEXT,
                mediumtextCol MEDIUMTEXT,
                longtextCol LONGTEXT,
                enumCol ENUM('Value1', 'Value2', 'Value3'),
                setCol SET('Option1', 'Option2', 'Option3')
            )
        """
            connection.createStatement().execute(
                createTableQuery.trimIndent()
            )

            @Language("SQL")
            val createTableQuery2 = """
                CREATE TABLE IF NOT EXISTS table2 (
                id INT AUTO_INCREMENT PRIMARY KEY,
                bitCol BIT,
                tinyintCol TINYINT,
                smallintCol SMALLINT,
                mediumintCol MEDIUMINT,
                mediumintUnsignedCol MEDIUMINT UNSIGNED,
                integerCol INTEGER,
                intCol INT,
                integerUnsignedCol INTEGER UNSIGNED,
                bigintCol BIGINT,
                floatCol FLOAT,
                doubleCol DOUBLE,
                decimalCol DECIMAL,
                dateCol DATE,
                datetimeCol DATETIME,
                timestampCol TIMESTAMP,
                timeCol TIME,
                yearCol YEAR,
                varcharCol VARCHAR(255),
                charCol CHAR(10),
                binaryCol BINARY(64),
                varbinaryCol VARBINARY(128),
                tinyblobCol TINYBLOB,
                blobCol BLOB,
                mediumblobCol MEDIUMBLOB,
                longblobCol LONGBLOB,
                textCol TEXT,
                mediumtextCol MEDIUMTEXT,
                longtextCol LONGTEXT,
                enumCol ENUM('Value1', 'Value2', 'Value3'),
                setCol SET('Option1', 'Option2', 'Option3')
            )
            """
            connection.createStatement().execute(
                createTableQuery2.trimIndent()
            )

            @Language("SQL")
            val insertData1 = """
            INSERT INTO table1 (
                bitCol, tinyintCol, smallintCol, mediumintCol, mediumintUnsignedCol, integerCol, intCol, 
                integerUnsignedCol, bigintCol, floatCol, doubleCol, decimalCol, dateCol, datetimeCol, timestampCol,
                timeCol, yearCol, varcharCol, charCol, binaryCol, varbinaryCol, tinyblobCol, blobCol,
                mediumblobCol, longblobCol, textCol, mediumtextCol, longtextCol, enumCol, setCol
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()


            @Language("SQL")
            val insertData2 = """
            INSERT INTO table2 (
                bitCol, tinyintCol, smallintCol, mediumintCol, mediumintUnsignedCol, integerCol, intCol, 
                integerUnsignedCol, bigintCol, floatCol, doubleCol, decimalCol, dateCol, datetimeCol, timestampCol,
                timeCol, yearCol, varcharCol, charCol, binaryCol, varbinaryCol, tinyblobCol, blobCol,
                mediumblobCol, longblobCol, textCol, mediumtextCol, longtextCol, enumCol, setCol
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

            connection.prepareStatement(insertData1).use { st ->
                // Insert data into table1
                for (i in 1..3) {
                    st.setBoolean(1, true)
                    st.setByte(2, i.toByte())
                    st.setShort(3, (i * 10).toShort())
                    st.setInt(4, i * 100)
                    st.setInt(5, i * 100)
                    st.setInt(6, i * 100)
                    st.setInt(7, i * 100)
                    st.setInt(8, i * 100)
                    st.setInt(9, i * 100)
                    st.setFloat(10, i * 10.0f)
                    st.setDouble(11, i * 10.0)
                    st.setBigDecimal(12, BigDecimal(i * 10))
                    st.setDate(13, java.sql.Date(System.currentTimeMillis()))
                    st.setTimestamp(14, java.sql.Timestamp(System.currentTimeMillis()))
                    st.setTimestamp(15, java.sql.Timestamp(System.currentTimeMillis()))
                    st.setTime(16, java.sql.Time(System.currentTimeMillis()))
                    st.setInt(17, 2023)
                    st.setString(18, "varcharValue$i")
                    st.setString(19, "charValue$i")
                    st.setBytes(20, "binaryValue".toByteArray())
                    st.setBytes(21, "varbinaryValue".toByteArray())
                    st.setBytes(22, "tinyblobValue".toByteArray())
                    st.setBytes(23, "blobValue".toByteArray())
                    st.setBytes(24, "mediumblobValue".toByteArray())
                    st.setBytes(25, "longblobValue".toByteArray())
                    st.setString(26, "textValue$i")
                    st.setString(27, "mediumtextValue$i")
                    st.setString(28, "longtextValue$i")
                    st.setString(29, "Value$i")
                    st.setString(30, "Option$i")

                    st.executeUpdate()
                }
            }

            connection.prepareStatement(insertData2).use { st ->
                // Insert data into table2
                for (i in 1..3) {
                    st.setBoolean(1, false)
                    st.setByte(2, (i * 2).toByte())
                    st.setShort(3, (i * 20).toShort())
                    st.setInt(4, i * 200)
                    st.setInt(5, i * 200)
                    st.setInt(6, i * 200)
                    st.setInt(7, i * 200)
                    st.setInt(8, i * 200)
                    st.setInt(9, i * 200)
                    st.setFloat(10, i * 20.0f)
                    st.setDouble(11, i * 20.0)
                    st.setBigDecimal(12, BigDecimal(i * 20))
                    st.setDate(13, java.sql.Date(System.currentTimeMillis()))
                    st.setTimestamp(14, java.sql.Timestamp(System.currentTimeMillis()))
                    st.setTimestamp(15, java.sql.Timestamp(System.currentTimeMillis()))
                    st.setTime(16, java.sql.Time(System.currentTimeMillis()))
                    st.setInt(17, 2023)
                    st.setString(18, "varcharValue$i")
                    st.setString(19, "charValue$i")
                    st.setBytes(20, "binaryValue".toByteArray())
                    st.setBytes(21, "varbinaryValue".toByteArray())
                    st.setBytes(22, "tinyblobValue".toByteArray())
                    st.setBytes(23, "blobValue".toByteArray())
                    st.setBytes(24, "mediumblobValue".toByteArray())
                    st.setBytes(25, "longblobValue".toByteArray())
                    st.setString(26, "textValue$i")
                    st.setString(27, "mediumtextValue$i")
                    st.setString(28, "longtextValue$i")
                    st.setString(29, "Value$i")
                    st.setString(30, "Option$i")
                    st.executeUpdate()
                }
            }
        }

        @AfterClass
        @JvmStatic
        fun tearDownClass() {
            try {
                connection.createStatement().use { st -> st.execute("DROP TABLE IF EXISTS table1") }
                connection.createStatement().use { st -> st.execute("DROP TABLE IF EXISTS table2") }
                connection.createStatement().use { st -> st.execute("DROP DATABASE IF EXISTS $TEST_DATABASE_NAME") }
                connection.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    @Test
    fun `basic test for reading sql tables`() {
        val df1 = DataFrame.readSqlTable(connection, "table1").cast<Table1MariaDb>()
        df1.print()
        assertEquals(3, df1.rowsCount())

        val df2 = DataFrame.readSqlTable(connection, "table2").cast<Table1MariaDb>()
        df2.print()
        assertEquals(3, df2.rowsCount())
    }

    @Test
    fun `read from sql query`() {
        @Language("SQL")
        val sqlQuery = """
            SELECT
               t1.id,
               t2.enumCol,
               t2.setCol
            FROM table1 t1
            JOIN table2 t2 ON t1.id = t2.id;
        """.trimIndent()

        val df = DataFrame.readSqlQuery(connection, sqlQuery = sqlQuery).cast<Table2MariaDb>()
        df.rowsCount() shouldBe 3
    }

    @Test
    fun `read from all tables`() {
        val dataframes = DataFrame.readAllSqlTables(connection)

        val table1Df = dataframes[0].cast<Table1MariaDb>()

        table1Df.rowsCount() shouldBe 3
        table1Df.filter { it[Table1MariaDb::integerCol] > 100 }.rowsCount() shouldBe 2
        table1Df[0][11] shouldBe 10.0

        val table2Df = dataframes[1].cast<Table1MariaDb>()

        table2Df.rowsCount() shouldBe 3
        table2Df.filter { it[Table1MariaDb::integerCol] > 400 }.rowsCount() shouldBe 1
        table2Df[0][11] shouldBe 20.0
    }
}

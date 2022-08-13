package com.github.alexmelyon.rubicssolver

import java.io.Closeable
import java.net.URLDecoder
import java.net.URLEncoder
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

class RubicsStorage : Closeable {

    lateinit var connection: Connection
    lateinit var statement: Statement

    fun init() {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:")
        statement = connection.createStatement()
        statement.queryTimeout = 30

        statement.executeUpdate("DROP TABLE IF EXISTS variants")
        statement.executeUpdate(
            "CREATE TABLE variants (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "cube BLOB, " +
                    "description STRING, " +
                    "depth INTEGER, " +
                    "previous_id INTEGER)"
        )
    }

    private val cellsToSave = intArrayOf(
        3, 4, 5, 12, 13, 14, 21, 22, 23,
        27, 28, 29, 30, 31, 32, 33, 34, 35,
        36, 37, 38, 39, 40, 41, 42, 43, 44,
        45, 46, 47, 48, 49, 50, 51, 52, 53,
        57, 58, 59, 66, 67, 68, 75, 76, 77,
        84, 85, 86, 93, 94, 95, 102, 103, 104
    )

    fun add(variant: RubicsSolver.Variant) {
//        val cube = variant.cube.joinToString(",")
        val cube = ByteArray(9 * 6)
        var offset = 0
        for (c in cellsToSave) {
            cube[offset] = variant.cube[c].toByte()
            offset++
        }
        val sql = "INSERT INTO variants (cube, description, depth, previous_id) VALUES(?, ?, ?, ?)"
        val stm = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
//        stm.setString(1, cube)
//        stm.setBlob(1, cube.inputStream()) // java.sql.SQLFeatureNotSupportedException
//        stm.setBinaryStream(1, cube.inputStream())
        stm.setBytes(1, cube)
        stm.setString(2, URLEncoder.encode(variant.description))
        stm.setInt(3, variant.depth)
        stm.setLong(4, variant.previousId)
        val affectedRows = stm.executeUpdate()
        val generated = stm.generatedKeys
        val hasNext = generated.next()
        val autoId = generated.getLong(1)
        variant.id = autoId
    }

    fun getById(id: Long): RubicsSolver.Variant {
        val rs = statement.executeQuery("SELECT * FROM variants WHERE id = $id")
        val exists = rs.next()
        val variantId = rs.getLong("id")
//        val cube = rs.getString("cube").split(",").map { it.toInt() }.toIntArray()\
//        val cube = rs.getBlob("cube").getBytes(0, 9 * 6)
//        val cube = rs.getBinaryStream("cube").readBytes()
        val bytes = rs.getBytes("cube")
        val cube = ByteArray(9 * 12)
        var offset = 0
        for(cell in cellsToSave) {
            cube[cell] = bytes[offset]
            offset++
        }
        try {
            return RubicsSolver.Variant(
                variantId,
                cube,
                URLDecoder.decode(rs.getString("description")),
                rs.getInt("depth"),
                rs.getLong("previous_id")
            )
        } catch (t: Throwable) {
            println("Error id=$id $exists count=${getCount()} max=${getMax()}")
            throw t
        }
    }

    private fun getCount(): Int {
        statement.executeQuery("SELECT COUNT(*) FROM variants").let { rs ->
            rs.next()
            return rs.getInt(1)
        }
    }

    private fun getMax(): Int {
        statement.executeQuery("SELECT MAX(id) FROM variants").let { rs ->
            rs.next()
            return rs.getInt(1)
        }
    }

    override fun close() {
        connection.close()
    }


}
package com.github.alexmelyon.rubicssolver

import java.io.Closeable
import java.net.URLDecoder
import java.net.URLEncoder
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

typealias TMove = List<Pair<Int, Int>>
typealias SMove = Pair<String, TMove>

abstract class RubicsSolver {

    companion object {
        const val R: Byte = 1
        const val W: Byte = 2
        const val B: Byte = 3
        const val Y: Byte = 4
        const val G: Byte = 5
        const val O: Byte = 6
    }

    protected val storage = RubicsStorage()

    private val solved = byteArrayOf(
        0, 0, 0, Y, Y, Y, 0, 0, 0, //  0  0  0,  3  4  5,  0  0  0
        0, 0, 0, Y, Y, Y, 0, 0, 0, //  0  0  0, 12 13 14,  0  0  0
        0, 0, 0, Y, Y, Y, 0, 0, 0, //  0  0  0, 21 22 23,  0  0  0
        O, O, O, B, B, B, R, R, R, // 27 28 29, 30 31 32, 33 34 35
        O, O, O, B, B, B, R, R, R, // 36 37 38, 39 40 41, 42 43 44
        O, O, O, B, B, B, R, R, R, // 45 46 47, 48 49 50, 51 52 53
        0, 0, 0, W, W, W, 0, 0, 0, //  0  0  0, 57 58 59,  0  0  0
        0, 0, 0, W, W, W, 0, 0, 0, //  0  0  0, 66 67 68,  0  0  0
        0, 0, 0, W, W, W, 0, 0, 0, //  0  0  0, 75 76 77,  0  0  0
        0, 0, 0, G, G, G, 0, 0, 0, //  0  0  0, 84 85 86,  0  0  0
        0, 0, 0, G, G, G, 0, 0, 0, //  0  0  0, 93 94 95,  0  0  0
        0, 0, 0, G, G, G, 0, 0, 0  //  0  0  0, 02 03 04,  0  0  0
    )

    val moveR: TMove = listOf(
        5 to 86,
        14 to 95,
        23 to 104,

        32 to 5,
        41 to 14,
        50 to 23,

        59 to 32,
        68 to 41,
        77 to 50,

        86 to 59,
        95 to 68,
        104 to 77,

        33 to 35,
        34 to 44,
        35 to 53,
        44 to 52,
        53 to 51,
        52 to 42,
        51 to 33,
        42 to 34
    )

    val moveL: TMove = listOf(
        3 to 30,
        12 to 39,
        21 to 48,

        30 to 57,
        39 to 66,
        48 to 75,

        57 to 84,
        66 to 93,
        75 to 102,

        84 to 3,
        93 to 12,
        102 to 21,

        27 to 29,
        28 to 38,
        29 to 47,
        38 to 46,
        47 to 45,
        46 to 36,
        45 to 27,
        36 to 28
    )

    val moveU: TMove = listOf(
        30 to 27,
        31 to 28,
        32 to 29,

        33 to 30,
        34 to 31,
        35 to 32,

        102 to 35,
        103 to 34,
        104 to 33,

        27 to 104,
        28 to 103,
        29 to 102,

        3 to 5,
        4 to 14,
        5 to 23,
        14 to 22,
        23 to 21,
        22 to 12,
        21 to 3,
        12 to 4
    )

    val moveD: TMove = listOf(
        45 to 48,
        46 to 49,
        47 to 50,

        48 to 51,
        49 to 52,
        50 to 53,

        51 to 86,
        52 to 85,
        53 to 84,

        84 to 47,
        85 to 46,
        86 to 45,

        57 to 59,
        58 to 68,
        59 to 77,
        68 to 76,
        77 to 75,
        76 to 66,
        75 to 57,
        66 to 58
    )

    val moveF: TMove = listOf(
        21 to 33,
        22 to 42,
        23 to 51,

        33 to 59,
        42 to 58,
        51 to 57,

        57 to 29,
        58 to 38,
        59 to 47,

        29 to 23,
        38 to 22,
        47 to 21,

        30 to 32,
        31 to 41,
        32 to 50,
        41 to 49,
        50 to 48,
        49 to 39,
        48 to 30,
        39 to 31
    )

    val moveB: TMove = listOf(
        35 to 3,
        44 to 4,
        53 to 5,

        77 to 35,
        76 to 44,
        75 to 53,

        27 to 75,
        36 to 76,
        45 to 77,

        3 to 45,
        4 to 36,
        5 to 27,

        84 to 86,
        85 to 95,
        86 to 104,
        95 to 103,
        104 to 102,
        103 to 93,
        102 to 84,
        93 to 85
    )

    fun move(cube: ByteArray, motion: TMove): ByteArray {
        val newCube = cube.clone()
        for (item in motion) {
            newCube[item.second] = cube[item.first]
        }
        return newCube
    }

    class Variant(var id: Long = 0L, val cube: ByteArray, val description: String, val depth: Int, val previousId: Long)

    abstract fun solve(cube: ByteArray): String

    protected fun isRecursion(variant: Variant): Boolean {
        var next = variant
        while (next.previousId != 0L) {
            val prevId = next.previousId ?: return false
            if (variant.id == prevId) {
                return false
            }
            if (prevId == 0L) {
                return false
            }
            val prev = storage.getById(prevId)
            if (next.cube.deepEquals(prev.cube)) {
                return true
            }
            next = prev
        }
        return false
    }

    private fun ByteArray?.deepEquals(other: ByteArray?): Boolean {
        if (this == null || other == null) {
            return false
        }
        if (this === other) {
            return true
        }
        if (this.size != other.size) {
            return false
        }
        for (i in 0 until size) {
            if (this[i] != other[i]) {
                return false
            }
        }
        // TODO Rotate front Blue, top Yellow, right Red and check
        return true
    }

    protected fun isSolved(cube: ByteArray): Boolean {
        return solved.deepEquals(cube)
    }

}

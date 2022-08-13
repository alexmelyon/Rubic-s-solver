package com.github.alexmelyon.rubicssolver

class DeepSolverStrategy : RubicsSolver() {

    override fun solve(cube: ByteArray): String {
        val motions: List<SMove> = listOf(
            "R" to moveR,
            "L" to moveL,
            "U" to moveU,
            "D" to moveD,
            "F" to moveF,
            "B" to moveB,
            "R'" to moveR.map { it.second to it.first },
            "L'" to moveL.map { it.second to it.first },
            "U'" to moveU.map { it.second to it.first },
            "D'" to moveD.map { it.second to it.first },
            "F'" to moveF.map { it.second to it.first },
            "B'" to moveB.map { it.second to it.first }
        )
        storage.init()
        val initial = Variant(1, cube, "", depth = 0, previousId = 0)
        storage.add(initial)
        var index = 0
        val maxDepth = 999
        try {
            while (true) {
                val ii = index
                index++
                val current = storage.getById((ii + 1).toLong())
                if (current.depth > maxDepth) {
                    throw RuntimeException("Max depth $maxDepth reached")
                }
                if (isSolved(current.cube)) {
                    return current.description
                }
                if (isRecursion(current)) {
                    println("$ii ${current.description} RECURSION")
                    continue
                }
                println("$ii ${current.description}")
                motions.forEach { motion ->
                    val c = move(current.cube, motion.second)
                    val desc = current.description + " " + motion.first
                    val v = Variant(id = 0L, c, desc, current.depth + 1, current.id)
                    storage.add(v)
                }
            }
        } finally {
            storage.close()
        }
    }
}
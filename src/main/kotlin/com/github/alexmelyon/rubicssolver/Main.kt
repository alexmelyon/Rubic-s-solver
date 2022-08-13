package com.github.alexmelyon.rubicssolver

import com.github.alexmelyon.rubicssolver.RubicsSolver.Companion.B
import com.github.alexmelyon.rubicssolver.RubicsSolver.Companion.G
import com.github.alexmelyon.rubicssolver.RubicsSolver.Companion.O
import com.github.alexmelyon.rubicssolver.RubicsSolver.Companion.R
import com.github.alexmelyon.rubicssolver.RubicsSolver.Companion.W
import com.github.alexmelyon.rubicssolver.RubicsSolver.Companion.Y


fun main() {

    val cube = byteArrayOf(
        0, 0, 0, B, O, R, 0, 0, 0,
        0, 0, 0, G, R, R, 0, 0, 0,
        0, 0, 0, Y, Y, R, 0, 0, 0,
        Y, O, R, G, O, B, W, W, W,
        G, Y, Y, R, B, B, W, W, W,
        Y, Y, O, G, B, B, W, W, W,
        0, 0, 0, Y, R, O, 0, 0, 0,
        0, 0, 0, G, O, O, 0, 0, 0,
        0, 0, 0, B, B, O, 0, 0, 0,
        0, 0, 0, O, Y, G, 0, 0, 0,
        0, 0, 0, R, G, G, 0, 0, 0,
        0, 0, 0, R, B, G, 0, 0, 0
    )
    val solver = DeepSolverStrategy()
    val solution = solver.solve(cube)
    println("Solved $solution")
}
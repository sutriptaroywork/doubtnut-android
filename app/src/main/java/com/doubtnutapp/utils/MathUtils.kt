package com.doubtnutapp.utils

object MathUtils {

    fun isFibonacciNumber(n: Int): Boolean {
        // n is Fibonacci if one of 5*n*n + 4 or 5*n*n - 4 or both
        // is a perfect square
        return isPerfectSquare(5 * n * n + 4) || isPerfectSquare(5 * n * n - 4)
    }

    fun getAdjustedHeight(originalWidth: Int, originalHeight: Int): String {
        val factor = computeGCD(originalWidth, originalHeight)
        val widthRatio = originalWidth / factor
        val heightRatio = originalHeight / factor
        return "$widthRatio:$heightRatio"
    }

    // Greatest Common Denominator
    private fun computeGCD(a: Int, b: Int): Int {
        return if (b == 0) a else computeGCD(b, a % b)
    }

    private fun isPerfectSquare(x: Int): Boolean {
        val s = Math.sqrt(x.toDouble()).toInt()
        return s * s == x
    }
}
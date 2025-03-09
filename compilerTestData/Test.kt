/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package compilerTestData

object Main {
    fun fibonacciRecursive(n: Int): Int {
        if (n < 2) {
            return n
        } else {
            return fibonacciRecursive(n - 1) + fibonacciRecursive(n - 2)
        }
    }

    fun main() {
        val result = fibonacciRecursive(4)
        println(result)
        return
    }
}
/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package compilerTestData

object Main {
    fun fibonacciRecursive(n: Int): Int {
        if (n == 0) return 0
        var a = 0
        var b = 1
        var count = 1  // 已经计算到第1项（从0开始）

        // 循环直到计算到第n项
        while (count < n) {
            val next = a + b
            a = b       // 更新前一项
            b = next    // 更新当前项
            count = count + 1     // 计数器递增
        }
        return b
    }

    fun main() {
        val result = fibonacciRecursive(4)
        println(result)
        return
    }
}
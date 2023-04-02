package com.bqliang.running.diary

import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun listPlusTest() {
        val list = MutableStateFlow(listOf(listOf("a", "b")))
        println(list.value)
        val x = list.value.plus(listOf("q"))
        println(list.value)
    }

    @Test
    fun hi() {
        val list = listOf(listOf("a", "b"), listOf("c", "d"))
        val l = list.plus(listOf(listOf("q", "w")))
        println(list)
        println(l)
    }
}
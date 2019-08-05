package test

import junit.framework.Assert.assertEquals
import org.junit.Test
import utils.ListSegment
import utils.SegmentsType
import java.util.*
import kotlin.test.assertFalse

class ListChunksTest {

    private val data = listOf<Int>(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17)
    private val dataBig = mutableListOf<Int>()
    private val data1 = listOf<Int>(1)
    private val data2 = listOf<Int>(1, 2)
    private val data3 = listOf<Int>(1, 2, 3)
    private val dataEmpty = listOf<Int>()

    @Test
    fun assertEmpty() {
        for (i in 0..100) {
            val chunks = ListSegment<Int>(dataEmpty, i, SegmentsType.REPEAT).segments
            assert(chunks.isEmpty())
        }
        for (i in 0..100) {
            val chunks = ListSegment<Int>(dataEmpty, i, SegmentsType.NON_REPEAT).segments
            assert(chunks.isEmpty())
        }
    }

    @Test
    fun assertOne() {
        for (i in 0..100) {
            val chunks = ListSegment<Int>(data1, i, SegmentsType.REPEAT, 3).segments
            assert(chunks.size == 1)
            assert(chunks[0].size == 1)
        }
        for (i in 0..100) {
            val chunks = ListSegment<Int>(data1, i, SegmentsType.NON_REPEAT, 3).segments
            assert(chunks.size == 1)
            assert(chunks[0].size == 1)
        }
    }

    @Test
    fun assertTwo() {
        for (i in 0..100) {
            val chunks = ListSegment<Int>(data2, i, SegmentsType.REPEAT, 3).segments
            assert(chunks.size == 1)
            assert(chunks[0].size == 2)
        }
        for (i in 0..100) {
            val chunks = ListSegment<Int>(data2, i, SegmentsType.NON_REPEAT, 3).segments
            assert(chunks.size == 1)
            assert(chunks[0].size == 2)
        }
    }

    @Test
    fun assertThree() {
        for (i in 0..100) {
            val chunks = ListSegment<Int>(data3, i, SegmentsType.REPEAT, 3).segments
            assert(chunks.size == 1)
            assert(chunks[0].size == 3)
        }
        for (i in 0..100) {
            val chunks = ListSegment<Int>(data3, i, SegmentsType.NON_REPEAT, 3).segments
            assert(chunks.size == 1)
            assert(chunks[0].size == 3)
        }
    }


    @Test
    fun eyeBall() {
        for (i in 1..123) dataBig.add(i)
        val sets = listOf<List<Int>>(dataEmpty, data1, data2, data3, data, dataBig)
        sets.forEach { set ->
            println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")
            println("SET: $set")
            for (i in 3..13) {
                val chunksRepeat = ListSegment<Int>(set, i, SegmentsType.REPEAT, 3).segments
                val chunksNonRepeat = ListSegment<Int>(set, i, SegmentsType.NON_REPEAT, 3).segments
                println("________________________________________________________________________")
                println("________________________________________________________________________")
                println("CHUNK: $i")
                println("________________________________________________________________________")
                println("REPEAT")
                println(chunksRepeat)
                println("________________________________________________________________________")
                println("NON_REPEAT")
                println(chunksNonRepeat)
            }
        }
    }

    @Test
    fun assertsGeneric() {
        println("-------------------------------------------------")
        println("-------------------------------------------------")
        println("-------------------------------------------------")
        println("assertsGeneric")
        println("-------------------------------------------------")
        println("-------------------------------------------------")
        println("-------------------------------------------------")

        for (i in 3..12) {
            val chunksOnEmptyRepeat = ListSegment<Int>(dataEmpty, i, SegmentsType.REPEAT, 3).segments
            val chunksOnEmptyNonRepeat = ListSegment<Int>(dataEmpty, i, SegmentsType.NON_REPEAT, 3).segments
            assert(chunksOnEmptyRepeat.isEmpty())
            assert(chunksOnEmptyNonRepeat.isEmpty())
        }

        val chunksRepeat = ListSegment<Int>(data, 3, SegmentsType.REPEAT, 3).segments

        println("data")
        println(data)
        println("chunks 3 repeat")
        println(chunksRepeat)

        assertEquals(6, chunksRepeat.size)

        val chunksNonRepeat = ListSegment<Int>(data, 3, SegmentsType.NON_REPEAT, 3).segments

        println("data")
        println(data)
        println("chunks 3 nonRepeat")
        println(chunksNonRepeat)

        assertEquals(6, chunksNonRepeat.size)
    }

    @Test
    fun test_non_repeat_contains_all_members_once() {
        val rnd = Random()
        for (i in 1..100) {
            val size = rnd.nextInt(1000)
            val data = mutableListOf<Int>()
            val haveSeen = mutableSetOf<Int>()
            for (j in 1..size) data.add(j)
            val segmentsNonRepeat = ListSegment<Int>(data, i, SegmentsType.NON_REPEAT, 3).segments
            var sum = 0
            segmentsNonRepeat.forEach {
                sum += it.size
                assert(it.isNotEmpty())
                it.forEach { current ->
                    assertFalse { haveSeen.contains(current) }
                    haveSeen.add(current)
                }
            }
            assertEquals(data.size, sum)
        }
    }
}
package test

import geospatial.MIN_SEGMENTS_SIZE
import junit.framework.Assert.assertEquals
import org.junit.Test
import utils.ListSegment
import utils.SegmentsType

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
    }

    @Test
    fun assertOne() {
        for (i in 0..100) {
            val chunks = ListSegment<Int>(data1, i, SegmentsType.REPEAT).segments
            assert(chunks.size == 1)
            assert(chunks[0].size == 1)
        }
    }

    @Test
    fun eyeBall() {
        for (i in 1..123) dataBig.add(i)
        val sets = listOf<List<Int>>(dataEmpty, data1, data2, data3, data, dataBig)
        sets.forEach { set ->
            println("SET: $set")
            for (i in MIN_SEGMENTS_SIZE..MIN_SEGMENTS_SIZE + 8) {
                val chunks = ListSegment<Int>(set, i, SegmentsType.REPEAT).segments
                println("CHUNK: $i")
                println(chunks)
            }
        }
    }

    @Test
    fun assertsGeneric(){
        println("-------------------------------------------------")
        println("-------------------------------------------------")
        println("-------------------------------------------------")
        println("assertsGeneric")
        println("-------------------------------------------------")
        println("-------------------------------------------------")
        println("-------------------------------------------------")

        for(i in 3..12) {
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

        assertEquals(9, chunksRepeat.size)

        val chunksNonRepeat = ListSegment<Int>(data, 3, SegmentsType.NON_REPEAT, 3).segments

        println("data")
        println(data)
        println("chunks 3 nonRepeat")
        println(chunksNonRepeat)

        assertEquals(9, chunksNonRepeat.size)
    }
}
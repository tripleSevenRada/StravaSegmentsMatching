package test

import geospatial.CHUNK_SIZE
import org.junit.Test
import utils.ListChunks

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
            val chunks = ListChunks<Int>(dataEmpty, i).chunks
            assert(chunks.isEmpty())
        }
    }

    @Test
    fun assertOne() {
        for (i in 0..100) {
            val chunks = ListChunks<Int>(data1, i).chunks
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
            for (i in CHUNK_SIZE..CHUNK_SIZE + 8) {
                val chunks = ListChunks<Int>(set, i).chunks
                println("CHUNK: $i")
                println(chunks)
            }
        }
    }
}
package utils

import geospatial.CHUNK_SIZE

class ListChunks<T>(val data: List<T>, val chunkSize: Int) {
    private val _chunks = mutableListOf<List<T>>()
    val chunks: List<List<T>>
        get() {
            if (data.isEmpty()) return _chunks
            var currentChunk = mutableListOf<T>()
            fun newChunk() {
                _chunks.add(currentChunk)
                currentChunk = mutableListOf<T>()
            }
            fun push(i: Int) {
                currentChunk.add(data[i])
            }
            fun clipChunkSize(ch: Int): Int = if (ch < CHUNK_SIZE) CHUNK_SIZE else ch
            val clippedChunk = clipChunkSize(chunkSize)
            for (i in 0 until data.size) {
                if (i == 0) {
                    push(i)
                    continue
                }
                if (i % clippedChunk == 0) {
                    push(i)
                    newChunk()
                    push(i)
                } else {
                    push(i)
                }
                if (i == data.lastIndex) newChunk()
            }
            return _chunks
        }
}
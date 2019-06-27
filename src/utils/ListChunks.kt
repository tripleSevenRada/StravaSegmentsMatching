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
            val clippedChunk = (clipChunkSize(chunkSize)) - 1
            for (i in data.indices) {
                if (i == 0) {
                    push(i)
                    if (i == data.lastIndex) newChunk()
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
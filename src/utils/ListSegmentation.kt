package utils

import geospatial.SEGMENTS_SIZE

class SegmentHelpFunctions{
    fun <T>newSegment(_segments: MutableList<List<T>>, currentSegment: MutableList<T>): List<T>{
        _segments.add(currentSegment)
        return mutableListOf<T>()
    }
    fun <T>push(currentSegment: MutableList<T>, data: MutableList<T>, i: Int) {
        currentSegment.add(data[i])
    }
    fun clipSegmentsSize(s: Int): Int = if (s < SEGMENTS_SIZE) SEGMENTS_SIZE else s
}

class ListChunks<T>(val data: List<T>, val chunkSize: Int) {
    // 1,2,3,4,5,6,7,8,9
    // 1,2,3-3,4,5-5,6,7-7,8,9
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
            fun clipChunkSize(ch: Int): Int = if (ch < SEGMENTS_SIZE) SEGMENTS_SIZE else ch
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

class ListSlices<T>(val data: List<T>, val sliceSize: Int) {
    // 1,2,3,4,5,6,7,8,9
    // 1,2,3-4,5,6-7,8,9
    private val _slices = mutableListOf<List<T>>()
    val slices: List<List<T>>
        get() {
            if (data.isEmpty()) return _slices

            //TODO
            return _slices
        }
}
package utils

import geospatial.MIN_SEGMENTS_SIZE

class SegmentHelpFunctions<T> {
    fun newSegment(segments: MutableList<List<T>>,
                   currentSegment: MutableList<T>): MutableList<T> {
        segments.add(currentSegment)
        return mutableListOf()
    }

    fun push(currentSegment: MutableList<T>, data: List<T>, i: Int) {
        currentSegment.add(data[i])
    }

    fun clipSegmentsSize(s: Int, minSegmentSize: Int): Int =
            if (s < minSegmentSize) minSegmentSize else s
}

class ListSegment<T>(private val data: List<T>,
                     private val segmentsSize: Int,
                     private val type: SegmentsType,
                     private val minSegmentSize: Int = MIN_SEGMENTS_SIZE) {

    /*
    EXPECTED:
    data
    [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17]
    chunks 3 repeat
    [[1, 2, 3, 4], [4, 5, 6, 7], [7, 8, 9, 10], [10, 11, 12, 13], [13, 14, 15, 16], [16, 17]]
    data
    [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17]
    chunks 3 nonRepeat
    [[1, 2, 3, 4], [5, 6, 7], [8, 9, 10], [11, 12, 13], [14, 15, 16], [17]]
    */

    private val _segments = mutableListOf<List<T>>()
    val segments: List<List<T>>
        get() {
            if (data.isEmpty()) return _segments
            val functions = SegmentHelpFunctions<T>()
            var currentSegment = mutableListOf<T>()
            val clippedSegmentSize = functions.clipSegmentsSize(segmentsSize, minSegmentSize)
            if (data.size <= clippedSegmentSize) {
                currentSegment.addAll(data)
                functions.newSegment(_segments, currentSegment)
                return _segments
            }

            for (i in data.indices) {
                if (i % clippedSegmentSize == 0 && i != 0) {
                    // push, new, push if repeat mode
                    functions.push(currentSegment, data, i)
                    currentSegment = functions.newSegment(_segments, currentSegment)
                    if (type == SegmentsType.REPEAT) functions.push(currentSegment, data, i)
                } else {
                    // push
                    functions.push(currentSegment, data, i)
                }
                if (i == data.lastIndex) functions.newSegment(_segments, currentSegment)
            }

            return _segments.filter { it.isNotEmpty() }
        }
}

enum class SegmentsType {
    REPEAT, NON_REPEAT
}
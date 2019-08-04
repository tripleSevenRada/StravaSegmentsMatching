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

    // REPEAT
    // 1,2,3,4,5,6,7,8,9
    // 1,2,3-3,4,5-5,6,7-7,8,9
    // NON_REPEAT
    // 1,2,3,4,5,6,7,8,9
    // 1,2,3-4,5,6-7,8,9

    private val _segments = mutableListOf<List<T>>()
    val segments: List<List<T>>
        get() {
            if (data.isEmpty()) return _segments
            val functions = SegmentHelpFunctions<T>()
            var currentSegment = mutableListOf<T>()
            val clippedSegmentSize = functions.clipSegmentsSize(segmentsSize, minSegmentSize) - 1
            for (i in data.indices) {
                if (i == 0) {
                    functions.push(currentSegment, data, i)
                    if (i == data.lastIndex) currentSegment =
                            functions.newSegment(_segments, currentSegment)
                    continue
                }
                if (i % clippedSegmentSize == 0) {
                    functions.push(currentSegment, data, i)
                    currentSegment = functions.newSegment(_segments, currentSegment)
                    if (type == SegmentsType.REPEAT) functions.push(currentSegment, data, i)
                } else {
                    functions.push(currentSegment, data, i)
                }
                if (i == data.lastIndex) currentSegment = functions.newSegment(_segments, currentSegment)
            }
            return _segments
        }
}

enum class SegmentsType {
    REPEAT, NON_REPEAT
}
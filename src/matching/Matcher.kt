package matching

import dataClasses.Location
import dataClasses.LocationIndex
import geospatial.Haversine
import geospatial.MIN_SEGMENTS_SIZE
import geospatial.Segment
import geospatial.THRESHOLD_PARALLEL
import kotlinx.coroutines.*
import utils.ListSegment
import utils.SegmentsType.NON_REPEAT

class Matcher(private val segment: Segment, private val config: MatchingConfig) {

    fun getClosest(
            matchingCandidate: List<LocationIndex>,
            indexInSegment: Int,
            segment: Segment = this.segment
    ): LocationIndex? {
        val element = segment.getElements()[indexInSegment]
        return matchingCandidate.minBy {
            // it: LocationIndex
            Haversine.haversineInM(
                    it.location.lat,
                    it.location.lon,
                    element.lat,
                    element.lon)
        }
    }

    fun getMatchingResult(matchingCandidate: List<LocationIndex>,
                          segment: Segment = this.segment,
                          config: MatchingConfig = this.config): MatchingResult {
        var inliers = 0
        var outliers = 0
        for (index in segment.getElements().indices) {
            val referenceInSegment = segment.getElements()[index]
            val closest: LocationIndex = getClosest(matchingCandidate, index, segment) ?: continue
            val dist = Haversine.haversineInM(
                    referenceInSegment.lat,
                    referenceInSegment.lon,
                    closest.location.lat,
                    closest.location.lon)
            if (dist < config.closeEnough) inliers++ else outliers++
        }
        return MatchingResult(inliers, outliers)
    }

    fun getMatchingResultsParallel(matchingCandidate: List<LocationIndex>,
                                   segment: Segment = this.segment,
                                   config: MatchingConfig = this.config,
                                   scope: CoroutineScope): MatchingResult {

        // println("getMatchingResultsParallel")
        // println("matchingCandidate.size: ${matchingCandidate.size}")
        // println("segment.data.size: ${segment.data.size}")

        if (segment.data.size < THRESHOLD_PARALLEL)
            return getMatchingResult(matchingCandidate, segment, config)

        val chunks = ListSegment<Location>(segment.data, MIN_SEGMENTS_SIZE * 2,
                NON_REPEAT, MIN_SEGMENTS_SIZE).segments

        // println("chunks.size: ${chunks.size}")

        val results: MutableList<MatchingResult> = mutableListOf()
        runBlocking(scope.coroutineContext) {
            val deferredArray = Array<Deferred<MatchingResult>>(chunks.size) { index ->
                async(Dispatchers.Default) {
                    getMatchingResult(
                            matchingCandidate,
                            Segment(chunks[index]),
                            config)
                }
            }
            deferredArray.forEach { deferred ->
                val deferredMatchingResult: MatchingResult = deferred.await()
                results.add(deferredMatchingResult)
            }
        }
        return MatchingResult(results.sumBy { it.inliyers }, results.sumBy { it.outliyers })
    }
}

const val CLOSE_ENOUGH = 10.0
const val DEFAULT_RATIO = 0.94

data class MatchingConfig(val ratio: Double = DEFAULT_RATIO,
                          val closeEnough: Double = CLOSE_ENOUGH)

data class MatchingResult(val inliyers: Int,
                          val outliyers: Int)

fun MatchingResult.isValid(config: MatchingConfig): Boolean {
    return if (this.inliyers < 1 || this.outliyers < 0) false
    else {
        val expectedPercentInliers: Double = config.ratio * 100.0
        val percentInliers: Double = this.inliyers.toDouble() /
                ((this.inliyers + this.outliyers).toDouble() / 100.0)
        percentInliers > expectedPercentInliers
    }
}
package matching

import dataClasses.Location
import dataClasses.LocationIndex
import geospatial.*
import kotlinx.coroutines.*
import utils.ListSegment
import utils.SegmentsType.NON_REPEAT

class Matcher(private val segment: Segment, private val config: MatchingConfig) {

    companion object {
        @JvmStatic fun main(args: Array<String>) {
        }
    }

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

    fun getMatchingResultParallel(matchingCandidate: List<LocationIndex>,
                                  segment: Segment = this.segment,
                                  config: MatchingConfig = this.config,
                                  scope: CoroutineScope): MatchingResult {

        // println("getMatchingResultParallel")
        // println("matchingCandidate.size: ${matchingCandidate.size}")
        // println("segment.data.size: ${segment.data.size}")

        if (segment.getElements().size < THRESHOLD_PARALLEL)
            return getMatchingResult(matchingCandidate, segment, config)

        val chunks = ListSegment<Location>(segment.getElements(), MIN_SEGMENTS_SIZE * 2,
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

    fun areValidAsDirection(segment: Segment, candidate: List<LocationIndex>): Boolean {
        if(segment.getElements().isEmpty() || candidate.isEmpty()) return false
        val indexStartInSegment = 0;
        val indexEndInSegment = segment.getElements().lastIndex
        val locIndexOfStartSegmentInCandidate = getClosest(candidate, indexStartInSegment, segment)
        val locIndexOfEndSegmentInCandidate = getClosest(candidate, indexEndInSegment, segment)
        return if (locIndexOfStartSegmentInCandidate == null ||
                locIndexOfEndSegmentInCandidate == null) false
        else locIndexOfStartSegmentInCandidate.index < locIndexOfEndSegmentInCandidate.index
    }
}

fun MatchingResult.isValidAsPolygon(config: MatchingConfig): Boolean {
    return if (this.inliyers < 1 || this.outliyers < 0) false
    else {
        val expectedPercentInliyers: Double = config.ratio * 100.0
        val actualPercentInliyers: Double = this.inliyers.toDouble() /
                ((this.inliyers + this.outliyers).toDouble() / 100.0)
        actualPercentInliyers > expectedPercentInliyers
    }
}

const val CLOSE_ENOUGH = 22.0
const val DEFAULT_RATIO = 0.94

data class MatchingConfig(val ratio: Double = DEFAULT_RATIO,
                          val closeEnough: Double = CLOSE_ENOUGH)

data class MatchingResult(val inliyers: Int,
                          val outliyers: Int)

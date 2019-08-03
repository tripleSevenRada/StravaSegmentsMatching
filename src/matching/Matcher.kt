package matching

import dataClasses.LocationIndex
import geospatial.Haversine
import geospatial.Route
import geospatial.Segment

class Matcher(private val route: Route, private val segment: Segment, private val config: MatchingConfig) {

    // route and segments are expected fully built

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
        for (index in segment.getElements().indices){
            val referenceInSegment = segment.getElements()[index]
            val closest: LocationIndex = getClosest(matchingCandidate, index)?: continue
            val dist = Haversine.haversineInM(referenceInSegment.lat,
                    referenceInSegment.lon,
                    closest.location.lat,
                    closest.location.lon)
            if (dist < config.closeEnough) inliers ++ else outliers ++
        }
        return MatchingResult(inliers, outliers)
    }

    // TODO parallel getMatchingResult

}

const val CLOSE_ENOUGH = 5.0
const val OUTLIERS_RATIO = 0.94

data class MatchingConfig(val outliersRatio: Double = OUTLIERS_RATIO,
                          val closeEnough: Double = CLOSE_ENOUGH)
data class MatchingResult(val inliyers: Int,
                          val outliyers: Int)
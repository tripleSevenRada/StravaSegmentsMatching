package matching

import dataClasses.LocationIndex
import geospatial.Haversine
import geospatial.Route
import geospatial.Segment

class Matcher(val route: Route, val segment: Segment, config: MatchingConfig) {

    // route and segments are expected fully built

    private fun getClosest(
            matchingCandidate: List<LocationIndex>,
            segment: Segment,
            indexInSegment: Int
    ): LocationIndex? {
        val element = segment.getElements()[indexInSegment]
        return matchingCandidate.minBy{ // it: LocationIndex
            Haversine.haversineInM(
                    it.location.lat,
                    it.location.lon,
                    element.lat,
                    element.lon)
        }
    }
}

//TODO
const val CLOSE_ENOUGH = 5.0

data class MatchingConfig(val closeEnough: Double = CLOSE_ENOUGH)
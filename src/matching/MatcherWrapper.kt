package matching

import dataClasses.Location
import dataClasses.LocationIndex
import geospatial.Haversine
import geospatial.Route
import geospatial.Segment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin

class MatcherWrapper {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    public suspend fun cancelAndJoin() {
        job.cancelAndJoin()
    }

    public fun matchParallel(candidate: List<LocationIndex>,
                             segment: Segment, config: MatchingConfig): Boolean {

        val matcher = Matcher(segment, config)
        val result: MatchingResult = matcher
                .getMatchingResultParallel(matchingCandidate = candidate, scope = scope)

        return result.isValidAsPolygon(config) && matcher.areValidAsDirection(segment, candidate)
    }

    public fun getClosestIndexInRoute(route: Route, location: Location): Int =
            route.getElements().withIndex().minBy { indexedValue ->
                Haversine.haversineInM(
                        location.lat, location.lon,
                        indexedValue.value.lat,
                        indexedValue.value.lon)
            }?.index ?: 0

}
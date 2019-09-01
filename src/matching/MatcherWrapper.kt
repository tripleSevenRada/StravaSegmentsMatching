package matching

import dataClasses.LocationIndex
import geospatial.Segment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin

class MatcherWrapper {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    public suspend fun cancelAndJoin(){
        job.cancelAndJoin()
    }

    public fun matchParallel(candidate: List<LocationIndex>,
                             segment: Segment, config: MatchingConfig): Boolean{

        val matcher = Matcher(segment, config)
        val result: MatchingResult = matcher
                .getMatchingResultParallel(matchingCandidate = candidate, scope = scope)

        return result.isValidAsPolygon(config) && matcher.areValidAsDirection(segment, candidate)
    }
}
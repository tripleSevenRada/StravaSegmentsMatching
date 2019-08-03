package test

import geospatial.Discretizer
import geospatial.Route
import geospatial.Segment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.junit.Test

class TestRunDiscretizeMatch {

    @Test
    fun test_run_discretize_match_on_real_data() {
        val job = Job()
        val scope = CoroutineScope(Dispatchers.Default + job)
        val routesRaw = listOf("/home/radim/Dropbox/outFit/testMatchingCandidates/route.gpx",
                "/home/radim/Dropbox/outFit/testMatchingCandidates/route2.gpx",
                "/home/radim/Dropbox/outFit/testMatchingCandidates/route3.gpx")
        val routesDiscretized = mutableListOf<Route>()
        routesRaw.forEach {
            val data = parseGPX(it)
            println("CANDIDATES TEST DISCRETIZED: raw route data size: ${data.size}")
            val routeRaw = Route(data)
            val routeDiscretizedLocations = Discretizer().discretizeInParallel(routeRaw, scope)
            val routeDiscretized = Route(routeDiscretizedLocations)
            routesDiscretized.add(routeDiscretized)
            println("CANDIDATES TEST DISCRETIZED: discretized route data size: ${routeDiscretized.getElements().size}")
        }
        val segmentRaw = parseGPX("/home/radim/Dropbox/outFit/testMatchingCandidates/segment.gpx")
        val segmentDiscretized = Segment(Discretizer().discretizeInParallel(Segment(segmentRaw),scope))
        val box = segmentDiscretized.box
        routesDiscretized.forEach { route ->
            val rawLocationsForCandidates = route.getPointsWithinBox(box)
            val candidates = route.getMatchingCandidates(rawLocationsForCandidates)
            println("CANDIDATES TEST DISCRETIZED:")
            println("CANDIDATES TEST DISCRETIZED: candidates.size = ${candidates.getCandidates().size}")
            candidates.getCandidates().forEach {
                val startIndex = it[0].index
                val endIndex = it[it.lastIndex].index
                println("startIndex: $startIndex")
                println("endIndex: $endIndex")
                assert(assertContinuity(it))
            }
        }
    }
}

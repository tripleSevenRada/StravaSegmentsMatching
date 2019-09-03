package test

import dataClasses.Location
import geospatial.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import matching.*
import org.junit.Test
import kotlin.test.assertEquals

class MatchingTest {

    @Test
    fun test_get_closest_real_data() {

        val job = Job()
        val scope = CoroutineScope(Dispatchers.Default + job)

        // route related
        val data = parseGPX("/home/radim/Dropbox/outFit/testMatchingCandidates/route.gpx")
        val routeRaw = Route(data)
        val routeDiscretizedLocations = Discretizer().discretizeInParallel(routeRaw, scope)
        // route
        val routeDiscretized = Route(routeDiscretizedLocations)

        // segmentRelated
        val avgReferenceLocation = Location(data.sumByDouble { it.lat } / data.size,
                data.sumByDouble { it.lon } / data.size)
        val segmentMockLocations = mutableListOf<Location>()
        val increment = 0.001
        for (i in 0..50) {
            segmentMockLocations.add(Location(avgReferenceLocation.lat + (i * increment),
                    avgReferenceLocation.lon + (i * increment)))
        }
        // segment
        val segmentMock = Segment(segmentMockLocations)

        val box = segmentMock.box

        val rawLocationsForCandidates = routeDiscretized.getPointsWithinBox(box)
        val candidates = routeDiscretized.getMatchingCandidates(rawLocationsForCandidates)

        val matcher = Matcher(segmentMock, MatchingConfig())

        candidates.getCandidates().forEach { matchingCandidate ->

            for (indexInSegment in segmentMock.getElements().indices) {

                // get closest
                val closestGotByTestedFunction = matcher.getClosest(matchingCandidate, indexInSegment)

                // imperative get closest impl
                var closest: Location? = null
                var minDist: Double = Double.MAX_VALUE
                val currentReferenceLocationInSegment = segmentMock.getElements()[indexInSegment]
                matchingCandidate.forEach {
                    val dist = Haversine.haversineInM(
                            currentReferenceLocationInSegment.lat,
                            currentReferenceLocationInSegment.lon,
                            it.location.lat,
                            it.location.lon
                    )
                    if (dist < minDist) {
                        minDist = dist
                        closest = it.location
                    }
                }

                // println("comparison lat: ${closestGotByTestedFunction?.location?.lat} | ${closest?.lat}")
                // println("comparison lon: ${closestGotByTestedFunction?.location?.lon} | ${closest?.lon}")

                assertEquals(closestGotByTestedFunction?.location?.lat, closest?.lat)
                assertEquals(closestGotByTestedFunction?.location?.lon, closest?.lon)

            }
        }
    }

    private fun getRouteRaw(): Route {
        val dataRoute = parseGPX("/home/radim/Dropbox/outFit/testMatchingCandidates/route.gpx")
        return Route(dataRoute)
    }

    private fun getSegmentRaw(): Segment {
        val dataSegment = parseGPX("/home/radim/Dropbox/outFit/testMatchingCandidates/segment.gpx")
        return Segment(dataSegment)
    }

    private fun getMatchingCandidates(route: Route, segment: Segment): MatchingCandidates {
        val box = segment.box
        val locationsForCandidates = route.getPointsWithinBox(box)
        return route.getMatchingCandidates(locationsForCandidates)
    }

    @Test
    fun test_getMatchingResult() {
        val routeRaw = getRouteRaw()
        val segmentRaw = getSegmentRaw()
        val candidates = getMatchingCandidates(routeRaw, segmentRaw)

        val closeEnoughValues = arrayOf<Double>(0.0, 5000000.0)
        val ins = arrayOf<Int>(0, segmentRaw.getElements().size)
        val outs = arrayOf<Int>(segmentRaw.getElements().size, 0)

        candidates.getCandidates().forEach { candidate ->
            val matchingResult = Matcher(segmentRaw, MatchingConfig()).getMatchingResult(candidate)
            println("matchingResult: $matchingResult")

            assertEquals(segmentRaw.getElements().size,
                    matchingResult.inliyers + matchingResult.outliyers)

            //
            //
            val config = MatchingConfig()
            var insCompare = 0
            var outsCompare = 0

            segmentRaw.getElements().forEach { segmentLoc ->
                var distMin = Double.MAX_VALUE
                candidate.forEach { candidateLocInd ->
                    val dist = Haversine.haversineInM(
                            segmentLoc.lat,
                            segmentLoc.lon,
                            candidateLocInd.location.lat,
                            candidateLocInd.location.lon
                    )
                    if (dist < distMin) distMin = dist
                }
                if (distMin < config.closeEnough) insCompare++ else outsCompare++
            }
            assertEquals(insCompare, matchingResult.inliyers)
            assertEquals(outsCompare, matchingResult.outliyers)
            //
            //

            for (i in closeEnoughValues.indices) {
                val matchingResultNow = Matcher(segmentRaw, MatchingConfig(0.94, closeEnoughValues[i]))
                        .getMatchingResult(candidate)
                assertEquals(ins[i], matchingResultNow.inliyers)
                assertEquals(outs[i], matchingResultNow.outliyers)
            }
        }
    }

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    @Test
    fun test_parallel_vs_non_parallel() {
        val routeRaw = getRouteRaw()
        val segmentRaw = getSegmentRaw()
        val candidates = getMatchingCandidates(routeRaw, segmentRaw)

        candidates.getCandidates().forEach { candidate ->
            val startNP = System.currentTimeMillis()
            val matchingResultNP = Matcher(segmentRaw, MatchingConfig()).getMatchingResult(candidate)
            val milisNP = System.currentTimeMillis() - startNP
            val startP = System.currentTimeMillis()
            val matchingResultP = Matcher(segmentRaw, MatchingConfig()).getMatchingResultParallel(
                    candidate,
                    segmentRaw,
                    MatchingConfig(),
                    scope)
            val milisP = System.currentTimeMillis() - startP
            assertEquals(matchingResultNP.inliyers, matchingResultP.inliyers)
            assertEquals(matchingResultNP.outliyers, matchingResultP.outliyers)
            println("test_parallel_vs_non_parallel: NP: $milisNP | P: $milisP")
        }
    }

    @Test
    fun testIsValid(){
        val inliers = arrayOf(97,100,80,200,10,1,1,1,100,0)
        val outliers = arrayOf(3,3,6,1,1,10,10,100,0,0)
        val expectedValid = arrayOf(true,true,false,true,false,false,false,false,true,false)
        val config = MatchingConfig(0.94,10.0)
        for(i in 0..9) {
            val result = MatchingResult(inliers[i], outliers[i])
            val valid = result.isValidAsPolygon(config)
            assertEquals(valid, expectedValid[i])
        }
    }

    @Test
    fun testGetClosestIndexInRoute(){
        val locations : List<Location> =
                parseGPX("/home/radim/Dropbox/outFit/segmentsTestData/realGPXMocks/segments/LabeLysa/noPass/Labe-Lysa-Mismatch.gpx")
        val route = Route(locations)
        val referenceLocation = Location(50.00, 14.00)
        val index = MatcherWrapper().getClosestIndexInRoute(route, referenceLocation)

        var compare = -1
        var closestSeen = Double.MAX_VALUE
        for (i in route.getElements().indices){
            val dist = Haversine.haversineInM(referenceLocation.lat, referenceLocation.lon,
            route.getElements()[i].lat, route.getElements()[i].lon)
            if (dist < closestSeen) {
                closestSeen = dist
                compare = i
            }
        }
        assertEquals(compare, index)
    }
}
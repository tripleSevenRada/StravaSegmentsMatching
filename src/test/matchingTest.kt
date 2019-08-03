package test

import dataClasses.Location
import geospatial.Discretizer
import geospatial.Haversine
import geospatial.Route
import geospatial.Segment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import matching.Matcher
import matching.MatchingConfig
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

        val matcher = Matcher(routeDiscretized, segmentMock, MatchingConfig())

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
                    if(dist < minDist){
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

    @Test
    fun test_getMatchingResult(){

        // route related
        val dataRoute = parseGPX("/home/radim/Dropbox/outFit/testMatchingCandidates/route.gpx")
        val routeRaw = Route(dataRoute)

        // segment related
        val dataSegment = parseGPX("/home/radim/Dropbox/outFit/testMatchingCandidates/segment.gpx")
        val segmentRaw = Segment(dataSegment)
        val box = segmentRaw.box

        val rawLocationsForCandidates = routeRaw.getPointsWithinBox(box)
        val candidates = routeRaw.getMatchingCandidates(rawLocationsForCandidates)

        val closeEnoughValues = arrayOf<Double>(0.0, 5000000.0)
        val ins = arrayOf<Int>(0, segmentRaw.getElements().size)
        val outs = arrayOf<Int>(segmentRaw.getElements().size, 0)

        candidates.getCandidates().forEach{
            val matchingResult = Matcher(routeRaw, segmentRaw, MatchingConfig()).getMatchingResult(it)
            println("matchingResult: $matchingResult")
            assertEquals(segmentRaw.getElements().size,
                    matchingResult.inliyers + matchingResult.outliyers)

            for (i in closeEnoughValues.indices) {
                val matchingResultNow = Matcher(routeRaw, segmentRaw, MatchingConfig(0.94, closeEnoughValues[i]))
                        .getMatchingResult(it)
                assertEquals(ins[i], matchingResultNow.inliyers)
                assertEquals(outs[i], matchingResultNow.outliyers)
            }
        }
    }
}
package test

import dataClasses.Location
import dataClasses.LocationIndex
import dataClasses.Valid
import geospatial.Route
import geospatial.Segment
import junit.framework.Assert.assertEquals
import org.junit.Test

val segmentData = listOf(
        Location(9.0, 2.0),
        Location(8.0, 3.0),
        Location(8.0, 5.0),
        Location(6.0, 5.0),
        Location(5.0, 6.0),
        Location(5.0, 7.0),
        Location(5.0, 9.0),
        Location(5.0, 11.0),
        Location(3.0, 11.0),
        Location(2.0, 10.0),
        Location(1.0, 10.0)
)
val routeData = listOf(
        Location(7.0, 2.0),
        Location(6.0, 4.0),
        Location(4.0, 1.0),
        Location(3.0, 1.0),
        Location(3.0, 4.0),
        Location(3.0, 5.0),
        Location(3.0, 6.0),
        Location(1.0, 6.0),
        Location(0.0, 6.0),
        Location(0.0, 7.0),
        Location(1.0, 7.0),
        Location(3.0, 7.0),
        Location(4.0, 8.0),
        Location(4.0, 9.0)
)

class PointsWithinBoxTest {
    @Test
    fun justSanityTestBox() {
        val segment = Segment(segmentData)
        val box = segment.box
        assert(box is Valid)
        if (box is Valid) {
            assertEquals(9.0, box.maxLat, 0.001)
            assertEquals(1.0, box.minLat, 0.001)
            assertEquals(2.0, box.minLon, 0.001)
            assertEquals(11.0, box.maxLon, 0.001)
        }
    }

    @Test
    fun testNumberOfPointsWithinAndTheirLocationsIndex() {
        val segment = Segment(segmentData)
        val box = segment.box
        assert(box is Valid)
        val route = Route(routeData)
        val pointsWithin = route.getPointsWithinBox(box)
        assertEquals(10, pointsWithin.size)

        assertEquals(pointsWithin[0].index, 0)
        assertEquals(pointsWithin[1].index, 1)
        assertEquals(pointsWithin[2].index, 4)
        assertEquals(pointsWithin[3].index, 5)
        assertEquals(pointsWithin[4].index, 6)
        assertEquals(pointsWithin[6].index, 10)
        assertEquals(pointsWithin[7].index, 11)
        assertEquals(pointsWithin[8].index, 12)
        assertEquals(pointsWithin[9].index, 13)
    }

    @Test
    fun testMatchingCandidates() {
        val segment = Segment(segmentData)
        val box = segment.box
        assert(box is Valid)
        val route = Route(routeData)
        val pointsWithin = route.getPointsWithinBox(box)
        val candidates = route.getMatchingCandidates(pointsWithin)

        assertEquals(3, candidates.getCandidates().size)
        assertEquals(2, candidates.getCandidates()[0].size)
        assertEquals(4, candidates.getCandidates()[1].size)
        assertEquals(4, candidates.getCandidates()[2].size)

        assertEquals(0, candidates.getCandidates()[0][0].index)
        assertEquals(1, candidates.getCandidates()[0][1].index)

        assertEquals(4, candidates.getCandidates()[1][0].index)
        assertEquals(5, candidates.getCandidates()[1][1].index)
        assertEquals(6, candidates.getCandidates()[1][2].index)
        assertEquals(7, candidates.getCandidates()[1][3].index)

        assertEquals(10, candidates.getCandidates()[2][0].index)
        assertEquals(11, candidates.getCandidates()[2][1].index)
        assertEquals(12, candidates.getCandidates()[2][2].index)
        assertEquals(13, candidates.getCandidates()[2][3].index)
    }

    @Test
    fun testContinuityInMatchingCandidatesRealData() {
        // /home/radim/Dropbox/outFit/testMatchingCandidates/segment.gpx
        val routes = listOf("/home/radim/Dropbox/outFit/testMatchingCandidates/route.gpx",
                "/home/radim/Dropbox/outFit/testMatchingCandidates/route2.gpx",
                "/home/radim/Dropbox/outFit/testMatchingCandidates/route3.gpx")
        routes.forEach {
            val route = Route(parseGPX(it))
            val segment = Segment(parseGPX("/home/radim/Dropbox/outFit/testMatchingCandidates/segment.gpx"))
            val box = segment.box
            val rawLocationsForCandidates = route.getPointsWithinBox(box)
            val candidates = route.getMatchingCandidates(rawLocationsForCandidates)
            println("CANDIDATES TEST:")
            println("CANDIDATES TEST: candidates.size = ${candidates.getCandidates().size}")
            candidates.getCandidates().forEach {
                assert(assertContinuity(it))
            }
        }
    }
}

fun assertContinuity(candidate: List<LocationIndex>): Boolean {
    if (candidate.size < 2) return true
    else {
        var last = candidate[0].index
        for (i in 1..candidate.lastIndex) {
            val now = candidate[i].index
            if (now != last + 1) return false
            last = now
        }
    }
    return true
}
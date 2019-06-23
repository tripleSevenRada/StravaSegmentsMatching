package test

import dataClasses.Location
import geospatial.DISCRETIZE_DISTANCE
import geospatial.Discretizer
import geospatial.Haversine
import geospatial.Route
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.util.*

fun Location.toTrkptRecord(): String = "<trkpt lat=\"${this.lat}\" lon=\"${this.lon}\"></trkpt>"
fun Location.toWptRecord(): String = "<wpt lat=\"${this.lat}\" lon=\"${this.lon}\"></wpt>"

class DiscretizerTests {
    @Test
    fun testRunDiscretizer() {
        val route = Route(routeData)
        Discretizer().discretize(route)
    }

    @Test
    fun testEmpty() {
        val route = Route(listOf())
        val discretized = Discretizer().discretize(route)
        assertEquals(0, discretized.size)
    }

    @Test
    fun testSingle() {
        val route = Route(listOf(Location(1.1, 1.2)))
        val discretized = Discretizer().discretize(route)
        assertEquals(1, discretized.size)
        assertEquals(discretized[0].lat, 1.1)
        assertEquals(discretized[0].lon, 1.2)
    }

    // 2 points, 2,49m
    // latitude    longitude   name    desc
    // 49.998485362    13.995538145    Track
    // 49.998485578    13.995573014
    @Test
    fun testTwoUnderDiscretizationDist() {
        val route = Route(listOf(Location(49.998485362, 13.995538145), Location(49.998485578, 13.995573014)))
        val discretized = Discretizer().discretize(route)
        assertEquals(2, discretized.size)
    }

    // 2 points 4.2m
    // latitude    longitude   name    desc
    // 49.998473293    13.995537810    Track#2 
    // 49.998472862    13.995596819      
    @Test
    fun testTwoOverDiscretizationDist() {
        val route = Route(listOf(Location(49.998473293, 13.995537810), Location(49.998472862, 13.995596819)))
        val discretized = Discretizer().discretize(route)
        assertEquals(3, discretized.size)
    }

    @Test
    fun generateRandomRouteDiscretizeItAssertDistances() {
        repeat(10) {
            val rnd = Random()
            val routeList = mutableListOf<Location>()
            repeat(20) {
                val location = Location(-0.5 + rnd.nextDouble(), -0.5 + rnd.nextDouble())
                routeList.add(location)
            }
            val route = Route(routeList)
            val discretized = Discretizer().discretize(route)

            assertEquals(routeList[0].lat, discretized[0].lat)
            assertEquals(routeList[0].lon, discretized[0].lon)

            assertEquals(routeList[routeList.lastIndex].lat, discretized[discretized.lastIndex].lat)
            assertEquals(routeList[routeList.lastIndex].lon, discretized[discretized.lastIndex].lon)

            for (i in 0..discretized.size - 2) {
                val dist = Haversine.haversineInM(
                        discretized[i].lat,
                        discretized[i].lon,
                        discretized[i + 1].lat,
                        discretized[i + 1].lon)
                assert(dist <= DISCRETIZE_DISTANCE)
            }
        }
    }

    /*
    type	latitude	longitude	name	desc
    T	50.057510963	14.506964188	forDiscretization
    T	50.056312417	14.506921273
    T	50.056408853	14.503766995
    T	50.056257311	14.503874284
    T	50.056257311	14.504088860
    T	50.056271087	14.504303437
    T	50.056133321	14.504496556
    T	50.055940448	14.504732590
    T	50.055844012	14.504818421
    T	50.055596030	14.505161744
    */

    @Test
    fun printforDiscretizationAsTrckpts() {
        val routeList = listOf<Location>(
                Location(50.057510963, 14.506964188),
                Location(50.056312417, 14.506921273),
                Location(50.056408853, 14.503766995),
                Location(50.056257311, 14.503874284),
                Location(50.056257311, 14.504088860),
                Location(50.056271087, 14.504303437),
                Location(50.056133321, 14.504496556),
                Location(50.055940448, 14.504732590),
                Location(50.055844012, 14.504818421),
                Location(50.055596030, 14.505161744)
        )
        val route = Route(routeList)
        val discretized = Discretizer().discretize(route)
        discretized.forEach {
            //println(it.toTrkptRecord())
        }
    }
}
package test

import dataClasses.Box
import dataClasses.Invalid
import dataClasses.Location
import dataClasses.Valid
import geospatial.Segment
import junit.framework.Assert.assertEquals
import junit.framework.Assert.fail
import org.junit.Test

class ComputeBoxTest {

    private val mock1 = listOf(Location(1.0,1.0))
    @Test
    fun testComputeBox1() {
        val box: Box = Segment(mock1).box
        if(box is Invalid){
            fail("1 returned Invalid")
        } else if (box is Valid){ // always true
            assertEquals(1.0, box.maxLat,0.001)
            assertEquals(1.0, box.minLat,0.001)
            assertEquals(1.0, box.maxLon,0.001)
            assertEquals(1.0, box.minLon, 0.001)
        }
    }
    private val mock2 = listOf<Location>()
    @Test
    fun testComputeBox2() {
        try {
            val box: Box = Segment(mock2).box
            if(box is Valid) {
                fail("2 returned Valid for empty")
            }
        } catch (re: RuntimeException){
        }
    }
    private val mock3 = listOf<Location>(
            Location(0.0, 0.0),
            Location(0.0, 0.0)
            )
    @Test
    fun testComputeBox3() {
        val box: Box = Segment(mock3).box
        if (box is Invalid) fail("3.1 fail")
        else if (box is Valid){
            assertEquals(box.maxLon, 0.0, 0.001)
            assertEquals(box.minLon, 0.0, 0.001)
            assertEquals(box.maxLat, 0.0, 0.001)
            assertEquals(box.minLat, 0.0, 0.001)
        }
    }
    private val mock4 = listOf<Location>(
            Location(-1.0, -2.0),
            Location(0.0, 0.0),
            Location(1.0, 1.0),
            Location(2.0, 3.0)
    )
    @Test
    fun testComputeBox4() {
        val box: Box = Segment(mock4).box
        if (box is Invalid) fail("4.1 fail")
        else if (box is Valid){
            assertEquals(box.maxLon, 3.0, 0.001)
            assertEquals(box.minLon, -2.0, 0.001)
            assertEquals(box.maxLat, 2.0, 0.001)
            assertEquals(box.minLat, -1.0, 0.001)
        }
    }

    // must fail
    private val mock5 = listOf<Location>(
            Location(-91.0, -2.0),
            Location(0.0, 0.0),
            Location(1.0, 1.0),
            Location(2.0, 3.0)
    )
    private val mock6 = listOf<Location>(
            Location(91.0, -2.0),
            Location(0.0, 0.0),
            Location(1.0, 1.0),
            Location(2.0, 3.0)
    )
    private val mock7 = listOf<Location>(
            Location(-1.0, -182.0),
            Location(0.0, 0.0),
            Location(1.0, 1.0),
            Location(-21.0, 3.0)
    )
    private val mock8 = listOf<Location>(
            Location(-1.0, 182.0),
            Location(0.0, 0.0),
            Location(1.0, 1.0),
            Location(2.0, 3.0)
    )
    private val failMocks = listOf(mock5, mock6, mock7, mock8)
    @Test
    fun testComputeBoxFail() {
        failMocks.forEach {
            try {
                val box: Box = Segment(it).box
                if (box is Valid) fail("testComputeBoxFail")
            } catch (re: RuntimeException){}
        }
    }
}
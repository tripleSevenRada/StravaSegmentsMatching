package test

import geospatial.getLonMargin
import org.junit.Test
import java.util.*

class TestLonMargin{

    private val random = Random()

    @Test
    fun testPrintRandomValues(){
        repeat(100){
            val degreesRatio = random.nextDouble()
            val sign = random.nextBoolean()
            var degrees = degreesRatio * 90.0
            if(sign) degrees = - degrees
            println("degrees: $degrees value: ${getLonMargin(degrees)}")
        }
    }

}
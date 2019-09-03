package test

import com.fasterxml.jackson.databind.ObjectMapper
import dataClasses.Location
import org.junit.Test
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import test.gpx_to_json_requested_route.ActivityType
import test.gpx_to_json_requested_route.LatLonPair
import test.gpx_to_json_requested_route.MatchingScenario
import test.gpx_to_json_requested_route.RequestedRoute
import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

fun parseGPX(path: String): List<Location> {
    val locations = mutableListOf<Location>()
    fun readXml(): Document {
        val xmlFile = File(path)
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val xmlInput = InputSource(StringReader(xmlFile.readText().trim().replaceFirst("^([\\W]+)<", "<")))
        val doc = dBuilder.parse(xmlInput)
        return doc
    }

    val doc = readXml()

    /*
    <?xml version="1.0"?>
    <gpx version="1.0" creator="Viking -- http://viking.sf.net/"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns="http://www.topografix.com/GPX/1/0"
    xsi:schemaLocation="http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd">
    <trk hidden="hidden">
    <name>Labe - Lysa - Mismatch</name>
    <trkseg>
    <trkpt lat="50.168174149203963" lon="14.855387187347411">
    </trkpt>
    */

    val nodeListTrckpt: NodeList = doc.getElementsByTagName("rtept")//trkpt
    for (i in 0 until nodeListTrckpt.length) {
        val node = nodeListTrckpt.item(i)
        val nodeElem = node as Element
        val latS = nodeElem.getAttribute("lat")
        val lonS = nodeElem.getAttribute("lon")
        val latD = latS.toDouble()
        val lonD = lonS.toDouble()
        locations.add(Location(latD, lonD))
    }
    return locations
}

class ParsedGPXTests {

    @Test
    fun sanityTestReadGPX() {
        val nodeList = parseGPX("/home/radim/Dropbox/outFit/segmentsTestData/realGPXMocks/segments/LabeLysa/noPass/Labe-Lysa-Mismatch.gpx")
        println("trackpoints: ${nodeList.size}")

        assert(nodeList.size == 35)

        assert(nodeList[0].lat == 50.168174149203963)
        assert(nodeList[0].lon == 14.855387187347411)

        assert(nodeList[nodeList.lastIndex].lat == 50.184060056634998)
        assert(nodeList[nodeList.lastIndex].lon == 14.859549975738524)
    }

    @Test
    fun readGPXSaveAsJSONUsingJackson() {
        val mapper = ObjectMapper()
        val pathRoot = "/home/radim/Segments/routes_with_segments/"
        File(pathRoot + "gpx/nizborClimb/").walk().forEach {
            if (it.isFile) {
                val nameIn = it.name
                println("reading: ${it.name}")
                println("path: ${it.absolutePath}")
                val activityType = ActivityType.RIDE
                val matchingScenario = MatchingScenario.ROUTE
                val nodeList = parseGPX(it.absolutePath)
                val requestedRoute: RequestedRoute = RequestedRoute()
                requestedRoute.type = activityType
                requestedRoute.matchingScenario = matchingScenario
                requestedRoute.token = "b0d77cdd6000365506e7149b77283eb064f36982"
                val locations = mutableListOf<LatLonPair>()
                nodeList.forEach { loc -> locations.add(LatLonPair(loc.lat, loc.lon)) }
                requestedRoute.locations = locations
                val jsonString = mapper.writeValueAsString(requestedRoute)
                val outFile = File(pathRoot + "/json/" + activityType.label + "/nizborClimb/" + matchingScenario.label + "/"
                        + nameIn.subSequence(0, nameIn.lastIndexOf('.')) + ".json")
                println("writing: ${outFile.name}")
                println("path: ${outFile.absolutePath}")
                outFile.printWriter().use { out ->
                    out.println(jsonString)
                }
            }
        }
    }
}
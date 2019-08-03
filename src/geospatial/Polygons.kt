package geospatial

import dataClasses.Location
import dataClasses.Box
import dataClasses.Invalid
import dataClasses.Valid
import dataClasses.LocationIndex
import interfaces.Discretizable
import java.lang.RuntimeException

// https://en.wikipedia.org/wiki/Discretization
// http://www.longitudestore.com/how-big-is-one-gps-degree.html

fun isWithinBox(location: Location, box: Box): Boolean = when (box) {
    is Invalid -> false
    is Valid -> box.covers(location)
}

const val latMargin = 0.000275 // approx. 30m
const val lonMargin0_30 = 0.000289 // approx. 30m
const val lonMargin30_60 = 0.000397
const val lonMargin60_90 = 0.001024

fun getLonMargin(lon: Double): Double = when (lon) {
    in -30.0..30.0 -> lonMargin0_30
    in -60.0..60.0 -> lonMargin30_60
    else -> lonMargin60_90
}

sealed class Polygon (val data: List<Location>)

class Route(data: List<Location>) : Discretizable, Polygon(data) {

    // 1
    fun getPointsWithinBox(box: Box): List<LocationIndex> {
        val locIndList = mutableListOf<LocationIndex>()
        if (box is Invalid) return locIndList
        for (i in data.indices) {
            if (isWithinBox(data[i], box)) locIndList.add(LocationIndex(data[i], i))
        }
        return locIndList
    }

    // 2
    fun getMatchingCandidates(rawList: List<LocationIndex>): MatchingCandidates{
        val candidates = MatchingCandidates()
        if (rawList.isEmpty())return candidates
        candidates.add(rawList[0])
        var index = rawList[0].index
        if (rawList.size > 1){
            for (i in 1..rawList.lastIndex){
                val current = rawList[i]
                if (current.index > index + 1) candidates.makeNew()
                index = current.index
                candidates.add(current)
            }
        }
        candidates.finish()
        return candidates
    }

    //Implementation Discretizable

    override fun getElements(): List<Location> = data
}

class Segment(data: List<Location>) : Discretizable, Polygon(data) {

    val box: Box

    // @Throws (RuntimeException::class)
    init {
        box = computeBox(data).addMargin()
        if (box is Invalid) throw RuntimeException("Invalid box")
    }

    private fun computeBox(locations: List<Location>): Box {
        val boxAsList = listOf<Double?>(
                (locations.minBy { it.lat }?.lat),
                locations.maxBy { it.lat }?.lat,
                locations.minBy { it.lon }?.lon,
                locations.maxBy { it.lon }?.lon
        )

        fun isValidLat(value: Double): Boolean = (value < 90.0 && value > -90.0)
        fun isValidLon(value: Double): Boolean = (value < 180.0 && value > -180.0)
        val invalidLat = "Contains invalid lat"
        val invalidLon = "Contains invalid lon"

        return when {
            boxAsList.any { it == null } -> Invalid("Empty list of Locations?")
            boxAsList[0]?.let { !isValidLat(it) } ?: true -> Invalid(invalidLat)
            boxAsList[1]?.let { !isValidLat(it) } ?: true -> Invalid(invalidLat)
            boxAsList[2]?.let { !isValidLon(it) } ?: true -> Invalid(invalidLon)
            boxAsList[3]?.let { !isValidLon(it) } ?: true -> Invalid(invalidLon)
            else -> Valid(
                    boxAsList[0] ?: 0.0, boxAsList[1] ?: 0.0, boxAsList[2] ?: 0.0, boxAsList[3] ?: 0.0
            )
        }
    }

    private fun Box.addMargin(): Box{
        return when (this){
            is Invalid -> this
            is Valid -> Valid(
                    this.minLat - latMargin,
                    this.maxLat + latMargin,
                    this.minLon - getLonMargin(this.minLon),
                    this.maxLon + getLonMargin(this.maxLon)
            )
        }
    }

    //Implementation Discretizable

    override fun getElements(): List<Location> = data
}

class MatchingCandidates{

    private var candidates = mutableListOf<List<LocationIndex>>()
    private var currentCandidate = mutableListOf<LocationIndex>()

    fun getCandidates(): List<List<LocationIndex>> = candidates
    fun add(locationIndex: LocationIndex) =  currentCandidate.add(locationIndex)
    fun makeNew(){
        candidates.add(currentCandidate)
        currentCandidate = mutableListOf<LocationIndex>()
    }
    fun finish() {
        if (currentCandidate.isNotEmpty()) candidates.add(currentCandidate)
    }
}
package geospatial

import dataClasses.Elevation
import dataClasses.Location
import interfaces.Discretizable
import kotlinx.coroutines.*
import utils.ListSegment
import utils.SegmentsType

const val DISCRETIZE_DISTANCE = 3.0
//--------------------------------
const val THRESHOLD_PARALLEL = 46
const val MIN_SEGMENTS_SIZE = 26
//--------------------------------

// https://proandroiddev.com/demystifying-kotlin-coroutines-6fe1f410570b
class Discretizer {

    private lateinit var start: LocationNode
    private lateinit var end: LocationNode

    private fun linkInput(input: Discretizable) {
        var last: LocationNode? = null
        for (i in input.getElements().indices.reversed()) {
            val instance = LocationNode(input.getElements()[i], last)
            if (last == null) end = instance
            if (i == 0) start = instance
            last = instance
        }
    }

    suspend fun discretize(input: Discretizable, discretizeDistance: Double): List<Location> {
        if (input.getElements().size < 2) return input.getElements()
        linkInput(input)
        var currentNode = start
        var nextNode = start.next

        suspend fun discretizePair(pair: Pair<LocationNode, LocationNode>) {
            val dist = Haversine.haversineInM(
                    pair.first.location.lat,
                    pair.first.location.lon,
                    pair.second.location.lat,
                    pair.second.location.lon
            )
            if (dist < discretizeDistance) return
            else {
                // instantiate new LocationNode
                val elevationInstance  = if (pair.first.location.elevation is Elevation.Value
                        && pair.second.location.elevation is Elevation.Value){
                    val insertedNodeElevation = ((pair.first.location.elevation as Elevation.Value).elevation
                            + (pair.second.location.elevation as Elevation.Value).elevation) / 2.0
                    Elevation.Value(insertedNodeElevation)
                } else Elevation.NoValue()

                val insertedNode = LocationNode(
                        Location(
                                (pair.first.location.lat + pair.second.location.lat) / 2.0,
                                (pair.first.location.lon + pair.second.location.lon) / 2.0,
                                elevationInstance

                        ),
                        pair.second
                )
                // change pointer to next from first node
                pair.first.next = insertedNode
                // discretize recursively both new pairs
                discretizePair(Pair(pair.first, insertedNode)) // left to inserted
                discretizePair(Pair(insertedNode, pair.second)) // right to inserted
            }
        }

        // MAIN procedure
        while (nextNode != null) {
            discretizePair(Pair(currentNode, nextNode))
            currentNode = nextNode
            nextNode = nextNode.next
        }

        return traverseStartEnd()
    }

    private fun traverseStartEnd(): List<Location> {
        val list = mutableListOf<Location>()
        var current = start
        var next = start.next
        while (true) {
            if (next == null) {
                list.add(current.location)
                break
            } else {
                list.add(current.location)
                current = next
                next = current.next
            }
        }
        return list
    }

    fun discretizeInParallel(input: Discretizable,
                             scope: CoroutineScope,
                             discretizeDistance: Double = DISCRETIZE_DISTANCE): List<Location> {
        val locations = input.getElements()
        val result = mutableListOf<Location>()
        if (locations.size < THRESHOLD_PARALLEL) {
            runBlocking(scope.coroutineContext) {
                result.addAll(discretize(input, discretizeDistance))
            }
            return result
        }

        val chunks = ListSegment<Location>(locations, MIN_SEGMENTS_SIZE, SegmentsType.REPEAT).segments

        runBlocking(scope.coroutineContext) {
            val deferredArray = Array<Deferred<List<Location>>>(chunks.size) { index ->
                async(Dispatchers.Default) { Discretizer().discretize(Route(chunks[index]), discretizeDistance) }
            }
            var first = true
            deferredArray.forEach { deferred ->
                val deferredList = deferred.await()
                if (!first) {
                    val deferredListFirstElementDeleted = deferredList.subList(1, deferredList.size)
                    result.addAll(deferredListFirstElementDeleted)
                } else {
                    result.addAll(deferredList)
                }
                if (first) first = false
            }
        }
        return result
    }

    data class LocationNode(val location: Location, var next: LocationNode?)
}
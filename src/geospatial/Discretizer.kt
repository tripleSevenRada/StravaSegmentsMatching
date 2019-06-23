package geospatial

import dataClasses.Location
import interfaces.Discretizable

const val DISCRETIZE_DISTANCE = 3.0

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

    fun discretize(input: Discretizable): List<Location> {
        if (input.getElements().size < 2) return input.getElements()
        linkInput(input)
        var currentNode = start
        var nextNode = start.next

        fun discretizePair(pair: Pair<LocationNode, LocationNode>) {
            val dist = Haversine.haversineInM(
                    pair.first.location.lat,
                    pair.first.location.lon,
                    pair.second.location.lat,
                    pair.second.location.lon
            )
            if (dist < DISCRETIZE_DISTANCE) return
            else {
                // instantiate new LocationNode
                val insertedNode = LocationNode(
                        Location(
                                (pair.first.location.lat + pair.second.location.lat) / 2.0,
                                (pair.first.location.lon + pair.second.location.lon) / 2.0
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

    data class LocationNode(val location: Location, var next: LocationNode?)
}
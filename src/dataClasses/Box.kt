package dataClasses

sealed class Box
    data class Valid(
            val minLat: Double,
            val maxLat: Double,
            val minLon: Double,
            val maxLon: Double
    ) : Box() {
        fun covers(location: Location): Boolean {
            return with(location.lat) { this in minLat..maxLat }
                    && with(location.lon) { this in minLon..maxLon }
        }
    }

    data class Invalid(
            val message: String = "Invalid. Why?"
    ) : Box()


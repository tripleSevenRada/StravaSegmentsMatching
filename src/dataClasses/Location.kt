package dataClasses

data class Location(
        val lat: Double,
        val lon: Double,
        val elevation: Elevation = Elevation.NoValue()
)

sealed class Elevation{
    data class Value(val elevation: Double): Elevation()
    data class NoValue(val why: String = ""): Elevation()
}
package geospatial

import dataClasses.Location
import interfaces.Discretizable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin

class DiscretizerWrapper{

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    public suspend fun cancelAndJoin(){
        job.cancelAndJoin()
    }

    public fun discretizeParallel(input: Discretizable, discretizeDistance: Double = DISCRETIZE_DISTANCE): List<Location>{
        return Discretizer().discretizeInParallel(input, scope, discretizeDistance)
    }
}
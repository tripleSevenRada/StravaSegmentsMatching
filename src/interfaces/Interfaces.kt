package interfaces

import dataClasses.Location

interface Discretizable{
    fun getElements(): List<Location>
}

interface Discretizer{
    fun discretize(discretizable: Discretizable)
}
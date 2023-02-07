package dk.itu.moapd.scootersharing.ahad

/**
 * A data class that encapsulates the information about a scooter
 */
data class Scooter (var name: String,
                    var location: String) {

    /**
     * Returns a formatted string with the information of the Scooter object
     * @return Returns String
     */
    override fun toString(): String {
        return "Ride started using Scooter(name=$name, location=$location)"
    }
}


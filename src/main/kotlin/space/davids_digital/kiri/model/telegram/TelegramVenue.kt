package space.davids_digital.kiri.model.telegram

data class TelegramVenue (
    /**
     * Venue location. Can't be a live location.
     */
    val location: TelegramLocation,
    val title: String,
    val address: String,
    val foursquareId: String? = null,
    /**
     * Foursquare type of the venue.
     * (For example, “arts_entertainment/default”, “arts_entertainment/aquarium” or “food/icecream”.)
     */
    val foursquareType: String? = null,
    val googlePlaceId: String? = null,
    /**
     * Google Places type of the venue.
     * See [supported types](https://developers.google.com/maps/documentation/places/web-service/supported_types).
     */
    val googlePlaceType: String? = null
)
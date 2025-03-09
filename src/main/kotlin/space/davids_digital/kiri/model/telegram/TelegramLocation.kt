package space.davids_digital.kiri.model.telegram

/**
 * Represents a point on the map.
 */
data class TelegramLocation(
    /**
     * Latitude as defined by the sender.
     */
    val latitude: Float,
    /**
     * Longitude as defined by the sender.
     */
    val longitude: Float,
    /**
     * The radius of uncertainty for the location, measured in meters; 0-1500.
     */
    val horizontalAccuracy: Float? = null,
    /**
     * Time relative to the message sending date, during which the location can be updated; in seconds.
     * For active live locations only.
     */
    val livePeriod: Int? = null,
    /**
     * The direction in which user is moving, in degrees; 1-360. For active live locations only.
     */
    val heading: Int? = null,
    /**
     * The maximum distance for proximity alerts about approaching another chat member, in meters.
     * For sent live locations only.
     */
    val proximityAlertRadius: Int? = null
)

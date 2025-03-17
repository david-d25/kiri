package space.davids_digital.kiri.model.telegram

sealed class TelegramPaidMedia {
    /**
     * The paid media isn't available before the payment.
     */
    data class Preview(
        /**
         * Media width as defined by the sender.
         */
        val width: Int,
        /**
         * Media height as defined by the sender
         */
        val height: Int,
        /**
         * Duration of the media in seconds as defined by the sender.
         */
        val duration: Int,
    ) : TelegramPaidMedia()

    /**
     * The paid media is a photo.
     */
    data class Photo(
        val photo: List<TelegramPhotoSize>,
    ) : TelegramPaidMedia()

    /**
     * The paid media is a video.
     */
    data class Video(
        val video: TelegramVideo,
    ) : TelegramPaidMedia()

    class Unknown : TelegramPaidMedia()
}
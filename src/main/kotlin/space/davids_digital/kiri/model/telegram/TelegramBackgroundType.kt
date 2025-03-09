package space.davids_digital.kiri.model.telegram

sealed class TelegramBackgroundType {
    /**
     * The background is automatically filled based on the selected colors.
     */
    data class Fill(
        val fill: TelegramBackgroundFill,
        /**
         * Dimming of the background in dark themes, as a percentage; 0-100
         */
        val darkThemeDimming: Int,
    ) : TelegramBackgroundType()

    /**
     * The background is a wallpaper in the JPEG format.
     */
    data class Wallpaper(
        /**
         * Document with the wallpaper.
         */
        val document: TelegramDocument,
        /**
         * Dimming of the background in dark themes, as a percentage; 0-100.
         */
        val darkThemeDimming: Int,
        /**
         * True, if the wallpaper is downscaled to fit in a 450x450 square and then box-blurred with radius 12.
         */
        val isBlurred: Boolean? = null,
        /**
         * True, if the background moves slightly when the device is tilted.
         */
        val isMoving: Boolean? = null,
    ) : TelegramBackgroundType()

    /**
     * The background is a .PNG or .TGV (gzipped subset of SVG with MIME type "application/x-tgwallpattern") pattern to
     * be combined with the background fill chosen by the user.
     */
    data class Pattern(
        /**
         * Document with the pattern.
         */
        val document: TelegramDocument,
        /**
         * The background fill that is combined with the pattern.
         */
        val fill: TelegramBackgroundFill,
        /**
         * Intensity of the pattern when it is shown above the filled background; 0-100.
         */
        val intensity: Int,
        /**
         * True, if the background fill must be applied only to the pattern itself.
         * All other pixels are black in this case. For dark themes only.
         */
        val isInverted: Boolean? = null,
        /**
         * True, if the background moves slightly when the device is tilted.
         */
        val isMoving: Boolean? = null,
    ) : TelegramBackgroundType()

    /**
     * The background is taken directly from a built-in chat theme.
     */
    data class ChatTheme(
        /**
         * Name of the chat theme, which is usually an emoji.
         */
        val themeName: String,
    )
}
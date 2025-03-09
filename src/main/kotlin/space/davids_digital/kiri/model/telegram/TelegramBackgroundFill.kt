package space.davids_digital.kiri.model.telegram

import java.awt.Color

/**
 * This object describes the way a background is filled based on the selected colors.
 */
sealed class TelegramBackgroundFill {
    data class Solid(val color: Color) : TelegramBackgroundFill()

    data class Gradient(
        val topColor: Color,
        val bottomColor: Color,
        /**
         * Clockwise rotation angle of the background fill in degrees; 0-359.
         */
        val rotationAngle: Int,
    ) : TelegramBackgroundFill()

    data class FreedomGradient(
        /**
         * A list of the 3 or 4 base colors that are used to generate the freeform gradient in the RGB24 format.
         */
        val colors: List<Color>,
    ) : TelegramBackgroundFill()
}
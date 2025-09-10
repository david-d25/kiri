package space.davids_digital.kiri.orm.mapper

import org.mapstruct.Mapper
import java.awt.Color

@Mapper
abstract class ColorRgbMapper {
    fun colorToRgb(color: Color?): Int? = color?.rgb?.and(0xFFFFFF)
    fun rgbToColor(rgb: Int?): Color? = rgb?.let { Color(it and 0xFFFFFF) }
}
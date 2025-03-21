package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import java.awt.Color

@Mapper(componentModel = "spring")
abstract class ColorRgbMapper {
    fun colorToRgb(color: Color?): Int? = color?.rgb?.and(0xFFFFFF)
    fun rgbToColor(rgb: Int?): Color? = rgb?.let { Color(it and 0xFFFFFF) }
}
package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Named
import space.davids_digital.kiri.model.telegram.TelegramBackgroundFill
import space.davids_digital.kiri.model.telegram.TelegramBackgroundFill.*
import space.davids_digital.kiri.orm.entity.telegram.*
import java.awt.Color

@Mapper(componentModel = "spring")
abstract class TelegramBackgroundFillEntityMapper {
    fun toEntity(model: TelegramBackgroundFill): TelegramBackgroundFillEntity {
        return when (model) {
            is Solid -> TelegramBackgroundFillSolidEntity().apply {
                colorRgb = model.color.rgb
            }
            is Gradient -> TelegramBackgroundFillGradientEntity().apply {
                topColorRgb = model.topColor.rgb
                bottomColorRgb = model.bottomColor.rgb
                rotationAngle = model.rotationAngle
            }
            is FreeformGradient -> TelegramBackgroundFillFreeformEntity().apply {
                color1Rgb = model.colors.getOrNull(0)?.rgb ?: 0
                color2Rgb = model.colors.getOrNull(1)?.rgb ?: 0
                color3Rgb = model.colors.getOrNull(2)?.rgb ?: 0
                color4Rgb = model.colors.getOrNull(3)?.rgb
            }
            is Unknown -> TelegramBackgroundFillUnknownEntity()
        }
    }

    fun toModel(entity: TelegramBackgroundFillEntity): TelegramBackgroundFill {
        return when (entity) {
            is TelegramBackgroundFillSolidEntity -> Solid(Color(entity.colorRgb))
            is TelegramBackgroundFillGradientEntity -> Gradient(
                topColor = Color(entity.topColorRgb),
                bottomColor = Color(entity.bottomColorRgb),
                rotationAngle = entity.rotationAngle
            )
            is TelegramBackgroundFillFreeformEntity -> FreeformGradient(
                colors = buildList {
                    add(Color(entity.color1Rgb))
                    add(Color(entity.color2Rgb))
                    add(Color(entity.color3Rgb))
                    entity.color4Rgb?.let { add(Color(it)) }
                }
            )
            is TelegramBackgroundFillUnknownEntity -> Unknown()
            else -> error("Unknown TelegramBackgroundFillEntity subclass: ${entity::class.qualifiedName}")
        }
    }
}

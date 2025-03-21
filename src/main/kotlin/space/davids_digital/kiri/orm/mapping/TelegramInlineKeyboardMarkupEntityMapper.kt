package space.davids_digital.kiri.orm.mapping

import org.mapstruct.*
import space.davids_digital.kiri.model.telegram.TelegramInlineKeyboardButton
import space.davids_digital.kiri.model.telegram.TelegramInlineKeyboardMarkup
import space.davids_digital.kiri.orm.entity.telegram.*

@Mapper(
    componentModel = "spring",
    uses = [TelegramInlineKeyboardButtonEntityMapper::class]
)
abstract class TelegramInlineKeyboardMarkupEntityMapper {

    abstract fun toModel(entity: TelegramInlineKeyboardButtonEntity): TelegramInlineKeyboardButton
    abstract fun toEntity(model: TelegramInlineKeyboardButton): TelegramInlineKeyboardButtonEntity

    fun toEntity(model: TelegramInlineKeyboardMarkup): TelegramInlineKeyboardMarkupEntity {
        val entity = TelegramInlineKeyboardMarkupEntity()
        model.inlineKeyboard.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, button ->
                val buttonEntity = toEntity(button)
                val placement = TelegramInlineKeyboardButtonPlacementEntity().apply {
                    this.markup = entity
                    this.button = buttonEntity
                    this.rowIndex = rowIndex
                    this.columnIndex = colIndex
                }
                entity.buttons += placement
            }
        }
        return entity
    }

    fun toModel(entity: TelegramInlineKeyboardMarkupEntity): TelegramInlineKeyboardMarkup {
        val maxRow = entity.buttons.maxOfOrNull { it.rowIndex } ?: -1
        val rows = MutableList(maxRow + 1) { mutableListOf<TelegramInlineKeyboardButton>() }

        entity.buttons.sortedWith(compareBy({ it.rowIndex }, { it.columnIndex })).forEach {
            val row = rows[it.rowIndex]
            row += toModel(it.button!!)
        }

        return TelegramInlineKeyboardMarkup(rows)
    }
}

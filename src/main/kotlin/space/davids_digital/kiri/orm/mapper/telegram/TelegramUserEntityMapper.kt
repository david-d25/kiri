package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import space.davids_digital.kiri.model.telegram.TelegramUser
import space.davids_digital.kiri.orm.entity.telegram.TelegramUserEntity
import space.davids_digital.kiri.service.TelegramUserMetadataService

@Mapper
abstract class TelegramUserEntityMapper {
    @Lazy
    @Autowired
    lateinit var telegramUserMetadataService: TelegramUserMetadataService

    abstract fun toEntity(model: TelegramUser?): TelegramUserEntity?

    @Mapping(target = "isBot", source = "bot")
    @Mapping(target = "isPremium", source = "premium")
    @Mapping(target = "metadata", source = "id", qualifiedByName = ["loadUserMetadata"])
    abstract fun toModel(entity: TelegramUserEntity?): TelegramUser?

    @Mapping(target = "isBot", source = "entity.bot")
    @Mapping(target = "isPremium", source = "entity.premium")
    abstract fun toModel(entity: TelegramUserEntity?, metadata: TelegramUser.Metadata?): TelegramUser?

    @Suppress("unused") // used in `toModel(User)`
    @Named("loadUserMetadata")
    fun loadUserMetadata(id: Long): TelegramUser.Metadata = telegramUserMetadataService.getOrCreateDefault(id)

}
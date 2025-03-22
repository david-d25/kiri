package space.davids_digital.kiri.orm.mapping

import org.mapstruct.*
import space.davids_digital.kiri.model.telegram.TelegramSuccessfulPayment
import space.davids_digital.kiri.orm.entity.telegram.TelegramSuccessfulPaymentEntity

// TODO use LocalDateMapper
@Mapper(
    componentModel = "spring",
    uses = [TelegramOrderInfoEntityMapper::class]
)
interface TelegramSuccessfulPaymentEntityMapper {
    @Mapping(
        target = "subscriptionExpirationDate",
        source = "subscriptionExpirationDate",
        qualifiedByName = ["zonedToOffset"]
    )
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramSuccessfulPayment?): TelegramSuccessfulPaymentEntity?

    @Mapping(
        target = "subscriptionExpirationDate",
        source = "subscriptionExpirationDate",
        qualifiedByName = ["offsetToZoned"]
    )
    @Mapping(source = "recurring", target = "isRecurring")
    @Mapping(source = "firstRecurring", target = "isFirstRecurring")
    fun toModel(entity: TelegramSuccessfulPaymentEntity?): TelegramSuccessfulPayment?

    companion object {
        @JvmStatic
        @Named("zonedToOffset")
        fun zonedToOffset(date: java.time.ZonedDateTime?): java.time.OffsetDateTime? =
            date?.toOffsetDateTime()

        @JvmStatic
        @Named("offsetToZoned")
        fun offsetToZoned(date: java.time.OffsetDateTime?): java.time.ZonedDateTime? =
            date?.toZonedDateTime()
    }
}

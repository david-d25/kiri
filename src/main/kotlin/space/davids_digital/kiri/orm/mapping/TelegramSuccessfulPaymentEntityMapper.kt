package space.davids_digital.kiri.orm.mapping

import org.mapstruct.*
import space.davids_digital.kiri.model.telegram.TelegramSuccessfulPayment
import space.davids_digital.kiri.orm.entity.telegram.TelegramSuccessfulPaymentEntity

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
    fun toEntity(model: TelegramSuccessfulPayment): TelegramSuccessfulPaymentEntity

    @Mapping(
        target = "subscriptionExpirationDate",
        source = "subscriptionExpirationDate",
        qualifiedByName = ["offsetToZoned"]
    )
    fun toModel(entity: TelegramSuccessfulPaymentEntity): TelegramSuccessfulPayment

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

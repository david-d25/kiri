package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("chat_theme")
class TelegramBackgroundTypeChatThemeEntity : TelegramBackgroundTypeEntity() {
    @Column(name = "theme_name")
    var themeName: String = ""
}
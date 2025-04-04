package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(schema = "telegram", name = "venues")
class TelegramVenueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    @JoinColumn(name = "location_id", referencedColumnName = "internal_id")
    var location: TelegramLocationEntity? = null

    @Column(name = "title")
    var title: String = ""

    @Column(name = "address")
    var address: String = ""

    @Column(name = "foursquare_id")
    var foursquareId: String? = null

    @Column(name = "foursquare_type")
    var foursquareType: String? = null

    @Column(name = "google_place_id")
    var googlePlaceId: String? = null

    @Column(name = "google_place_type")
    var googlePlaceType: String? = null
}

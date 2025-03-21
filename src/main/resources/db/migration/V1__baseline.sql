create table user_sessions (
    id                      uuid primary key,
    user_id                 bigint not null,
    session_token_encrypted bytea not null,
    valid_until             timestamp with time zone not null
);
create index idx_user_sessions__user_id on user_sessions (user_id);

create table telegram_chat_photos (
    internal_id             bigint generated always as identity primary key,
    small_file_id           text not null,
    small_file_download_id  text not null,
    big_file_id             text not null,
    big_file_download_id    text not null
);
create index idx_telegram_chat_photos__small_file_id on telegram_chat_photos (small_file_id);
create index idx_telegram_chat_photos__big_file_id on telegram_chat_photos (big_file_id);

create table telegram_photo_sizes (
    file_unique_id      text primary key,
    file_download_id    text not null,
    width               integer not null,
    height              integer not null,
    file_size           bigint
);

create table telegram_polls (
    id                       text primary key,
    question                 text not null,
    total_voter_count        integer not null,
    is_closed                boolean not null,
    is_anonymous             boolean not null,
    type                     text not null,
    allows_multiple_answers  boolean not null,
    correct_option_id        integer,
    explanation              text,
    open_period              integer,
    close_date               timestamp with time zone
);

create table telegram_poll_options (
    internal_id   bigint generated always as identity primary key,
    text          text not null,
    voter_count   integer not null
);

create table telegram_text_quotes (
    internal_id  bigint generated always as identity primary key,
    text         text not null,
    position     integer not null,
    is_manual    boolean not null
);

create table telegram_animations (
    file_unique_id   text primary key,
    file_download_id text not null,
    width            integer not null,
    height           integer not null,
    duration         integer not null,
    thumbnail_id     text references telegram_photo_sizes(file_unique_id) on update cascade,
    file_name        text,
    mime_type        text,
    file_size        bigint
);

create table telegram_audios (
    file_unique_id   text primary key,
    file_download_id text not null,
    duration         integer not null,
    performer        text,
    title            text,
    file_name        text,
    mime_type        text,
    file_size        bigint,
    thumbnail_id     text references telegram_photo_sizes(file_unique_id) on update cascade
);

create table telegram_background_fills (
    internal_id      bigint generated always as identity primary key,
    type             text not null,

    -- Solid
    color_rgb        integer,

    -- Gradient
    top_color_rgb    integer,
    bottom_color_rgb integer,
    rotation_angle   integer,

    -- FreeformGradient
    color1_rgb       integer,
    color2_rgb       integer,
    color3_rgb       integer,
    color4_rgb       integer,

    -- Constraints
    check (
        (type = 'solid' and color_rgb is not null) or
        (type = 'gradient'  and top_color_rgb is not null
                            and bottom_color_rgb is not null
                            and rotation_angle is not null) or
        (type = 'freeform' and color1_rgb is not null and color2_rgb is not null and color3_rgb is not null) or
        (type = 'unknown')
    )
);

create table telegram_documents (
    file_unique_id   text primary key,
    file_download_id text not null,
    thumbnail_id     text references telegram_photo_sizes(file_unique_id) on update cascade,
    file_name        text,
    mime_type        text,
    file_size        bigint
);

create table telegram_background_types (
    internal_id         bigint generated always as identity primary key,
    type                text not null,

    -- fill
    fill_id             bigint references telegram_background_fills(internal_id) on update cascade,
    dark_theme_dimming  integer,

    -- wallpaper
    document_id         text references telegram_documents(file_unique_id) on update cascade,
    is_blurred          boolean,
    is_moving           boolean,

    -- pattern
    intensity           integer,
    is_inverted         boolean,

    -- chat_theme
    theme_name          text,

    check (
        (type = 'fill' and fill_id is not null and dark_theme_dimming is not null) or
        (type = 'wallpaper' and document_id is not null and dark_theme_dimming is not null
         and is_blurred is not null and is_moving is not null) or
        (type = 'pattern' and document_id is not null and fill_id is not null
         and intensity is not null and is_inverted is not null and is_moving is not null) or
        (type = 'chat_theme' and theme_name is not null) or
        (type = 'unknown')
    )
);

create table telegram_message_origins (
    internal_id        bigint generated always as identity primary key,
    type               text not null,
    date               timestamp with time zone not null,

    -- user
    sender_user_id     bigint,

    -- hidden_user
    sender_user_name   text,

    -- chat
    sender_chat_id     bigint,
    author_signature   text,

    -- channel
    chat_id            bigint,
    message_id         bigint,

    check (
        (type = 'user' and sender_user_id is not null) or
        (type = 'hidden_user' and sender_user_name is not null) or
        (type = 'chat' and sender_chat_id is not null) or
        (type = 'channel' and chat_id is not null and message_id is not null) or
        (type = 'unknown')
    )
);
create index idx_telegram_message_origins__sender_user_id on telegram_message_origins (sender_user_id);
create index idx_telegram_message_origins__sender_chat_id on telegram_message_origins (sender_chat_id);
create index idx_telegram_message_origins__chat_id on telegram_message_origins (chat_id);

create table telegram_videos (
    file_unique_id     text primary key,
    file_download_id   text not null,
    width              integer not null,
    height             integer not null,
    duration           integer not null,
    thumbnail_id       text references telegram_photo_sizes(file_unique_id) on update cascade,
    start_timestamp    integer,
    file_name          text,
    mime_type          text,
    file_size          bigint
);

create table telegram_video_cover_cross_links (
    video_file_unique_id text references telegram_videos(file_unique_id) on update cascade,
    photo_size_id        text references telegram_photo_sizes(file_unique_id) on update cascade,
    primary key (video_file_unique_id, photo_size_id)
);

create table telegram_video_notes (
    file_unique_id     text primary key,
    file_download_id   text not null,
    length             integer not null,
    duration           integer not null,
    thumbnail_id       text references telegram_photo_sizes(file_unique_id) on update cascade,
    file_size          bigint
);

create table telegram_link_preview_options (
    internal_id         bigint generated always as identity primary key,
    is_disabled         boolean not null,
    url                 text,
    prefer_small_media  boolean not null,
    prefer_large_media  boolean not null,
    show_above_text     boolean not null
);

create table telegram_paid_media_info (
    internal_id bigint generated always as identity primary key,
    star_count  integer not null
);

create table telegram_paid_media (
    internal_id         bigint generated always as identity primary key,
    paid_media_info_id  bigint references telegram_paid_media_info(internal_id) on update cascade,
    type                text not null,

    -- for preview
    width               integer,
    height              integer,
    duration            integer,

    -- for video
    video_id            text references telegram_videos(file_unique_id) on update cascade

    check (
        (type = 'preview' and width is not null and height is not null and duration is not null) or
        (type = 'photo') or
        (type = 'video' and video_id is not null) or
        (type = 'unknown')
    )
);
create index idx_telegram_paid_media__paid_media_info_id on telegram_paid_media (paid_media_info_id);

create table telegram_paid_media_photo_sizes (
    paid_media_id   bigint references telegram_paid_media(internal_id) on delete cascade on update cascade,
    photo_size_id   text references telegram_photo_sizes(file_unique_id) on update cascade,
    primary key (paid_media_id, photo_size_id)
);

create table telegram_files (
    file_unique_id    text primary key,
    file_download_id  text not null,
    file_size         bigint,
    file_path         text
);

create table telegram_mask_positions (
    internal_id bigint generated always as identity primary key,
    point       text not null,
    x_shift     real not null,
    y_shift     real not null,
    scale       real not null
);

create table telegram_stickers (
    file_unique_id            text primary key,
    file_download_id          text not null,
    type                      text not null,
    width                     integer not null,
    height                    integer not null,
    is_animated               boolean not null,
    is_video                  boolean not null,
    thumbnail_id              text references telegram_photo_sizes(file_unique_id) on update cascade,
    emoji                     text,
    set_name                  text,
    premium_animation_file_id text references telegram_files(file_unique_id) on update cascade,
    mask_position_id          bigint references telegram_mask_positions(internal_id) on update cascade,
    custom_emoji_id           text,
    needs_repainting          boolean,
    file_size                 bigint
);

create table telegram_stories (
    chat_id    bigint not null,
    story_id   bigint not null,
    primary key (chat_id, story_id)
);
create index idx_telegram_stories__chat_id on telegram_stories (chat_id);

create table telegram_contacts (
    internal_id   bigint generated always as identity primary key,
    phone_number  text not null,
    first_name    text not null,
    last_name     text,
    user_id       bigint,
    vcard         text
);
create index idx_telegram_contacts__user_id on telegram_contacts (user_id);

create table telegram_dice (
    internal_id bigint generated always as identity primary key,
    emoji       text not null,
    value       integer not null
);

create table telegram_games (
    internal_id   bigint generated always as identity primary key,
    title         text not null,
    description   text not null,
    text          text,
    animation_id  text references telegram_animations(file_unique_id) on update cascade
);

create table telegram_game_photos (
    game_id   bigint references telegram_games(internal_id) on delete cascade on update cascade,
    photo_id  text references telegram_photo_sizes(file_unique_id) on update cascade,
    primary key (game_id, photo_id)
);

create table telegram_giveaways (
    internal_id                         bigint generated always as identity primary key,
    winners_selection_date              timestamp with time zone not null,
    winner_count                        integer not null,
    only_new_members                    boolean not null,
    has_public_winners                  boolean not null,
    prize_description                   text,
    prize_star_count                    integer,
    premium_subscription_month_count    integer
);

create table telegram_giveaway_country_codes (
    giveaway_id   bigint references telegram_giveaways(internal_id) on delete cascade on update cascade,
    country_code  text not null
);

create table telegram_giveaway_chat_ids (
    giveaway_id bigint references telegram_giveaways(internal_id) on delete cascade on update cascade,
    chat_id     bigint not null
);

create table telegram_giveaway_winners (
    chat_id                             bigint not null,
    giveaway_message_id                 bigint not null,
    winners_selection_date              timestamp with time zone not null,
    winner_count                        integer not null,
    additional_chat_count               integer,
    prize_star_count                    integer,
    premium_subscription_month_count    integer,
    unclaimed_prize_count               integer,
    only_new_members                    boolean not null,
    was_refunded                        boolean not null,
    prize_description                   text,
    primary key (chat_id, giveaway_message_id)
);
create index idx_telegram_giveaway_winners__chat_id on telegram_giveaway_winners (chat_id);

create table telegram_giveaway_winner_user_ids (
    chat_id              bigint not null,
    giveaway_message_id  bigint not null,
    user_id              bigint not null,
    primary key (chat_id, giveaway_message_id, user_id),
    foreign key (chat_id, giveaway_message_id)
        references telegram_giveaway_winners(chat_id, giveaway_message_id)
        on delete cascade on update cascade
);

create table telegram_invoices (
    internal_id      bigint generated always as identity primary key,
    title            text not null,
    description      text not null,
    start_parameter  text not null,
    currency         text not null,
    total_amount     bigint not null
);

create table telegram_locations (
    internal_id             bigint generated always as identity primary key,
    latitude                real not null,
    longitude               real not null,
    horizontal_accuracy     real,
    live_period             integer,
    heading                 integer,
    proximity_alert_radius  integer
);

create table telegram_poll_options_map (
    poll_id         text references telegram_polls(id) on delete cascade on update cascade,
    poll_option_id  bigint references telegram_poll_options(internal_id) on update cascade,
    primary key (poll_id, poll_option_id)
);

create table telegram_venues (
    internal_id         bigint generated always as identity primary key,
    location_id         bigint references telegram_locations(internal_id) on update cascade,
    title               text not null,
    address             text not null,
    foursquare_id       text,
    foursquare_type     text,
    google_place_id     text,
    google_place_type   text
);

create table telegram_external_reply_info (
    internal_id                  bigint generated always as identity primary key,
    origin_id                    bigint references telegram_message_origins(internal_id) on update cascade,
    chat_id                      bigint,
    message_id                   bigint,
    link_preview_options_id      bigint references telegram_link_preview_options(internal_id) on update cascade,
    animation_id                 text references telegram_animations(file_unique_id) on update cascade,
    audio_id                     text references telegram_audios(file_unique_id) on update cascade,
    document_id                  text references telegram_documents(file_unique_id) on update cascade,
    paid_media_info_id           bigint references telegram_paid_media_info(internal_id) on update cascade,
    sticker_id                   text references telegram_stickers(file_unique_id) on update cascade,
    story_chat_id                bigint,
    video_id                     text references telegram_videos(file_unique_id) on update cascade,
    video_note_id                text references telegram_video_notes(file_unique_id) on update cascade,
    has_media_spoiler            boolean not null,
    contact_id                   bigint references telegram_contacts(internal_id) on update cascade,
    dice_id                      bigint references telegram_dice(internal_id) on update cascade,
    game_id                      bigint references telegram_games(internal_id) on update cascade,
    giveaway_id                  bigint references telegram_giveaways(internal_id) on update cascade,
    giveaway_winners_chat_id     bigint,
    giveaway_winners_message_id  bigint,
    invoice_id                   bigint references telegram_invoices(internal_id) on update cascade,
    location_id                  bigint references telegram_locations(internal_id) on update cascade,
    poll_id                      text references telegram_polls(id) on update cascade,
    venue_id                     bigint references telegram_venues(internal_id) on update cascade,

    foreign key (giveaway_winners_chat_id, giveaway_winners_message_id)
        references telegram_giveaway_winners(chat_id, giveaway_message_id) on update cascade
);

create table telegram_external_reply_info_photo_cross_links (
    reply_info_id bigint references telegram_external_reply_info(internal_id) on delete cascade on update cascade,
    photo_id      text references telegram_photo_sizes(file_unique_id) on update cascade,
    primary key (reply_info_id, photo_id)
);

create table telegram_users (
    id                          bigint primary key,
    is_bot                      boolean not null,
    first_name                  text not null,
    last_name                   text,
    username                    text,
    language_code               text,
    is_premium                  boolean not null,
    added_to_attachment_menu    boolean not null,
    can_join_groups             boolean not null,
    can_read_all_group_messages boolean not null,
    supports_inline_queries     boolean not null,
    can_connect_to_business     boolean not null,
    has_main_web_app            boolean not null
);

create table telegram_message_auto_delete_timer_changes (
    internal_id              bigint generated always as identity primary key,
    message_auto_delete_time integer not null
);

create table telegram_inaccessible_messages (
    chat_id     bigint not null,
    message_id  bigint not null,
    primary key (chat_id, message_id)
);
create index idx_telegram_inaccessible_messages__chat_id on telegram_inaccessible_messages (chat_id);

create table telegram_voices (
    file_unique_id     text primary key,
    file_download_id   text not null,
    duration           integer not null,
    mime_type          text,
    file_size          bigint
);

create table telegram_shipping_addresses (
    internal_id     bigint generated always as identity primary key,
    country_code    text not null,
    state           text not null,
    city            text not null,
    street_line1    text not null,
    street_line2    text not null,
    postcode        text not null
);

create table telegram_order_info (
    internal_id          bigint generated always as identity primary key,
    name                 text,
    phone_number         text,
    email                text,
    shipping_address_id  bigint references telegram_shipping_addresses(internal_id) on update cascade
);

create table telegram_successful_payments (
    internal_id                     bigint generated always as identity primary key,
    currency                        text not null,
    total_amount                    integer not null,
    invoice_payload                 text not null,
    subscription_expiration_date    timestamp with time zone,
    is_recurring                    boolean not null,
    is_first_recurring              boolean not null,
    shipping_option_id              text,
    order_info_id                   bigint references telegram_order_info(internal_id) on update cascade,
    telegram_payment_charge_id      text not null,
    provider_payment_charge_id      text not null
);

create table telegram_refunded_payments (
    internal_id                 bigint generated always as identity primary key,
    currency                    text not null,
    total_amount                integer not null,
    invoice_payload             text not null,
    telegram_payment_charge_id  text not null,
    provider_payment_charge_id  text
);

create table telegram_shared_users (
    user_id     bigint primary key,
    first_name  text,
    last_name   text,
    username    text
);

create table telegram_shared_users_photo_cross_links (
    user_id  bigint not null references telegram_shared_users(user_id) on delete cascade on update cascade,
    photo_id text not null references telegram_photo_sizes(file_unique_id) on update cascade,
    primary key (user_id, photo_id)
);

create table telegram_users_shared (
    internal_id  bigint generated always as identity primary key,
    request_id   bigint not null
);

create table telegram_users_shared_cross_links (
    users_shared_id bigint not null references telegram_users_shared(internal_id) on delete cascade on update cascade,
    shared_user_id  bigint not null references telegram_shared_users(user_id) on update cascade,
    primary key (users_shared_id, shared_user_id)
);

create table telegram_chat_shared (
    internal_id  bigint generated always as identity primary key,
    request_id   bigint not null,
    chat_id      bigint not null,
    title        text,
    username     text
);

create table telegram_chat_shared_photo_cross_links (
    chat_shared_id bigint not null references telegram_chat_shared(internal_id) on delete cascade on update cascade,
    photo_id       text not null references telegram_photo_sizes(file_unique_id) on update cascade,
    primary key (chat_shared_id, photo_id)
);

create table telegram_write_access_allowed (
    internal_id           bigint generated always as identity primary key,
    from_request          boolean not null,
    web_app_name          text,
    from_attachment_menu  boolean not null
);

create table telegram_passport_files (
    file_unique_id     text primary key,
    file_download_id   text not null,
    file_size_bytes    bigint not null,
    file_date          timestamp with time zone not null
);

create table telegram_encrypted_passport_elements (
    internal_id       bigint generated always as identity primary key,
    type              text not null,
    data_base64       text,
    phone_number      text,
    email             text,
    hash_base64       text,
    front_side_id     text references telegram_passport_files(file_unique_id) on update cascade,
    reverse_side_id   text references telegram_passport_files(file_unique_id) on update cascade,
    selfie_id         text references telegram_passport_files(file_unique_id) on update cascade
);

create table telegram_encrypted_passport_elements_files_cross_links (
    element_id  bigint references telegram_encrypted_passport_elements(internal_id) on delete cascade on update cascade,
    file_id     text references telegram_passport_files(file_unique_id) on update cascade,
    primary key (element_id, file_id)
);

create table telegram_encrypted_passport_elements_translations_cross_links (
    element_id  bigint references telegram_encrypted_passport_elements(internal_id) on delete cascade on update cascade,
    file_id     text references telegram_passport_files(file_unique_id) on update cascade,
    primary key (element_id, file_id)
);

create table telegram_encrypted_credentials (
    internal_id     bigint generated always as identity primary key,
    data_base64     text not null,
    hash_base64     text not null,
    secret_base64   text not null
);

create table telegram_passport_data (
    internal_id     bigint generated always as identity primary key,
    credentials_id  bigint not null references telegram_encrypted_credentials(internal_id) on update cascade
);

create table telegram_passport_data_elements_cross_links (
    passport_data_id bigint references telegram_passport_data(internal_id) on delete cascade on update cascade,
    element_id       bigint references telegram_encrypted_passport_elements(internal_id) on update cascade,
    primary key (passport_data_id, element_id)
);

create table telegram_proximity_alerts_triggered (
    internal_id   bigint generated always as identity primary key,
    traveler_id   bigint not null,
    watcher_id    bigint not null,
    distance      integer not null
);

create table telegram_chat_boosts_added (
    internal_id   bigint generated always as identity primary key,
    boost_count   integer not null
);

create table telegram_chat_backgrounds (
    internal_id         bigint generated always as identity primary key,
    background_type_id  bigint references telegram_background_types(internal_id) on update cascade
);

create table telegram_forum_topics_created (
    internal_id           bigint generated always as identity primary key,
    name                  text not null,
    icon_color            integer not null,
    icon_custom_emoji_id  text
);

create table telegram_forum_topics_edited (
    internal_id           bigint generated always as identity primary key,
    name                  text,
    icon_custom_emoji_id  text
);

create table telegram_giveaways_created (
    internal_id        bigint generated always as identity primary key,
    prize_star_count   integer
);

create table telegram_video_chat_scheduled (
    internal_id  bigint generated always as identity primary key,
    start_date   timestamp with time zone not null
);

create table telegram_video_chat_ended (
    internal_id  bigint generated always as identity primary key,
    duration     bigint not null
);

create table telegram_video_chat_participants_invited (
    internal_id  bigint generated always as identity primary key
);

create table telegram_video_chat_invited_user_ids (
    invited_id  bigint not null references telegram_video_chat_participants_invited(internal_id)
        on delete cascade on update cascade,
    user_id     bigint not null,
    primary key (invited_id, user_id)
);

create table telegram_web_app_data (
    internal_id   bigint generated always as identity primary key,
    data          text not null,
    button_text   text not null
);

create table telegram_web_app_info (
    internal_id  bigint generated always as identity primary key,
    url          text not null
);

create table telegram_login_urls (
    internal_id           bigint generated always as identity primary key,
    url                   text not null,
    forward_text          text,
    bot_username          text,
    request_write_access  boolean
);

create table telegram_switch_inline_query_chosen_chats (
    internal_id           bigint generated always as identity primary key,
    query                 text,
    allow_user_chats      boolean,
    allow_bot_chats       boolean,
    allow_group_chats     boolean,
    allow_channel_chats   boolean
);

create table telegram_copy_text_buttons (
    internal_id  bigint generated always as identity primary key,
    text         text not null
);

create table telegram_inline_keyboard_buttons (
    internal_id                         bigint generated always as identity primary key,
    text                                text not null,
    url                                 text,
    callback_data                       text,
    web_app_id                          bigint references telegram_web_app_info(internal_id) on update cascade,
    login_url_id                        bigint references telegram_login_urls(internal_id) on update cascade,
    switch_inline_query                 text,
    switch_inline_query_current_chat    text,
    switch_inline_query_chosen_chat_id  bigint references telegram_switch_inline_query_chosen_chats(internal_id)
                                            on update cascade,
    copy_text_button_id                 bigint references telegram_copy_text_buttons(internal_id) on update cascade,
    pay                                 boolean not null
);

create table telegram_inline_keyboard_markups (
    internal_id bigint generated always as identity primary key
);

create table telegram_inline_keyboard_buttons_cross_links (
    internal_id    bigint generated always as identity primary key,
    markup_id      bigint not null references telegram_inline_keyboard_markups(internal_id) on update cascade,
    button_id      bigint not null references telegram_inline_keyboard_buttons(internal_id) on update cascade,
    row_index      integer not null,
    column_index   integer not null
);
create index idx_telegram_inline_keyboard_buttons_cross_links__markup_id
    on telegram_inline_keyboard_buttons_cross_links (markup_id);

create table telegram_giveaways_completed (
    internal_id                  bigint generated always as identity primary key,
    winner_count                 integer not null,
    unclaimed_prize_count        integer,
    original_message_chat_id     bigint,
    original_message_message_id  bigint,
    is_star_giveaway             boolean not null
    -- foreign key for [original_message_chat_id, original_message_message_id] added after [telegram_messages]
);

create table telegram_messages (
    chat_id                         bigint not null,
    message_id                      bigint not null,
    message_thread_id               bigint,
    date                            timestamp with time zone not null,
    edit_date                       timestamp with time zone,
    from_id                         bigint,
    sender_chat_id                  bigint,
    sender_boost_count              bigint,
    sender_business_bot_id          bigint,
    business_connection_id          text,
    forward_origin_id               bigint references telegram_message_origins(internal_id) on update cascade,
    is_topic_message                boolean not null,
    is_automatic_forward            boolean not null,
    reply_to_message__chat_id       bigint,
    reply_to_message__message_id    bigint,
    external_reply_info_id          bigint references telegram_external_reply_info(internal_id) on update cascade,
    quote_id                        bigint references telegram_text_quotes(internal_id) on update cascade,
    reply_to_story__chat_id         bigint,
    reply_to_story__story_id        bigint,
    via_bot_id                      bigint references telegram_users(id) on update cascade,
    has_protected_content           boolean not null,
    is_from_offline                 boolean not null,
    media_group_id                  text,
    author_signature                text,
    text                            text,
    link_preview_options_id         bigint,
    effect_id                       text,
    animation_id                    text references telegram_animations(file_unique_id) on update cascade,
    audio_id                        text references telegram_audios(file_unique_id) on update cascade,
    document_id                     text references telegram_documents(file_unique_id) on update cascade,
    paid_media_info_id              bigint references telegram_paid_media(internal_id) on update cascade,
    sticker_id                      text references telegram_stickers(file_unique_id) on update cascade,
    story_chat_id                   bigint,
    story_id                        bigint,
    video_id                        text references telegram_videos(file_unique_id) on update cascade,
    video_note_id                   text references telegram_video_notes(file_unique_id) on update cascade,
    voice_id                        text references telegram_voices(file_unique_id) on update cascade,
    caption                         text,
    show_caption_above_media        boolean not null,
    has_media_spoiler               boolean not null,
    contact_id                      bigint references telegram_contacts(internal_id) on update cascade,
    dice_id                         bigint references telegram_dice(internal_id) on update cascade,
    game_id                         bigint references telegram_games(internal_id) on update cascade,
    poll_id                         text references telegram_polls(id) on update cascade,
    venue_id                        bigint references telegram_venues(internal_id) on update cascade,
    location_id                     bigint references telegram_locations(internal_id) on update cascade,
    new_chat_members                bigint[],
    left_chat_member_id             bigint,
    new_chat_title                  text,
    group_chat_created              boolean not null,
    supergroup_chat_created         boolean not null,
    channel_chat_created            boolean not null,
    message_auto_delete_timer_changed_id bigint references telegram_message_auto_delete_timer_changes(internal_id)
                                                on update cascade,
    migrate_to_chat_id              bigint,
    migrate_from_chat_id            bigint,
    pinned_message_chat_id          bigint,
    pinned_message_message_id       bigint,
    invoice_id                      bigint references telegram_invoices(internal_id) on update cascade,
    successful_payment_id           bigint references telegram_successful_payments(internal_id) on update cascade,
    refunded_payment_id             bigint references telegram_refunded_payments(internal_id) on update cascade,
    users_shared_id                 bigint references telegram_users_shared(internal_id) on update cascade,
    chat_shared_id                  bigint references telegram_chat_shared(internal_id) on update cascade,
    connected_website               text,
    write_access_allowed_id         bigint references telegram_write_access_allowed(internal_id) on update cascade,
    passport_data_id                bigint references telegram_passport_data(internal_id) on update cascade,
    proximity_alert_triggered_id    bigint references telegram_proximity_alerts_triggered(internal_id)
                                            on update cascade,
    chat_boost_added_id             bigint references telegram_chat_boosts_added(internal_id) on update cascade,
    chat_background_set_id          bigint references telegram_chat_backgrounds(internal_id) on update cascade,
    forum_topic_created_id          bigint references telegram_forum_topics_created(internal_id) on update cascade,
    forum_topic_edited_id           bigint references telegram_forum_topics_edited(internal_id) on update cascade,
    forum_topic_closed              boolean not null,
    forum_topic_reopened            boolean not null,
    general_forum_topic_hidden      boolean not null,
    general_forum_topic_unhidden    boolean not null,
    giveaway_created_id             bigint references telegram_giveaways_created(internal_id) on update cascade,
    giveaway_id                     bigint references telegram_giveaways(internal_id) on update cascade,
    giveaway_winners_chat_id        bigint,
    giveaway_winners_message_id     bigint,
    giveaway_completed_id           bigint references telegram_giveaways_completed(internal_id) on update cascade,
    video_chat_scheduled_id         bigint references telegram_video_chat_scheduled(internal_id) on update cascade,
    video_chat_ended_id             bigint references telegram_video_chat_ended(internal_id) on update cascade,
    video_chat_participants_invited_id  bigint references telegram_video_chat_participants_invited(internal_id)
                                            on update cascade,
    web_app_data_id                 bigint references telegram_web_app_data(internal_id) on update cascade,
    reply_markup_id                 bigint references telegram_inline_keyboard_markups(internal_id) on update cascade,

    primary key (chat_id, message_id),

    foreign key (reply_to_message__chat_id, reply_to_message__message_id)
        references telegram_messages(chat_id, message_id) on update cascade,
    foreign key (reply_to_story__chat_id, reply_to_story__story_id)
        references telegram_stories(chat_id, story_id) on update cascade,
    foreign key (pinned_message_chat_id, pinned_message_message_id)
        references telegram_messages(chat_id, message_id) on update cascade,
    foreign key (pinned_message_chat_id, pinned_message_message_id)
        references telegram_inaccessible_messages(chat_id, message_id) on update cascade,
    foreign key (story_chat_id, story_id) references telegram_stories(chat_id, story_id) on update cascade,
    foreign key (giveaway_winners_chat_id, giveaway_winners_message_id)
        references telegram_giveaway_winners(chat_id, giveaway_message_id) on update cascade,
);
create table telegram_messages_photo_cross_links (
    chat_id     bigint not null,
    message_id  bigint not null,
    photo_id    text not null,
    primary key (chat_id, message_id, photo_id),
    foreign key (chat_id, message_id) references telegram_messages(chat_id, message_id)
        on delete cascade on update cascade,
    foreign key (photo_id) references telegram_photo_sizes(file_unique_id) on update cascade
);
create table telegram_messages_new_chat_photo_cross_links (
    chat_id     bigint not null,
    message_id  bigint not null,
    photo_id    text not null,
    primary key (chat_id, message_id, photo_id),
    foreign key (chat_id, message_id) references telegram_messages(chat_id, message_id)
        on delete cascade on update cascade,
    foreign key (photo_id) references telegram_photo_sizes(file_unique_id) on update cascade
);

alter table telegram_giveaways_completed
    add constraint fk_telegram_giveaways_completed__original_message
        foreign key (original_message_chat_id, original_message_message_id)
            references telegram_messages(chat_id, message_id) on delete set null on update cascade;
            -- "on delete set null" here to avoid circular dependency

create table telegram_message_entities (
    internal_id                         bigint generated always as identity primary key,
    parent_game_text_id                 bigint references telegram_games(internal_id) on update cascade,
    parent_message_text_chat_id         bigint,
    parent_message_text_message_id      bigint,
    parent_message_caption_chat_id      bigint,
    parent_message_caption_message_id   bigint,
    parent_poll_question_id             text references telegram_polls(id) on update cascade,
    parent_poll_explanation_id          text references telegram_polls(id) on update cascade,
    parent_poll_option_text_id          bigint references telegram_poll_options(internal_id) on update cascade,
    parent_text_quote_id                bigint references telegram_text_quotes(internal_id) on update cascade,
    type                                text not null,
    "offset"                            integer not null,
    length                              integer not null,
    url                                 text,
    user_id                             bigint,
    language                            text,
    custom_emoji_id                     text,

    foreign key (parent_message_text_chat_id, parent_message_text_message_id)
        references telegram_messages(chat_id, message_id) on update cascade,
    foreign key (parent_message_caption_chat_id, parent_message_caption_message_id)
        references telegram_messages(chat_id, message_id) on update cascade,

    -- Only one of the parent columns can be set
    check (
        (parent_game_text_id is not null)::int +
        (parent_message_text_chat_id is not null and parent_message_text_message_id is not null)::int +
        (parent_message_caption_chat_id is not null and parent_message_caption_message_id is not null)::int +
        (parent_poll_question_id is not null)::int +
        (parent_poll_explanation_id is not null)::int +
        (parent_poll_option_text_id is not null)::int +
        (parent_text_quote_id is not null)::int
        = 1
    )
);

create table telegram_poll_option_message_entities (
    poll_option_id      bigint references telegram_poll_options(internal_id) on delete cascade on update cascade,
    message_entity_id   bigint references telegram_message_entities(internal_id) on update cascade,
    primary key (poll_option_id, message_entity_id)
);

create table telegram_chat_permissions (
    internal_id                  bigint generated always as identity primary key,
    can_send_messages            boolean not null,
    can_send_audios              boolean not null,
    can_send_documents           boolean not null,
    can_send_photos              boolean not null,
    can_send_videos              boolean not null,
    can_send_video_notes         boolean not null,
    can_send_voice_notes         boolean not null,
    can_send_polls               boolean not null,
    can_send_other_messages      boolean not null,
    can_add_web_page_previews    boolean not null,
    can_change_info              boolean not null,
    can_invite_users             boolean not null,
    can_pin_messages             boolean not null,
    can_manage_topics            boolean not null
);

create table telegram_chat_locations (
    internal_id    bigint generated always as identity primary key,
    location_id    bigint references telegram_locations(internal_id) on update cascade,
    address        text not null
);

create table telegram_chats (
    id                          bigint primary key,
    type                        text not null,
    title                       text,
    username                    text,
    first_name                  text,
    last_name                   text,
    photo_id                    bigint references telegram_chat_photos(internal_id) on update cascade,
    bio                         text,
    description                 text,
    invite_link                 text,
    pinned_message_chat_id      bigint,
    pinned_message_message_id   bigint,
    permissions_id              bigint references telegram_chat_permissions(internal_id) on update cascade,
    slow_mode_delay             integer,
    sticker_set_name            text,
    can_set_sticker_set         boolean,
    linked_chat_id              bigint,
    location_id                 bigint references telegram_chat_locations(internal_id) on update cascade,

    foreign key (pinned_message_chat_id, pinned_message_message_id)
        references telegram_messages(chat_id, message_id)
        on update cascade
);

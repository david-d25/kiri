package space.davids_digital.kiri.model.telegram

/**
 * Represents the options used for link preview generation.
 */
data class TelegramLinkPreviewOptions (
    /**
     * True, if the link preview is disabled
     */
    val isDisabled: Boolean = false,
    /**
     * URL to use for the link preview. If empty, then the first URL found in the message text will be used
     */
    val url: String? = null,
    /**
     * True, if the media in the link preview is supposed to be shrunk;
     * ignored if the URL isn't explicitly specified or media size change isn't supported for the preview
     */
    val preferSmallMedia: Boolean = false,
    /**
     * True, if the media in the link preview is supposed to be enlarged;
     * ignored if the URL isn't explicitly specified or media size change isn't supported for the preview
     */
    val preferLargeMedia: Boolean = false,
    /**
     * True, if the link preview must be shown above the message text;
     * otherwise, the link preview will be shown below the message text
     */
    val showAboveText: Boolean = false,
)
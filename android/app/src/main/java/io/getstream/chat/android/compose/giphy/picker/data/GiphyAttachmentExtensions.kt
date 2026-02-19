package io.getstream.chat.android.compose.giphy.picker.data

import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType

/**
 * Converts a [GiphyGif] from the Giphy API into a Stream [Attachment].
 */
fun GiphyGif.toStreamAttachment(): Attachment {
    val images = images
    val rendition = images?.downsized
        ?: images?.fixedHeightDownsampled
        ?: images?.fixedHeight
        ?: images?.fixedHeightSmall
        ?: images?.fixedWidthSmall

    requireNotNull(rendition) {
        "GiphyGif must have at least one image rendition"
    }

    val pageUrl = url ?: embedUrl ?: "https://giphy.com/gifs/$id"
    val width = rendition.width?.toIntOrNull() ?: 200
    val height = rendition.height?.toIntOrNull() ?: 200

    return Attachment(
        type = CUSTOM_GIPHY_ATTACHMENT_TYPE,
        titleLink = pageUrl,
        thumbUrl = rendition.url,
        title = title,
        extraData = mapOf(
            CustomGiphyExtraDataKeys.GIF_URL to rendition.url,
            CustomGiphyExtraDataKeys.PAGE_URL to pageUrl,
            CustomGiphyExtraDataKeys.TITLE to (title ?: ""),
            CustomGiphyExtraDataKeys.WIDTH to width.toString(),
            CustomGiphyExtraDataKeys.HEIGHT to height.toString(),
        ),
    )
}

/**
 * Converts a [GiphyGif] from the Giphy API into a Stream [Attachment] using the standard
 * [AttachmentType.GIPHY] format. Rendered by the built-in GiphyAttachmentContent.
 *
 * This is an alternative to [toStreamAttachment] when you prefer the SDK's default giphy
 * rendering (with Giphy label, sizing, etc.) instead of a custom attachment factory.
 *
 * Usage: Replace `gif.toStreamAttachment()` with `gif.toStreamAttachmentStandard()`.
 * You must also remove `customGiphyAttachmentFactory` from ChatTheme.attachmentFactories
 * so the built-in GiphyAttachmentFactory handles it.
 */
@Suppress("Unused")
fun GiphyGif.toStreamAttachmentStandard(): Attachment {
    val images = images
    val rendition = images?.downsized
        ?: images?.fixedHeightDownsampled
        ?: images?.fixedHeight
        ?: images?.fixedHeightSmall
        ?: images?.fixedWidthSmall

    requireNotNull(rendition) {
        "GiphyGif must have at least one image rendition"
    }

    val pageUrl = url ?: embedUrl ?: "https://giphy.com/gifs/$id"
    val width = rendition.width?.toIntOrNull()?.toString() ?: "200"
    val height = rendition.height?.toIntOrNull()?.toString() ?: "200"

    val giphyRenditionMap = mapOf(
        "url" to rendition.url,
        "width" to width,
        "height" to height,
    )

    return Attachment(
        type = AttachmentType.GIPHY,
        titleLink = pageUrl,
        thumbUrl = rendition.url,
        title = title,
        extraData = mapOf(
            AttachmentType.GIPHY to mapOf(
                "original" to giphyRenditionMap,
                "fixed_height" to giphyRenditionMap,
                "fixed_height_downsampled" to giphyRenditionMap,
            ),
        ),
    )
}

/** Custom attachment type for Giphy GIFs. */
const val CUSTOM_GIPHY_ATTACHMENT_TYPE = "custom_giphy"

/** Keys used in extraData for custom Giphy attachments. */
object CustomGiphyExtraDataKeys {
    const val GIF_URL = "gif_url"
    const val PAGE_URL = "page_url"
    const val TITLE = "title"
    const val WIDTH = "width"
    const val HEIGHT = "height"
}

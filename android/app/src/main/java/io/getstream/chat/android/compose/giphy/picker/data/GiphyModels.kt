package io.getstream.chat.android.compose.giphy.picker.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response wrapper from the Giphy API.
 *
 * @property data List of [GiphyGif] objects returned by the API.
 * @property pagination Pagination information for the response.
 */
@JsonClass(generateAdapter = false)
data class GiphyResponse(
    @Json(name = "data") val data: List<GiphyGif> = emptyList(),
    @Json(name = "pagination") val pagination: GiphyPagination? = null,
)

/**
 * Represents a single GIF from the Giphy API.
 *
 * @property id Unique identifier for this GIF.
 * @property title Human-readable title of the GIF.
 * @property url URL to the GIF's page on Giphy.
 * @property embedUrl URL for embedding the GIF.
 * @property images Container for various image renditions of this GIF.
 */
@JsonClass(generateAdapter = false)
data class GiphyGif(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "embed_url") val embedUrl: String? = null,
    @Json(name = "images") val images: GiphyImages? = null,
) {
    /**
     * Returns the best available thumbnail URL for carousel display.
     * Prefers fixed_height_small (100px height), then fixed_width_small, then downsized.
     */
    val thumbnailUrl: String?
        get() = images?.fixedHeightSmall?.url
            ?: images?.fixedWidthSmall?.url
            ?: images?.downsized?.url

    /**
     * Returns the original GIF URL for full-size display.
     */
    val originalUrl: String?
        get() = images?.original?.url
            ?: images?.fixedHeight?.url
            ?: images?.downsized?.url
}

/**
 * Container for various image renditions of a GIF.
 *
 * Giphy provides multiple renditions optimized for different use cases (thumbnails, full-size, etc.).
 *
 * @property fixedHeightSmall Small thumbnail with fixed 100px height.
 * @property fixedWidthSmall Small thumbnail with fixed 100px width.
 * @property fixedHeight Standard rendition with fixed 200px height.
 * @property fixedHeightDownsampled Downsampled version with reduced frame rate.
 * @property original Original full-size GIF.
 * @property downsized Downsized version under 2MB for easier loading.
 */
@JsonClass(generateAdapter = false)
data class GiphyImages(
    @Json(name = "fixed_height_small") val fixedHeightSmall: GiphyImageRendition? = null,
    @Json(name = "fixed_width_small") val fixedWidthSmall: GiphyImageRendition? = null,
    @Json(name = "fixed_height") val fixedHeight: GiphyImageRendition? = null,
    @Json(name = "fixed_height_downsampled") val fixedHeightDownsampled: GiphyImageRendition? = null,
    @Json(name = "original") val original: GiphyImageRendition? = null,
    @Json(name = "downsized") val downsized: GiphyImageRendition? = null,
)

/**
 * A single image rendition with its URL and dimensions.
 *
 * @property url Direct URL to the image file.
 * @property width Width of the image in pixels (as a string).
 * @property height Height of the image in pixels (as a string).
 */
@JsonClass(generateAdapter = false)
data class GiphyImageRendition(
    @Json(name = "url") val url: String,
    @Json(name = "width") val width: String? = null,
    @Json(name = "height") val height: String? = null,
)

/**
 * Pagination information from the Giphy API response.
 *
 * @property offset Current position in the result set.
 * @property count Number of results returned in this response.
 * @property totalCount Total number of results available (may be null for trending).
 */
@JsonClass(generateAdapter = false)
data class GiphyPagination(
    @Json(name = "offset") val offset: Int = 0,
    @Json(name = "count") val count: Int = 0,
    @Json(name = "total_count") val totalCount: Int? = null,
)
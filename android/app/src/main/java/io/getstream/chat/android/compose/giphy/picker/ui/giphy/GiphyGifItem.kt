package io.getstream.chat.android.compose.giphy.picker.ui.giphy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.getstream.chat.android.compose.giphy.picker.data.GiphyGif
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * A single GIF item displayed in the [GiphyPicker] carousel.
 *
 * Renders the GIF thumbnail as a clickable, rounded square image.
 *
 * @param gif The [GiphyGif] to display.
 * @param onClick Callback invoked when this item is clicked.
 * @param modifier Modifier to be applied to this item.
 */
@Composable
fun GiphyGifItem(
    gif: GiphyGif,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // For simplicity sake, return early. In a real-world scenario, handle this gracefully.
    val thumbnailUrl = gif.thumbnailUrl ?: return

    AsyncImage(
        model = thumbnailUrl,
        contentDescription = gif.title ?: "GIF",
        modifier = modifier
            .size(width = 120.dp, height = 120.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ChatTheme.colors.appBackground)
            .clickable(onClick = onClick),
        contentScale = ContentScale.Crop,
    )
}
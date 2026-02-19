package io.getstream.chat.android.compose.giphy.picker.ui.giphy

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import io.getstream.chat.android.compose.giphy.picker.data.CUSTOM_GIPHY_ATTACHMENT_TYPE
import io.getstream.chat.android.compose.giphy.picker.data.CustomGiphyExtraDataKeys
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Custom [AttachmentFactory] for Giphy GIF attachments.
 * Renders the animated GIF in the message list and opens the Giphy page on click.
 */
val customGiphyAttachmentFactory: AttachmentFactory = AttachmentFactory(
    canHandle = { attachments -> attachments.any { it.type == CUSTOM_GIPHY_ATTACHMENT_TYPE } },
    content = @Composable { modifier, attachmentState ->
        CustomGiphyAttachmentContent(
            modifier = modifier,
            attachmentState = attachmentState,
        )
    },
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CustomGiphyAttachmentContent(
    attachmentState: AttachmentState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val (message, _, onLongItemClick) = attachmentState
    val attachment = message.attachments.firstOrNull { it.type == CUSTOM_GIPHY_ATTACHMENT_TYPE }
        ?: return

    val gifUrl = attachment.extraData[CustomGiphyExtraDataKeys.GIF_URL]?.toString()
    val pageUrl = attachment.extraData[CustomGiphyExtraDataKeys.PAGE_URL]?.toString()
    val title = attachment.extraData[CustomGiphyExtraDataKeys.TITLE]?.toString()
    val width = (attachment.extraData[CustomGiphyExtraDataKeys.WIDTH]?.toString()?.toIntOrNull() ?: 200)
    val height = (attachment.extraData[CustomGiphyExtraDataKeys.HEIGHT]?.toString()?.toIntOrNull() ?: 200)

    if (gifUrl == null) return

    val aspectRatio = if (height > 0) width.toFloat() / height else 1f
    val maxWidth = ChatTheme.dimens.attachmentsContentGiphyMaxWidth

    Box(
        modifier = modifier
            .widthIn(max = maxWidth)
            .aspectRatio(aspectRatio)
            .clip(ChatTheme.shapes.attachment)
            .combinedClickable(
                onClick = {
                    pageUrl?.let { url ->
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(url)),
                        )
                    }
                },
                onLongClick = { onLongItemClick(message) },
            ),
    ) {
        AsyncImage(
            model = gifUrl,
            contentDescription = title ?: "GIF",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop,
        )
    }
}

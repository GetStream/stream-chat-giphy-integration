package io.getstream.chat.android.compose.giphy.picker.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Gif
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.giphy.picker.data.toStreamAttachment
import io.getstream.chat.android.compose.giphy.picker.ui.giphy.GiphyIntegrationController
import io.getstream.chat.android.compose.giphy.picker.ui.giphy.GiphyPicker
import io.getstream.chat.android.compose.giphy.picker.ui.giphy.GiphyPickerViewModel
import io.getstream.chat.android.compose.giphy.picker.ui.giphy.GiphyPickerViewModelFactory
import io.getstream.chat.android.compose.giphy.picker.ui.giphy.LocalGiphyIntegrationController
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.LinkPreview
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState

private const val GIPHY_POPUP_HEIGHT_DP = 160

/**
 * Custom [ChatComponentFactory] that integrates a Giphy picker into the message composer.
 *
 * Replaces the default integrations with a GIF button and wraps the composer with an
 * animated Giphy picker carousel that appears above the input field.
 */
class GiphyChatComponentFactory : ChatComponentFactory {

    /**
     * Provides a custom message composer with an integrated Giphy picker.
     *
     * Wraps the default composer with a [GiphyPicker] carousel that animates in/out
     * above the composer. The picker searches for GIFs based on the composer input text.
     *
     * @param messageComposerState The current state of the message composer.
     * @param onSendMessage Callback to send a message with text and attachments.
     * @param modifier Modifier to be applied to the composer.
     * @param onAttachmentsClick Callback for attachments button click.
     * @param onCommandsClick Callback for commands button click.
     * @param onValueChange Callback when the input text changes.
     * @param onAttachmentRemoved Callback when an attachment is removed.
     * @param onCancelAction Callback to cancel the current action.
     * @param onLinkPreviewClick Callback when a link preview is clicked.
     * @param onMentionSelected Callback when a mention is selected.
     * @param onCommandSelected Callback when a command is selected.
     * @param onAlsoSendToChannelSelected Callback for "also send to channel" toggle.
     * @param recordingActions Audio recording action handlers.
     * @param headerContent Content shown in the composer header.
     * @param footerContent Content shown in the composer footer.
     * @param mentionPopupContent Content for the mention suggestions popup.
     * @param commandPopupContent Content for the command suggestions popup.
     * @param integrations Content for the integrations row (replaced with GIF button).
     * @param label Label content for the input field.
     * @param input The input field content.
     * @param audioRecordingContent Content shown during audio recording.
     * @param trailingContent Content shown at the end of the composer.
     */
    @Composable
    override fun MessageComposer(
        messageComposerState: MessageComposerState,
        onSendMessage: (String, List<Attachment>) -> Unit,
        modifier: Modifier,
        onAttachmentsClick: () -> Unit,
        onCommandsClick: () -> Unit,
        onValueChange: (String) -> Unit,
        onAttachmentRemoved: (Attachment) -> Unit,
        onCancelAction: () -> Unit,
        onLinkPreviewClick: ((LinkPreview) -> Unit)?,
        onMentionSelected: (User) -> Unit,
        onCommandSelected: (Command) -> Unit,
        onAlsoSendToChannelSelected: (Boolean) -> Unit,
        recordingActions: AudioRecordingActions,
        headerContent: @Composable ColumnScope.(MessageComposerState) -> Unit,
        footerContent: @Composable ColumnScope.(MessageComposerState) -> Unit,
        mentionPopupContent: @Composable (List<User>) -> Unit,
        commandPopupContent: @Composable (List<Command>) -> Unit,
        integrations: @Composable RowScope.(MessageComposerState) -> Unit,
        label: @Composable (MessageComposerState) -> Unit,
        input: @Composable RowScope.(MessageComposerState) -> Unit,
        audioRecordingContent: @Composable RowScope.(MessageComposerState) -> Unit,
        trailingContent: @Composable (MessageComposerState) -> Unit,
    ) {
        val giphyViewModel: GiphyPickerViewModel = viewModel(factory = GiphyPickerViewModelFactory())
        val giphyController = remember { GiphyIntegrationController() }

        val inputValue = messageComposerState.inputValue
        val isGiphyVisible = giphyController.isVisible

        LaunchedEffect(isGiphyVisible, inputValue) {
            if (!isGiphyVisible) return@LaunchedEffect
            giphyViewModel.onSearchInputChanged(inputValue)
        }

        BackHandler(enabled = isGiphyVisible) {
            giphyController.hide()
        }

        CompositionLocalProvider(
            LocalGiphyIntegrationController provides giphyController,
        ) {
            Column(modifier = modifier.fillMaxWidth()) {
                AnimatedVisibility(
                    visible = isGiphyVisible,
                    enter = expandVertically(),
                    exit = shrinkVertically(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(GIPHY_POPUP_HEIGHT_DP.dp)
                            .background(ChatTheme.colors.appBackground),
                    ) {
                        GiphyPicker(
                            viewModel = giphyViewModel,
                            onGiphyClick = { gif ->
                                val attachment = gif.toStreamAttachment()
                                onSendMessage("", listOf(attachment))
                                giphyController.hide()
                            },
                        )
                    }
                }
                io.getstream.chat.android.compose.ui.messages.composer.MessageComposer(
                    messageComposerState = messageComposerState,
                    onSendMessage = onSendMessage,
                    modifier = Modifier.fillMaxWidth(),
                    onAttachmentsClick = onAttachmentsClick,
                    onCommandsClick = onCommandsClick,
                    onValueChange = onValueChange,
                    onAttachmentRemoved = onAttachmentRemoved,
                    onCancelAction = onCancelAction,
                    onLinkPreviewClick = onLinkPreviewClick,
                    onMentionSelected = onMentionSelected,
                    onCommandSelected = onCommandSelected,
                    onAlsoSendToChannelSelected = onAlsoSendToChannelSelected,
                    recordingActions = recordingActions,
                    headerContent = headerContent,
                    footerContent = footerContent,
                    mentionPopupContent = mentionPopupContent,
                    commandPopupContent = commandPopupContent,
                    integrations = integrations,
                    label = label,
                    input = input,
                    audioRecordingContent = audioRecordingContent,
                    trailingContent = trailingContent,
                )
            }
        }
    }

    /**
     * Provides the integrations row content for the message composer.
     *
     * Replaces the default attachments and commands buttons with a single GIF button
     * that toggles the [GiphyPicker] visibility.
     *
     * @param state The current state of the message composer.
     * @param onAttachmentsClick Callback for attachments button click (unused in this implementation).
     * @param onCommandsClick Callback for commands button click (unused in this implementation).
     */
    @Composable
    override fun RowScope.MessageComposerIntegrations(
        state: MessageComposerState,
        onAttachmentsClick: () -> Unit,
        onCommandsClick: () -> Unit,
    ) {
        val giphyController = LocalGiphyIntegrationController.current ?: return

        val isSelected = giphyController.isVisible
        IconButton(
            modifier = Modifier
                .size(44.dp)
                .padding(4.dp),
            onClick = { giphyController.toggle() },
            content = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Rounded.Gif,
                    contentDescription = "GIF",
                    tint = if (isSelected) {
                        ChatTheme.colors.primaryAccent
                    } else {
                        ChatTheme.colors.textLowEmphasis
                    },
                )
            },
        )
    }
}

//
// Copyright © 2026 Stream.io Inc. All rights reserved.
//

import StreamChat
import StreamChatSwiftUI
import SwiftUI

/// View factory that adds a GIF option to the attachment picker and custom_giphy message view.
final class GiphyIntegrationFactory: ViewFactory {

    var chatClient: ChatClient { DefaultViewFactory.shared.chatClient }

    func makeChannelDestination() -> (ChannelSelectionInfo) -> ChatChannelView<GiphyIntegrationFactory> {
        { [unowned self] selectionInfo in
            let controller = chatClient.channelController(for: selectionInfo.channel.cid)
            return ChatChannelView(
                viewFactory: self,
                channelController: controller,
                scrollToMessage: selectionInfo.message
            )
        }
    }

    func makeLeadingComposerView(
        state: Binding<PickerTypeState>,
        channelConfig: ChannelConfig?
    ) -> some View {
        LeadingComposerViewWithGifButton(
            pickerTypeState: state,
            channelConfig: channelConfig
        )
    }

    func makeComposerInputView(
        text: Binding<String>,
        selectedRangeLocation: Binding<Int>,
        command: Binding<ComposerCommand?>,
        addedAssets: [AddedAsset],
        addedFileURLs: [URL],
        addedCustomAttachments: [CustomAttachment],
        quotedMessage: Binding<ChatMessage?>,
        maxMessageLength: Int?,
        cooldownDuration: Int,
        onCustomAttachmentTap: @escaping (CustomAttachment) -> Void,
        shouldScroll: Bool,
        removeAttachmentWithId: @escaping (String) -> Void
    ) -> some View {
        ComposerInputWithGifGridAbove(
            text: text,
            selectedRangeLocation: selectedRangeLocation,
            command: command,
            addedAssets: addedAssets,
            addedFileURLs: addedFileURLs,
            addedCustomAttachments: addedCustomAttachments,
            quotedMessage: quotedMessage,
            maxMessageLength: maxMessageLength,
            cooldownDuration: cooldownDuration,
            onCustomAttachmentTap: onCustomAttachmentTap,
            shouldScroll: shouldScroll,
            removeAttachmentWithId: removeAttachmentWithId
        )
    }

    func makeAttachmentSourcePickerView(
        selected: AttachmentPickerState,
        onPickerStateChange: @escaping (AttachmentPickerState) -> Void
    ) -> some View {
        DefaultViewFactory.shared.makeAttachmentSourcePickerView(
            selected: selected,
            onPickerStateChange: onPickerStateChange
        )
    }

    func makeCustomAttachmentView(
        addedCustomAttachments: [CustomAttachment],
        onCustomAttachmentTap: @escaping (CustomAttachment) -> Void
    ) -> some View {
        // Grid is shown above the composer (in ComposerInputWithGifGridAbove), not in the attachment area below.
        EmptyView()
    }

    func makeCommandsContainerView(
        suggestions: [String: Any],
        handleCommand: @escaping ([String: Any]) -> Void
    ) -> some View {
        DefaultViewFactory.shared.makeCommandsContainerView(
            suggestions: suggestions,
            handleCommand: handleCommand
        )
    }

    func makeCustomAttachmentViewType(
        for message: ChatMessage,
        isFirst: Bool,
        availableWidth: CGFloat,
        scrolledId: Binding<String?>
    ) -> some View {
        CustomGiphyMessageView(
            message: message,
            isFirst: isFirst,
            availableWidth: availableWidth,
            scrolledId: scrolledId
        )
    }
}

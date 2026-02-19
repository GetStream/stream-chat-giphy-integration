//
// Copyright © 2026 Stream.io Inc. All rights reserved.
//

import StreamChat
import StreamChatSwiftUI
import SwiftUI

// MARK: - Leading composer view with GIF icon (next to instant commands)

/// Wraps the default leading composer view (attachment + instant commands icons) and adds a GIF button next to them.
struct LeadingComposerViewWithGifButton: View {
    @EnvironmentObject private var viewModel: MessageComposerViewModel
    @Injected(\.colors) private var colors

    @Binding var pickerTypeState: PickerTypeState
    var channelConfig: ChannelConfig?

    /// Driven by pickerState only so we don't open the attachment overlay; grid visibility is independent.
    private var isGifSelected: Bool {
        viewModel.pickerState == .custom
    }

    var body: some View {
        HStack(spacing: 16) {
            DefaultViewFactory.shared.makeLeadingComposerView(
                state: $pickerTypeState,
                channelConfig: channelConfig
            )
            .environmentObject(viewModel)

            Button {
                withAnimation {
                    if isGifSelected {
                        viewModel.change(pickerState: .photos)
                    } else {
                        viewModel.change(pickerState: .custom)
                    }
                }
            } label: {
                Text("GIF")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(
                        isGifSelected
                            ? Color(colors.highlightedAccentBackground)
                            : Color(colors.textLowEmphasis)
                    )
            }
            .accessibilityLabel("GIF")
            .accessibilityIdentifier("LeadingComposerGifButton")
        }
        .padding(.bottom, 8)
    }
}

// MARK: - Composer input with GIF grid above (when GIF is selected)

/// When the user taps the GIF icon, the grid is shown **above** the text field (in the composer column), not in the attachment area below.
struct ComposerInputWithGifGridAbove: View {
    @EnvironmentObject private var viewModel: MessageComposerViewModel
    @Injected(\.colors) private var colors

    private static let gridHeight: CGFloat = 260

    private var screenWidth: CGFloat {
        let scene = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first { $0.activationState == .foregroundActive }
            ?? UIApplication.shared.connectedScenes
                .compactMap { $0 as? UIWindowScene }
                .first
        return scene?.screen.bounds.width ?? 393
    }

    let text: Binding<String>
    let selectedRangeLocation: Binding<Int>
    let command: Binding<ComposerCommand?>
    let addedAssets: [AddedAsset]
    let addedFileURLs: [URL]
    let addedCustomAttachments: [CustomAttachment]
    let quotedMessage: Binding<ChatMessage?>
    let maxMessageLength: Int?
    let cooldownDuration: Int
    let onCustomAttachmentTap: (CustomAttachment) -> Void
    let shouldScroll: Bool
    let removeAttachmentWithId: (String) -> Void

    var body: some View {
        let defaultInput = DefaultViewFactory.shared.makeComposerInputView(
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

        Group {
            if viewModel.pickerState == .custom {
                VStack(alignment: .leading, spacing: 0) {
                    GeometryReader { geo in
                        GiphyCustomAttachmentPickerView(
                            addedCustomAttachments: viewModel.addedCustomAttachments,
                            onCustomAttachmentTap: viewModel.customAttachmentTapped(_:)
                        )
                        .environmentObject(viewModel)
                        .frame(width: screenWidth, height: Self.gridHeight)
                        .background(Color(colors.background1))
                        .offset(x: -geo.frame(in: .global).minX)
                    }
                    .frame(height: Self.gridHeight)
                    defaultInput
                }
            } else {
                defaultInput
            }
        }
    }
}

// MARK: - Custom attachment content: Giphy grid (adds to composer)

/// The actual Giphy grid view. Shown above the composer when GIF is selected (via ComposerInputWithGifGridAbove). The attachment area below shows EmptyView to avoid duplicate grid.
struct GiphyCustomAttachmentPickerView: View {
    @EnvironmentObject private var viewModel: MessageComposerViewModel
    @Injected(\.colors) private var colors

    var addedCustomAttachments: [CustomAttachment]
    var onCustomAttachmentTap: (CustomAttachment) -> Void

    private var searchQueryBinding: Binding<String> {
        Binding(
            get: { viewModel.text },
            set: { viewModel.text = $0 }
        )
    }

    var body: some View {
        VStack(spacing: 0) {
            if !addedCustomAttachments.isEmpty {
                addedAttachmentsBar
            }
            GiphyGridView(
                searchQuery: searchQueryBinding,
                isSelectionDisabled: false,
                onSelect: { item in
                    addGifToComposer(item: item)
                }
            )
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .background(Color(colors.background1))
    }

    private var addedAttachmentsBar: some View {
        HStack(spacing: 8) {
            ForEach(addedCustomAttachments) { attachment in
                Button {
                    onCustomAttachmentTap(attachment)
                } label: {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundColor(Color(colors.textLowEmphasis))
                }
                .accessibilityLabel("Remove attachment")
            }
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
        .background(Color(colors.background1))
    }

    private func addGifToComposer(item: GiphyService.GiphyItem) {
        guard let url = item.fullURL ?? item.previewURL else { return }
        let payload = CustomGiphyAttachmentPayload(
            title: item.title ?? "GIF",
            previewURL: url
        )
        let anyPayload = AnyAttachmentPayload(payload: payload)
        let custom = CustomAttachment(id: item.id, content: anyPayload)
        viewModel.addedCustomAttachments = viewModel.addedCustomAttachments + [custom]
        viewModel.sendMessage(
            quotedMessage: viewModel.quotedMessage?.wrappedValue,
            editedMessage: nil
        ) {
            viewModel.change(pickerState: .photos)
        }
    }
}

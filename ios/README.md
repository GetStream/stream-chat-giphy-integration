# Stream Chat iOS – Custom Giphy Picker

This sample demonstrates how to add a custom Giphy GIF picker that integrates with the [Stream Chat SwiftUI SDK](https://getstream.io/chat/docs/ios-swift/). It shows how to extend the SDK's view factory to add a GIF grid above the composer and render `custom_giphy` attachments as animated GIFs in the message list.

## Features

- Custom Giphy picker grid shown above the composer when `/giphy` is active
- Search driven by composer text (or trending when empty)
- Custom attachment type `custom_giphy` with animated GIF rendering in message bubbles
- View factory overrides for leading composer (GIF button), composer input (grid above), and custom attachment view

## Dependencies

- **Stream Chat SwiftUI** – via Swift Package Manager:
  - URL: `https://github.com/GetStream/stream-chat-swiftui.git`
  - Product: `StreamChatSwiftUI`
- **System frameworks** – `Foundation`, `SwiftUI`, `UIKit`, `ImageIO` (for GIF decoding)

## Setup

### Prerequisites

- Xcode 15+
- iOS 14+
- [Stream Chat](https://getstream.io/chat/) account and [Giphy](https://developers.giphy.com/dashboard/) API key

### Configuration

1. **Clone the repository** (from the repo root):

   ```bash
   git clone https://github.com/GetStream/stream-chat-giphy-integration.git
   cd stream-chat-giphy-integration/ios
   ```

2. **Open the project**

   - Open `GiphyIntegrationDemo/GiphyIntegrationDemo.xcodeproj` in Xcode.

3. **Add Stream Chat SwiftUI** (if not already added)

   - File → Add Package Dependencies…
   - URL: `https://github.com/GetStream/stream-chat-swiftui.git`
   - Add the **StreamChatSwiftUI** library to the app target.

4. **Configure credentials**

   - **Stream:** In `GiphyIntegrationDemo/Credentials.swift`, set `streamAPIKey`, `streamUserToken`, and optionally `streamUserId` / `streamUserName`. Use a valid [user token](https://getstream.io/chat/docs/ios-swift/init_and_users/).
   - **Giphy:** In the same file, set `giphyAPIKey` to your [Giphy API key](https://developers.giphy.com/dashboard/).

5. **Build and run** the app.

## Project structure

```
GiphyIntegrationDemo/
├── GiphyIntegrationDemoApp.swift     # App entry, Chat client config
├── AppDelegate.swift                 # Stream Chat initialization
├── AppState.swift                    # App-level state
├── ContentView.swift                 # Root navigation (login / channel list)
├── LoginView.swift                  # Login using Credentials
├── Credentials.swift                # Stream + Giphy API keys (configure here)
├── GiphyService.swift               # Giphy API client (trending + search)
├── GiphyIntegrationFactory.swift    # ViewFactory with Giphy picker + custom attachment
├── GiphyAttachmentPickerViews.swift # Leading composer GIF button, composer input with grid above
├── GiphyCommandsContainerView.swift # /giphy command + grid when command is active
├── GiphyGridView.swift              # Search UI + grid of GIF cells
├── AnimatedGifView.swift            # Animated GIF view (ImageIO-based, no WebKit)
├── CustomGiphyAttachmentPayload.swift # custom_giphy payload + message resolver + bubble view
└── Assets.xcassets
```

## Integration guide

### 1. View factory

Use `GiphyIntegrationFactory` as your view factory so the composer shows the GIF button and grid, and `custom_giphy` messages use the custom view:

```swift
ChatChannelView(
    viewFactory: GiphyIntegrationFactory(),
    channelController: channelController
)
```

The factory overrides:

- `makeLeadingComposerView` – adds the GIF button next to attachments/commands.
- `makeComposerInputView` – wraps the default input and shows the Giphy grid above it when the GIF picker is selected.
- `makeCommandsContainerView` – provides the `/giphy` command and hosts the grid when that command is active.
- `makeCustomAttachmentView` – returns `EmptyView` (the grid is above the composer, not in the attachment strip).
- Custom attachment rendering for `custom_giphy` is wired via `GiphyMessageTypeResolver` and `CustomGiphyMessageView` (see CustomGiphyAttachmentPayload.swift).

### 2. Custom attachment type and rendering

- **Type:** `AttachmentType.customGiphy` (raw value `"custom_giphy"`).
- **Payload:** `CustomGiphyAttachmentPayload` with `title` and `previewURL` (GIF URL).
- **Resolver:** `GiphyMessageTypeResolver` marks messages that have `custom_giphy` attachments so they use the custom view.
- **View:** `CustomGiphyMessageView` / `CustomGiphyAttachmentMessageCell` render the animated GIF in the bubble via `AnimatedGifView`.

### 3. Giphy API

`GiphyService` uses the public Giphy REST API (trending and search). The API key is read from `Credentials.giphyAPIKey`. No extra networking dependencies; `URLSession` only.

## Key components

### GiphyIntegrationFactory

Subclass of `ViewFactory` that plugs the GIF button, grid-above-composer, and `/giphy` command into the default composer, and ensures `custom_giphy` messages are rendered with the custom view.

### GiphyService

- `GiphyService.trending(limit:offset:)` – trending GIFs.
- `GiphyService.search(query:limit:offset:)` – search by query.
- Returns `GiphyItem` with `previewURL` (and optional title, etc.) used for the grid and for the attachment payload.

### Composer flow

1. User taps the GIF button or types `/giphy` → picker state becomes “custom” and the grid appears above the composer.
2. `GiphyGridView` shows trending or search results; composer text (minus `/giphy`) drives debounced search.
3. On tap, the selected `GiphyItem` is converted to a `CustomAttachment` with type `custom_giphy` and the GIF URL, and added to the composer (then sent with the message).

### AnimatedGifView

Displays an animated GIF from a URL using `ImageIO` (no WebKit). Fetches data, decodes frames and delays, and presents an animated `UIImage` in a `UIImageView` (SwiftUI `UIViewRepresentable`).

## License

See the [LICENSE](../LICENSE) file in the repository root.

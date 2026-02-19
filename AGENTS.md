# Agent guide: Stream Chat Giphy Integration

This file gives AI agents the context needed to work on this repo. Read it when editing code, adding features, or keeping iOS and Android in sync.

## What this repo is

- **Purpose:** Sample integration of a Giphy GIF picker with [Stream Chat](https://getstream.io/chat/) on **iOS** and **Android**.
- **Flow:** User types `/giphy` or taps GIF → grid shows (trending/search) → tap GIF → sent as custom attachment → message shows animated GIF in bubble.
- **Docs:** Root [README.md](README.md) is cross-platform; [ios/README.md](ios/README.md) and [android/README.md](android/README.md) are platform-specific setup and architecture.

## Repo structure

```
stream-chat-giphy-integration/
├── README.md          # Cross-platform overview, links to ios/ and android/
├── AGENTS.md          # This file
├── LICENSE
├── ios/               # SwiftUI app (Xcode project, no Package.swift)
│   ├── README.md
│   └── GiphyIntegrationDemo/
│       └── GiphyIntegrationDemo/   # Source + Assets + .xcodeproj
└── android/           # Kotlin Compose app
    ├── README.md
    └── app/src/main/java/io/getstream/chat/android/compose/giphy/picker/
        ├── data/      # Giphy API, models, attachment conversion, config
        └── ui/        # ChatComponentFactory, GiphyPicker, attachment factory
```

## Cross-platform contract (keep in sync)

- **Attachment type:** `custom_giphy` (raw string). Both platforms use this for Giphy-picked GIFs.
- **Extra data keys** (for custom_giphy attachments):
  - `gif_url` – URL of the GIF to display (required).
  - `page_url` – Giphy page link (for “open in browser”).
  - `title` – optional title.
  - `width` / `height` – optional dimensions (strings).
- **Giphy API:** Public REST API (trending + search). Each platform has its own client; behavior and payload shape should stay aligned so attachments are interchangeable.
- When changing attachment shape, conversion logic, or extra keys, update **both** iOS and Android and any docs that reference them.

## iOS

- **Stack:** Swift, SwiftUI, Stream Chat SwiftUI SDK (SPM via Xcode). GIF decoding: ImageIO (no WebKit).
- **Build:** Open `ios/GiphyIntegrationDemo/GiphyIntegrationDemo.xcodeproj` in Xcode. No `Package.swift`; dependency is added in the project.
- **Config:** All in code. [Credentials.swift](ios/GiphyIntegrationDemo/GiphyIntegrationDemo/Credentials.swift): `streamAPIKey`, `streamUserToken`, `streamUserId`, `streamUserName`, `giphyAPIKey`. Prefilled for demo; replace for production.
- **Entry / factory:** [GiphyIntegrationFactory](ios/GiphyIntegrationDemo/GiphyIntegrationDemo/GiphyIntegrationFactory.swift) – custom `ViewFactory`: leading composer (GIF button), composer input (grid above), commands container (`/giphy`), custom attachment view (empty; grid is above composer). [CustomGiphyAttachmentPayload](ios/GiphyIntegrationDemo/GiphyIntegrationDemo/CustomGiphyAttachmentPayload.swift): `AttachmentType.customGiphy`, `CustomGiphyAttachmentPayload` (title, previewURL), `GiphyMessageTypeResolver`, `CustomGiphyMessageView` / `CustomGiphyAttachmentMessageCell` (uses `AnimatedGifView`).
- **Giphy client:** [GiphyService](ios/GiphyIntegrationDemo/GiphyIntegrationDemo/GiphyService.swift) – `trending`, `search`; uses `Credentials.giphyAPIKey`. Returns `GiphyItem` with `previewURL` etc.
- **UI pieces:** [GiphyAttachmentPickerViews](ios/GiphyIntegrationDemo/GiphyIntegrationDemo/GiphyAttachmentPickerViews.swift) – leading composer with GIF button, composer input with grid above; [GiphyCommandsContainerView](ios/GiphyIntegrationDemo/GiphyIntegrationDemo/GiphyCommandsContainerView.swift) – `/giphy` command + grid; [GiphyGridView](ios/GiphyIntegrationDemo/GiphyIntegrationDemo/GiphyGridView.swift) – search + grid; [AnimatedGifView](ios/GiphyIntegrationDemo/GiphyIntegrationDemo/AnimatedGifView.swift) – loads URL, decodes GIF with ImageIO, shows in `UIImageView`.
- **App icon:** Copied from Stream’s DemoAppSwiftUI `Assets.xcassets/AppIcon.appiconset` (multi-size + Contents.json).

## Android

- **Stack:** Kotlin, Compose, Stream Chat Android Compose SDK. GIFs: Coil with `coil-gif`.
- **Build:** Standard Gradle; Giphy API key from `local.properties`: `giphy_api_key=...`. Stream credentials in code: [StreamChatConfig.kt](android/app/src/main/java/io/getstream/chat/android/compose/giphy/picker/data/StreamChatConfig.kt) – `apiKey`, `predefinedUsers` (tokens); prefilled for demo.
- **Entry / factory:** [GiphyChatComponentFactory](android/app/src/main/java/io/getstream/chat/android/compose/giphy/picker/ui/GiphyChatComponentFactory.kt) – custom `ChatComponentFactory`: `MessageComposer` (Giphy picker above + default composer), `MessageComposerIntegrations` (GIF button). [CustomGiphyAttachmentFactory](android/app/src/main/java/io/getstream/chat/android/compose/giphy/picker/ui/giphy/CustomGiphyAttachmentFactory.kt): `canHandle` for `CUSTOM_GIPHY_ATTACHMENT_TYPE`, renders GIF from `CustomGiphyExtraDataKeys.GIF_URL`, opens `PAGE_URL` on click.
- **Giphy:** [GiphyApi](android/app/src/main/java/io/getstream/chat/android/compose/giphy/picker/data/GiphyApi.kt) (Retrofit), [GiphyModels](android/app/src/main/java/io/getstream/chat/android/compose/giphy/picker/data/GiphyModels.kt), [GiphyAttachmentExtensions.kt](android/app/src/main/java/io/getstream/chat/android/compose/giphy/picker/data/GiphyAttachmentExtensions.kt) – `toStreamAttachment()` (custom_giphy) and `toStreamAttachmentStandard()` (SDK giphy type). [GiphyPickerViewModel](android/app/src/main/java/io/getstream/chat/android/compose/giphy/picker/ui/giphy/GiphyPickerViewModel.kt) – trending, debounced search, pagination.
- **Optional:** Android also supports standard Stream `AttachmentType.GIPHY` via `toStreamAttachmentStandard()` and default SDK rendering; iOS sample uses only custom_giphy.

## Conventions for agents

1. **Cross-platform:** Any change to attachment type, extra-data keys, or Giphy payload semantics must be done on both platforms and noted in READMEs if user-visible.
2. **Credentials:** Do not commit real production keys. iOS: use `Credentials.swift` (prefilled demo values are fine for samples). Android: Stream in `StreamChatConfig.kt`, Giphy in `local.properties` (gitignored).
3. **Deprecations:** Prefer current APIs (e.g. iOS 17+ `onChange`; avoid `UIScreen.main` when a context-based alternative exists).
4. **Docs:** Root README stays high-level and links to ios/ and android/. Platform-specific details (setup, architecture, code references) live in `ios/README.md` and `android/README.md`. Update them when you add or rename key files or flows.
5. **Naming:** Use existing naming: `custom_giphy`, `CustomGiphyAttachmentPayload`, `GiphyIntegrationFactory`, `GiphyChatComponentFactory`, `CustomGiphyAttachmentFactory`, `CustomGiphyExtraDataKeys`, etc. Keep iOS/Android naming analogous where possible.

## Quick file reference

| Concern | iOS | Android |
|--------|-----|---------|
| Config / credentials | `Credentials.swift` | `StreamChatConfig.kt`, `local.properties` (Giphy) |
| View/component factory | `GiphyIntegrationFactory.swift` | `GiphyChatComponentFactory.kt` |
| Custom attachment type + rendering | `CustomGiphyAttachmentPayload.swift` | `CustomGiphyAttachmentFactory.kt`, `GiphyAttachmentExtensions.kt` |
| Giphy API / models | `GiphyService.swift` | `GiphyApi.kt`, `GiphyModels.kt`, `GiphyApiProvider.kt` |
| Picker UI | `GiphyGridView.swift`, `GiphyAttachmentPickerViews.swift`, `GiphyCommandsContainerView.swift` | `GiphyPicker.kt`, `GiphyGifItem.kt`, `GiphyPickerViewModel.kt` |
| Animated GIF display | `AnimatedGifView.swift` (ImageIO) | Coil `AsyncImage` + `coil-gif` |

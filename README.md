# Stream Chat + Giphy Integration

Sample integration of a **Giphy GIF picker** with [Stream Chat](https://getstream.io/chat/) on **iOS** and **Android**. In the composer, type `/giphy` (or tap the GIF button) to open a GIF grid; search is driven by the text you type; picking a GIF sends it as a custom attachment and renders as an animated GIF in the message bubble.

## Repo structure

| Platform | Location | Description |
|----------|----------|-------------|
| **iOS** | [ios/](ios/) | SwiftUI + Stream Chat SwiftUI SDK. See [ios/README.md](ios/README.md) for setup and integration details. |
| **Android** | [android/](android/) | Kotlin Compose + Stream Chat Android Compose SDK. See [android/README.md](android/README.md) for setup and integration details. |

## Flow

1. User opens a channel.
2. In the composer, user types `/giphy` or taps the GIF button.
3. A scrollable grid of GIFs appears (trending or search results).
4. User taps a GIF → it is added as a custom Giphy attachment and the message is sent.
5. The message is displayed with the animated GIF in the bubble.

## License

See the [LICENSE](LICENSE) file in this repository.

# Stream Chat Android Compose - Custom Giphy Picker

This sample project demonstrates how to build a custom Giphy picker carousel that integrates with the [Stream Chat Android Compose SDK](https://getstream.io/chat/docs/sdk/android/compose/overview/). It showcases how to extend the SDK's `ChatComponentFactory` to add custom functionality while maintaining seamless integration with Stream's messaging infrastructure.

## Features

- Custom Giphy picker carousel with horizontal scrolling
- Real-time Giphy search with debounced input
- Infinite scroll pagination for GIF results
- Two attachment bridging approaches (custom type vs. standard Stream type)
- Custom attachment rendering factory
- Full integration with Stream Chat's message composer

## Demo

<p align="center">
  <video src="assets/giphy-picker-demo.mp4" width="300" controls></video>
</p>

## Dependencies

```kotlin
// Stream Chat
implementation("io.getstream:stream-chat-android-compose:<version>")
implementation("io.getstream:stream-chat-android-offline:<version>")

// Networking
implementation("com.squareup.retrofit2:retrofit:<version>")
implementation("com.squareup.retrofit2:converter-moshi:<version>")
implementation("com.squareup.moshi:moshi-kotlin:<version>")

// Image Loading (for GIF support)
implementation("io.coil-kt.coil3:coil-compose:<version>")
implementation("io.coil-kt.coil3:coil-gif:<version>")
```

## Setup

### Prerequisites

1. A [Giphy Developer Account](https://developers.giphy.com/) and API key
2. A [Stream Chat Account](https://getstream.io/chat/) with API credentials (optional for initial testing)

### Configuration

1. Clone this repository

2. Create or update `local.properties` in the project root with your Giphy API key:

```properties
giphy_api_key=YOUR_GIPHY_API_KEY
```

3. **Stream Chat Credentials:** This sample includes predefined Stream Chat credentials (API key and test users) so you can run and test the integration immediately. However, for production use or your own development, you should replace these with your own credentials in `StreamChatConfig.kt`:

```kotlin
object StreamChatConfig {
    val apiKey: String = "your_stream_api_key"

    val predefinedUsers: List<StreamUserCredentials> = listOf(
        StreamUserCredentials(
            user = User(
                id = "your_user_id",
                name = "Your User Name",
                image = "https://example.com/avatar.jpg",
            ),
            token = "your_user_token",
        ),
    )
}

class StreamUserCredentials(
    val user: User,
    val token: String,
)
```

You can obtain your own credentials by creating a free account at [getstream.io](https://getstream.io/chat/).

4. Build and run the app

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        MessagesActivity                          │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                      ChatTheme                             │  │
│  │  - componentFactory: GiphyChatComponentFactory             │  │
│  │  - attachmentFactories: [customGiphyAttachmentFactory]     │  │
│  │  ┌─────────────────────────────────────────────────────┐  │  │
│  │  │                  MessagesScreen                      │  │  │
│  │  │  ┌───────────────────────────────────────────────┐  │  │  │
│  │  │  │     MessageComposer (via ComponentFactory)    │  │  │  │
│  │  │  │  ┌─────────────────────────────────────────┐  │  │  │  │
│  │  │  │  │            GiphyPicker                   │  │  │  │  │
│  │  │  │  │  (animated carousel above composer)     │  │  │  │  │
│  │  │  │  └─────────────────────────────────────────┘  │  │  │  │
│  │  │  │  ┌─────────────────────────────────────────┐  │  │  │  │
│  │  │  │  │     Default MessageComposer UI          │  │  │  │  │
│  │  │  │  │  [GIF Button] [Input Field] [Send]      │  │  │  │  │
│  │  │  │  └─────────────────────────────────────────┘  │  │  │  │
│  │  │  └───────────────────────────────────────────────┘  │  │  │
│  │  └─────────────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## Project Structure

```
app/src/main/java/io/getstream/chat/android/compose/giphy/picker/
├── data/
│   ├── GiphyApi.kt                    # Retrofit interface for Giphy API
│   ├── GiphyApiProvider.kt            # Singleton API client provider
│   ├── GiphyModels.kt                 # Data classes for Giphy responses
│   ├── GiphyAttachmentExtensions.kt   # Extensions to convert GIFs to Stream attachments
│   └── StreamChatConfig.kt            # Stream Chat credentials configuration
├── ui/
│   ├── GiphyChatComponentFactory.kt   # Custom ChatComponentFactory with Giphy integration
│   └── giphy/
│       ├── GiphyPicker.kt             # Horizontal GIF carousel composable
│       ├── GiphyGifItem.kt            # Individual GIF item in the carousel
│       ├── GiphyPickerViewModel.kt    # ViewModel for GIF loading and search
│       ├── GiphyIntegrationController.kt  # Controller for picker visibility state
│       └── CustomGiphyAttachmentFactory.kt # Custom attachment renderer
├── App.kt                             # Application class with Stream initialization
├── LoginActivity.kt                   # Login screen
├── ChannelListActivity.kt             # Channel list screen
└── MessagesActivity.kt                # Messages screen with Giphy integration
```

## Integration Guide

### 1. Giphy API Integration

The project uses Retrofit to communicate with the Giphy API:

```kotlin
interface GiphyApi {
    @GET("v1/gifs/trending")
    suspend fun trending(
        @Query("api_key") apiKey: String,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0,
        @Query("rating") rating: String = "g",
    ): GiphyResponse

    @GET("v1/gifs/search")
    suspend fun search(
        @Query("api_key") apiKey: String,
        @Query("q") query: String,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0,
        @Query("rating") rating: String = "g",
    ): GiphyResponse
}
```

### 2. ChatComponentFactory Integration

The `GiphyChatComponentFactory` extends Stream's `ChatComponentFactory` to customize the message composer:

```kotlin
class GiphyChatComponentFactory : ChatComponentFactory {
    
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
                io.getstream.chat.android.compose.ui.messages.composer.MessageComposer(/* ... */)
            }
        }
    }

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
```

### 3. Using the Custom Component Factory

Apply the factory in your `ChatTheme`:

```kotlin
ChatTheme(
    attachmentFactories = listOf(customGiphyAttachmentFactory) + StreamAttachmentFactories.defaults(),
    componentFactory = GiphyChatComponentFactory(),
) {
    MessagesScreen(
        viewModelFactory = viewModelFactory,
        onBackPressed = { finish() },
    )
}
```

## Key Components

### GiphyPickerViewModel

Manages GIF loading with:
- Trending GIFs on initial load
- Debounced search (300ms delay)
- Automatic pagination
- Loading and error states

```kotlin
class GiphyPickerViewModel(
    private val giphyApi: GiphyApi,
    private val apiKey: String,
) : ViewModel() {

    val gifs: StateFlow<List<GiphyGif>>
    val loading: StateFlow<Boolean>
    val loadingMore: StateFlow<Boolean>
    val error: StateFlow<String?>
    val hasMore: StateFlow<Boolean>

    fun onSearchInputChanged(input: String)  // Debounced search
    fun loadMore()                            // Load next page
}
```

### GiphyIntegrationController

Manages the picker visibility state, shared via `CompositionLocal`:

```kotlin
class GiphyIntegrationController {
    var isVisible: Boolean by mutableStateOf(false)
        private set

    fun show()
    fun hide()
    fun toggle()
}

val LocalGiphyIntegrationController = compositionLocalOf<GiphyIntegrationController?> { null }
```

### GiphyPicker

Horizontal carousel with infinite scroll:

```kotlin
@Composable
fun GiphyPicker(
    viewModel: GiphyPickerViewModel,
    modifier: Modifier = Modifier,
    onGiphyClick: (GiphyGif) -> Unit = {},
)
```

## Bridging Giphy Data to Stream Attachments

This project demonstrates two approaches for converting Giphy GIFs to Stream Chat attachments.

### Approach 1: Custom Attachment Type (Default)

Uses a custom attachment type (`custom_giphy`) with full control over rendering.

**Conversion Extension:**

```kotlin
const val CUSTOM_GIPHY_ATTACHMENT_TYPE = "custom_giphy"

object CustomGiphyExtraDataKeys {
    const val GIF_URL = "gif_url"
    const val PAGE_URL = "page_url"
    const val TITLE = "title"
    const val WIDTH = "width"
    const val HEIGHT = "height"
}

fun GiphyGif.toStreamAttachment(): Attachment {
    val rendition = images?.downsized
        ?: images?.fixedHeightDownsampled
        ?: images?.fixedHeight
        ?: images?.fixedHeightSmall
        ?: images?.fixedWidthSmall

    requireNotNull(rendition) { "GiphyGif must have at least one image rendition" }

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
```

**Custom Attachment Factory:**

```kotlin
val customGiphyAttachmentFactory: AttachmentFactory = AttachmentFactory(
    canHandle = { attachments ->
        attachments.any { it.type == CUSTOM_GIPHY_ATTACHMENT_TYPE }
    },
    content = @Composable { modifier, attachmentState ->
        CustomGiphyAttachmentContent(modifier, attachmentState)
    },
)

@Composable
private fun CustomGiphyAttachmentContent(
    modifier: Modifier,
    attachmentState: AttachmentState,
) {
    val context = LocalContext.current
    val (message, _, onLongItemClick) = attachmentState
    val attachment = message.attachments
        .firstOrNull { it.type == CUSTOM_GIPHY_ATTACHMENT_TYPE } ?: return

    val gifUrl = attachment.extraData[CustomGiphyExtraDataKeys.GIF_URL]?.toString() ?: return
    val pageUrl = attachment.extraData[CustomGiphyExtraDataKeys.PAGE_URL]?.toString()
    val title = attachment.extraData[CustomGiphyExtraDataKeys.TITLE]?.toString()
    val width = attachment.extraData[CustomGiphyExtraDataKeys.WIDTH]?.toString()?.toIntOrNull() ?: 200
    val height = attachment.extraData[CustomGiphyExtraDataKeys.HEIGHT]?.toString()?.toIntOrNull() ?: 200

    val aspectRatio = if (height > 0) width.toFloat() / height else 1f

    Box(
        modifier = modifier
            .widthIn(max = ChatTheme.dimens.attachmentsContentGiphyMaxWidth)
            .aspectRatio(aspectRatio)
            .clip(ChatTheme.shapes.attachment)
            .combinedClickable(
                onClick = {
                    // Open Giphy page in browser
                    pageUrl?.let { url ->
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
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
```

**Usage:**

```kotlin
ChatTheme(
    attachmentFactories = listOf(customGiphyAttachmentFactory) + StreamAttachmentFactories.defaults(),
    componentFactory = GiphyChatComponentFactory(),
) {
    // Your content
}
```

**Advantages:**
- Full control over attachment rendering
- Custom data structure optimized for your needs
- Independent of SDK's built-in Giphy handling

**Considerations:**
- Requires implementing and maintaining the attachment factory
- Custom type may not be recognized by other Stream clients without similar customization

---

### Approach 2: Standard Stream Giphy Type

Uses Stream's built-in `AttachmentType.GIPHY` format, rendered by the SDK's default `GiphyAttachmentFactory`.

**Conversion Extension:**

```kotlin
fun GiphyGif.toStreamAttachmentStandard(): Attachment {
    val rendition = images?.downsized
        ?: images?.fixedHeightDownsampled
        ?: images?.fixedHeight
        ?: images?.fixedHeightSmall
        ?: images?.fixedWidthSmall

    requireNotNull(rendition) { "GiphyGif must have at least one image rendition" }

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
```

**Usage:**

To use this approach, make the following changes:

1. In `GiphyChatComponentFactory.kt`, change the import and function call:

```kotlin
// Change this import
import io.getstream.chat.android.compose.giphy.picker.data.toStreamAttachmentStandard

// In MessageComposer, change the onGiphyClick handler:
onGiphyClick = { gif ->
    val attachment = gif.toStreamAttachmentStandard()  // Use standard format
    onSendMessage("", listOf(attachment))
    giphyController.hide()
}
```

2. In `MessagesActivity.kt`, remove the custom attachment factory:

```kotlin
ChatTheme(
    // Use only default factories - the built-in GiphyAttachmentFactory will handle it
    attachmentFactories = StreamAttachmentFactories.defaults(),
    componentFactory = GiphyChatComponentFactory(),
) {
    // Your content
}
```

**Advantages:**
- Uses SDK's built-in rendering with Giphy branding and styling
- Consistent appearance with Stream's standard Giphy integration
- Compatible with other Stream clients out of the box

**Considerations:**
- Less control over visual customization
- Must match the expected `extraData` structure

---

### Comparison Table

| Aspect          | Custom Type                | Standard Type                      |
|-----------------|----------------------------|------------------------------------|
| Attachment Type | `"custom_giphy"`           | `AttachmentType.GIPHY` (`"giphy"`) |
| Rendering       | Custom `AttachmentFactory` | Built-in `GiphyAttachmentFactory`  |
| Data Structure  | Flat key-value pairs       | Nested rendition maps              |
| Customization   | Full control               | Limited to SDK styling             |
| Cross-platform  | Requires custom handling   | Native support                     |
| Maintenance     | You maintain the factory   | SDK maintains rendering            |

## License

```
Copyright 2024 Stream.IO, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

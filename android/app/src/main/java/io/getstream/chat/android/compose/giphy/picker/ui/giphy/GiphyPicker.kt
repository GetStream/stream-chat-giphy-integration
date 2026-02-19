package io.getstream.chat.android.compose.giphy.picker.ui.giphy

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.giphy.picker.data.GiphyGif
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * A horizontal scrolling carousel that displays GIFs from the Giphy API.
 *
 * Shows trending GIFs by default and supports searching via the [viewModel].
 * Automatically loads more GIFs as the user scrolls near the end of the list.
 *
 * @param viewModel The [GiphyPickerViewModel] that manages the GIF data and loading state.
 * @param modifier Modifier to be applied to the picker container.
 * @param onGiphyClick Callback invoked when a GIF is clicked, providing the selected [GiphyGif].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GiphyPicker(
    viewModel: GiphyPickerViewModel,
    modifier: Modifier = Modifier,
    onGiphyClick: (GiphyGif) -> Unit = {},
) {
    val gifs by viewModel.gifs.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val loadingMore by viewModel.loadingMore.collectAsState()
    val error by viewModel.error.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()

    val listState = rememberLazyListState()
    var loadMoreTriggeredForOffset by rememberSaveable { mutableIntStateOf(-1) }

    LaunchedEffect(listState.layoutInfo) {
        val layoutInfo = listState.layoutInfo
        val totalItems = layoutInfo.totalItemsCount
        val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

        if (totalItems > 0 && hasMore && !loadingMore) {
            if (lastVisibleIndex >= totalItems - 3 && loadMoreTriggeredForOffset != totalItems) {
                loadMoreTriggeredForOffset = totalItems
                viewModel.loadMore()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        when {
            error != null -> {
                Text(
                    text = error!!,
                    style = ChatTheme.typography.body,
                    color = ChatTheme.colors.errorAccent,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .align(Alignment.Center),
                )
            }
            loading && gifs.isEmpty() -> {
                LoadingIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                )
            }
            else -> {
                LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = gifs,
                        key = { it.id },
                    ) { gif ->
                        GiphyGifItem(
                            gif = gif,
                            onClick = { onGiphyClick(gif) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                    if (loadingMore) {
                        item(key = "loading_more") {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 2.dp,
                                    color = ChatTheme.colors.primaryAccent,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
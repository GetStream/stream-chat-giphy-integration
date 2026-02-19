package io.getstream.chat.android.compose.giphy.picker.ui.giphy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.compose.giphy.picker.data.GiphyApi
import io.getstream.chat.android.compose.giphy.picker.data.GiphyApiProvider
import io.getstream.chat.android.compose.giphy.picker.data.GiphyGif
import io.getstream.chat.android.compose.giphy.picker.data.GiphyPagination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PAGE_SIZE = 10
private const val SEARCH_DEBOUNCE_MS = 300L
private const val MAX_TRENDING_OFFSET = 50
private const val MAX_SEARCH_OFFSET = 50

/**
 * ViewModel for the [GiphyPicker] that manages GIF loading, searching, and pagination.
 *
 * Loads trending GIFs on initialization and supports searching with debounced input.
 * Handles pagination automatically when [loadMore] is called.
 *
 * @param giphyApi The [GiphyApi] instance for making API calls.
 * @param apiKey The Giphy API key for authentication.
 */
class GiphyPickerViewModel(
    private val giphyApi: GiphyApi = GiphyApiProvider.api,
    private val apiKey: String = GiphyApiProvider.apiKey,
) : ViewModel() {

    private val _gifs = MutableStateFlow<List<GiphyGif>>(emptyList())

    /**
     * The current list of GIFs to display.
     */
    val gifs: StateFlow<List<GiphyGif>> = _gifs.asStateFlow()

    private val _loading = MutableStateFlow(false)

    /**
     * Whether the initial load or search is in progress.
     */
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _loadingMore = MutableStateFlow(false)

    /**
     * Whether additional pages are being loaded.
     */
    val loadingMore: StateFlow<Boolean> = _loadingMore.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)

    /**
     * Current error message, or null if no error.
     */
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    private val _hasMore = MutableStateFlow(true)

    /**
     * Whether more pages of results are available to load.
     */
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private var currentOffset = 0
    private var searchJob: Job? = null

    init {
        loadInitial()
    }

    /**
     * Called when the search input changes. Debounces the search to avoid excessive API calls.
     *
     * @param input The current search query text.
     */
    fun onSearchInputChanged(input: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            search(input)
        }
    }

    /**
     * Loads trending GIFs (initial load). Resets the list.
     */
    private fun loadInitial() {
        viewModelScope.launch {
            if (apiKey.isBlank()) {
                _error.value = "Giphy API key not configured. Add giphy_api_key to local.properties"
                return@launch
            }
            _loading.value = true
            _error.value = null
            _searchQuery.value = ""
            currentOffset = 0
            _hasMore.value = true

            withContext(Dispatchers.IO) {
                runCatching {
                    giphyApi.trending(
                        apiKey = apiKey,
                        limit = PAGE_SIZE,
                        offset = 0,
                    )
                }.fold(
                    onSuccess = { response ->
                        _gifs.value = response.data
                        currentOffset = response.data.size
                        updateHasMore(response.pagination, isTrending = true)
                    },
                    onFailure = {
                        _error.value = it.message ?: "Failed to load GIFs"
                        _gifs.value = emptyList()
                    },
                )
            }
            _loading.value = false
        }
    }

    /**
     * Searches for GIFs matching the query. Resets the list.
     */
    private fun search(query: String) {
        viewModelScope.launch {
            if (apiKey.isBlank()) {
                _error.value = "Giphy API key not configured. Add giphy_api_key to local.properties"
                return@launch
            }
            val trimmedQuery = query.trim()
            if (trimmedQuery.isEmpty()) {
                loadInitial()
                return@launch
            }
            _loading.value = true
            _error.value = null
            _searchQuery.value = trimmedQuery
            currentOffset = 0
            _hasMore.value = true

            withContext(Dispatchers.IO) {
                runCatching {
                    giphyApi.search(
                        apiKey = apiKey,
                        query = trimmedQuery,
                        limit = PAGE_SIZE,
                        offset = 0,
                    )
                }.fold(
                    onSuccess = { response ->
                        _gifs.value = response.data
                        currentOffset = response.data.size
                        updateHasMore(response.pagination, isTrending = false)
                    },
                    onFailure = {
                        _error.value = it.message ?: "Search failed"
                        _gifs.value = emptyList()
                    },
                )
            }
            _loading.value = false
        }
    }

    /**
     * Loads the next page of results (trending or search based on current mode).
     */
    fun loadMore() {
        if (!_hasMore.value || _loadingMore.value) return
        if (apiKey.isBlank()) return

        viewModelScope.launch {
            _loadingMore.value = true
            _error.value = null

            val query = _searchQuery.value
            val offset = currentOffset

            withContext(Dispatchers.IO) {
                runCatching {
                    if (query.isEmpty()) {
                        giphyApi.trending(
                            apiKey = apiKey,
                            limit = PAGE_SIZE,
                            offset = offset,
                        )
                    } else {
                        giphyApi.search(
                            apiKey = apiKey,
                            query = query,
                            limit = PAGE_SIZE,
                            offset = offset,
                        )
                    }
                }.fold(
                    onSuccess = { response ->
                        _gifs.value = _gifs.value + response.data
                        currentOffset += response.data.size
                        updateHasMore(response.pagination, isTrending = query.isEmpty())
                    },
                    onFailure = {
                        _error.value = it.message ?: "Failed to load more"
                    },
                )
            }
            _loadingMore.value = false
        }
    }

    private fun updateHasMore(pagination: GiphyPagination?, isTrending: Boolean) {
        if (pagination == null) {
            _hasMore.value = false
            return
        }
        val maxOffset = if (isTrending) MAX_TRENDING_OFFSET else MAX_SEARCH_OFFSET
        val totalCount = pagination.totalCount ?: Int.MAX_VALUE
        _hasMore.value = currentOffset < maxOffset &&
                currentOffset < totalCount &&
                pagination.count >= PAGE_SIZE
    }
}

/**
 * Factory for creating [GiphyPickerViewModel] instances with custom dependencies.
 *
 * @param giphyApi The [GiphyApi] instance for making API calls.
 * @param apiKey The Giphy API key for authentication.
 */
class GiphyPickerViewModelFactory(
    private val giphyApi: GiphyApi = GiphyApiProvider.api,
    private val apiKey: String = GiphyApiProvider.apiKey,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == GiphyPickerViewModel::class.java) {
            "GiphyPickerViewModelFactory can only create instances of GiphyPickerViewModel"
        }
        return GiphyPickerViewModel(giphyApi, apiKey) as T
    }
}
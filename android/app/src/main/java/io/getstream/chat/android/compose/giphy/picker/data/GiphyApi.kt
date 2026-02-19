package io.getstream.chat.android.compose.giphy.picker.data

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the Giphy API.
 *
 * Provides endpoints for fetching trending GIFs and searching for GIFs by query.
 *
 * @see <a href="https://developers.giphy.com/docs/api">Giphy API Documentation</a>
 */
interface GiphyApi {

    /**
     * Fetches trending GIFs from the Giphy API.
     *
     * @param apiKey The Giphy API key for authentication.
     * @param limit Maximum number of GIFs to return (default: 10).
     * @param offset Starting position in results for pagination (default: 0).
     * @param rating Content rating filter (default: "g" for general audiences).
     * @return [GiphyResponse] containing the list of trending GIFs and pagination info.
     */
    @GET("v1/gifs/trending")
    suspend fun trending(
        @Query("api_key") apiKey: String,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0,
        @Query("rating") rating: String = "g",
    ): GiphyResponse

    /**
     * Searches for GIFs matching the given query.
     *
     * @param apiKey The Giphy API key for authentication.
     * @param query The search query string.
     * @param limit Maximum number of GIFs to return (default: 10).
     * @param offset Starting position in results for pagination (default: 0).
     * @param rating Content rating filter (default: "g" for general audiences).
     * @return [GiphyResponse] containing the list of matching GIFs and pagination info.
     */
    @GET("v1/gifs/search")
    suspend fun search(
        @Query("api_key") apiKey: String,
        @Query("q") query: String,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0,
        @Query("rating") rating: String = "g",
    ): GiphyResponse
}
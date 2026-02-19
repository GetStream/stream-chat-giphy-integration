package io.getstream.chat.android.compose.giphy.picker.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.getstream.chat.android.compose.giphy.picker.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Singleton provider for the Giphy API client.
 *
 * Configures and provides a Retrofit-based [GiphyApi] instance with Moshi JSON parsing.
 * Also provides access to the Giphy API key from build configuration.
 */
object GiphyApiProvider {
    private const val BASE_URL = "https://api.giphy.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    /**
     * The configured [GiphyApi] instance for making API calls.
     */
    val api: GiphyApi = retrofit.create(GiphyApi::class.java)

    /**
     * The Giphy API key from build configuration.
     *
     * Set via `giphy_api_key` in `local.properties`.
     */
    val apiKey: String
        get() = BuildConfig.GIPHY_API_KEY
}
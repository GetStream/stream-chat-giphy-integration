package io.getstream.chat.android.compose.giphy.picker.ui.giphy

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Controller for the Giphy integration popup visibility.
 * Used to show/hide the Giphy carousel and to disable composer send when visible.
 */
class GiphyIntegrationController {

    /**
     * Whether the Giphy picker popup is currently visible.
     */
    var isVisible: Boolean by mutableStateOf(false)
        private set

    /**
     * Shows the Giphy picker popup.
     */
    fun show() {
        isVisible = true
    }

    /**
     * Hides the Giphy picker popup.
     */
    fun hide() {
        isVisible = false
    }

    /**
     * Toggles the visibility of the Giphy picker popup.
     */
    fun toggle() {
        isVisible = !isVisible
    }
}

/**
 * CompositionLocal for the Giphy integration controller.
 * When null, the Giphy integration is not active (default bottom bar).
 * When provided, the Giphy button is shown and the popup can be displayed.
 */
val LocalGiphyIntegrationController = compositionLocalOf<GiphyIntegrationController?> { null }

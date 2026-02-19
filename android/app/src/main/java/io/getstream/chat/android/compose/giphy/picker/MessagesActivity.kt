package io.getstream.chat.android.compose.giphy.picker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.giphy.picker.ui.GiphyChatComponentFactory
import io.getstream.chat.android.compose.giphy.picker.ui.giphy.customGiphyAttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.StreamAttachmentFactories
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory

class MessagesActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID)
            ?: return finish()

        enableEdgeToEdge()
        setContent {
            val viewModelFactory = remember {
                MessagesViewModelFactory(
                    context = this@MessagesActivity,
                    channelId = channelId,
                )
            }
            val componentFactory = remember { GiphyChatComponentFactory() }
            val attachmentFactories = remember {
                listOf(customGiphyAttachmentFactory) + StreamAttachmentFactories.defaults()
            }

            ChatTheme(
                attachmentFactories = attachmentFactories,
                componentFactory = componentFactory,
            ) {
                Scaffold { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                    ) {
                        MessagesScreen(
                            viewModelFactory = viewModelFactory,
                            onBackPressed = { finish() },
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val EXTRA_CHANNEL_ID = "channel_id"

        fun createIntent(context: Context, channelId: String): Intent {
            return Intent(context, MessagesActivity::class.java).apply {
                putExtra(EXTRA_CHANNEL_ID, channelId)
            }
        }
    }
}

package io.getstream.chat.android.compose.giphy.picker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import kotlinx.coroutines.launch

class ChannelListActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var showLogoutDialog by remember { mutableStateOf(false) }

            ChatTheme {
                Scaffold { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                    ) {
                        ChannelsScreen(
                            onChannelClick = { channel ->
                                startActivity(MessagesActivity.createIntent(this@ChannelListActivity, channel.cid))
                            },
                            onBackPressed = { finish() },
                            onHeaderAvatarClick = {
                                showLogoutDialog = true
                            }
                        )
                    }
                }

                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { showLogoutDialog = false },
                        title = { Text("Logout") },
                        text = { Text("Are you sure you want to logout?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showLogoutDialog = false
                                    logout()
                                }
                            ) {
                                Text("Yes")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLogoutDialog = false }) {
                                Text("No")
                            }
                        }
                    )
                }
            }
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            val result = ChatClient.instance().disconnect(flushPersistence = true).await()

            if (result.isSuccess) {
                startActivity(Intent(this@ChannelListActivity, LoginActivity::class.java))
                finish()
            } else {
                val error = result.errorOrNull()?.message ?: "Logout failed"
                Toast.makeText(this@ChannelListActivity, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, ChannelListActivity::class.java)
        }
    }
}

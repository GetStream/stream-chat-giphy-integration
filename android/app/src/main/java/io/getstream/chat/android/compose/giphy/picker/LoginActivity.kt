package io.getstream.chat.android.compose.giphy.picker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil3.compose.AsyncImage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.giphy.picker.data.StreamChatConfig
import io.getstream.chat.android.compose.giphy.picker.data.StreamUserCredentials
import io.getstream.chat.android.compose.giphy.picker.ui.theme.StreamChatAndroidComposeGiphyPickerTheme
import io.getstream.chat.android.models.User
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StreamChatAndroidComposeGiphyPickerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        users = StreamChatConfig.predefinedUsers,
                        onUserClick = { credentials ->
                            connectUser(credentials)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun connectUser(credentials: StreamUserCredentials) {
        lifecycleScope.launch {
            val result = ChatClient.instance().connectUser(
                user = credentials.user,
                token = credentials.token
            ).await()

            if (result.isSuccess) {
                startActivity(ChannelListActivity.createIntent(this@LoginActivity))
                finish()
            } else {
                val error = result.errorOrNull()?.message ?: "Connection failed"
                android.widget.Toast.makeText(
                    this@LoginActivity,
                    error,
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

@Composable
fun LoginScreen(
    users: List<StreamUserCredentials>,
    onUserClick: (StreamUserCredentials) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Select a User",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn {
            items(users) { credentials ->
                UserItem(
                    user = credentials.user,
                    onClick = { onUserClick(credentials) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun UserItem(
    user: User,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.image,
            contentDescription = "${user.name} avatar",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = user.name,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    StreamChatAndroidComposeGiphyPickerTheme {
        LoginScreen(
            users = StreamChatConfig.predefinedUsers,
            onUserClick = {}
        )
    }
}
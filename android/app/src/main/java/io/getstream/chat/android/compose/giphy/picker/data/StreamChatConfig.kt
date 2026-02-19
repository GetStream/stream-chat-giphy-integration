package io.getstream.chat.android.compose.giphy.picker.data

import io.getstream.chat.android.models.User

object StreamChatConfig {

    val apiKey: String = "qx5us2v6xvmh"

    val predefinedUsers: List<StreamUserCredentials> = listOf(
        StreamUserCredentials(
            User(
                id = "lskywalker",
                name = "Luke Skywalker",
                image = "https://vignette.wikia.nocookie.net/starwars/images/2/20/LukeTLJ.jpg",
            ),
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoibHNreXdhbGtlciJ9.81fNAUJHS-qrwEJCqoXgixCxkTIgpLqEVsu-8leoq-Y",
        ),
        StreamUserCredentials(
            user = User(
                id = "lorgana",
                name = "Leia Organa",
                image = "https://vignette.wikia.nocookie.net/starwars/images/f/fc/Leia_Organa_TLJ.png",
            ),
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoibG9yZ2FuYSJ9.8ojDnrQnbxFk2o71u1GgCZLhM2SIb8FsTJRPc6bHHxE",
        ),
        StreamUserCredentials(
            user = User(
                id = "hsolo",
                name = "Han Solo",
                image = "https://vignette.wikia.nocookie.net/starwars/images/e/e2/TFAHanSolo.png",
            ),
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiaHNvbG8ifQ.waMrpdksoUaRMuTA1cN-2PzYXj5gk9qCqpUaJUYVqdg",
        ),
    )
}

class StreamUserCredentials(
    val user: User,
    val token: String,
)
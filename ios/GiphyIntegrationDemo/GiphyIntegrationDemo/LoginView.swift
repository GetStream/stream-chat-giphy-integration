//
// Copyright © 2026 Stream.io Inc. All rights reserved.
//

import StreamChat
import StreamChatSwiftUI
import SwiftUI

struct LoginView: View {
    let chatClient: ChatClient
    @State private var loading = false
    @State private var error: String?

    var body: some View {
        VStack(spacing: 24) {
            Text("StreamChat + Giphy")
                .font(.title)
            Text("Set Credentials.streamAPIKey and streamUserToken in Credentials.swift, then tap to connect.")
                .font(.footnote)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal)

            if let error = error {
                Text(error)
                    .font(.caption)
                    .foregroundColor(.red)
                    .padding()
            }

            Button("Connect") {
                connect()
            }
            .buttonStyle(.borderedProminent)
            .disabled(Credentials.streamAPIKey.isEmpty || Credentials.streamUserToken.isEmpty || loading)
        }
        .padding()
        .overlay(loading ? ProgressView() : nil)
    }

    private func connect() {
        guard !Credentials.streamAPIKey.isEmpty, !Credentials.streamUserToken.isEmpty else {
            error = "Set API key and token in Credentials.swift"
            return
        }
        error = nil
        loading = true
        let token = try? Token(rawValue: Credentials.streamUserToken)
        guard let token else {
            error = "Invalid token"
            loading = false
            return
        }
        // Timeout so we don't spin forever if the completion is never called
        let timeoutWork = DispatchWorkItem { [self] in
            guard loading else { return }
            Task { @MainActor in
                loading = false
                error = "Connection timed out. Check network and credentials."
            }
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 15, execute: timeoutWork)
        chatClient.connectUser(
            userInfo: .init(
                id: Credentials.streamUserId,
                name: Credentials.streamUserName,
                imageURL: nil
            ),
            token: token
        ) { [self] connectError in
            timeoutWork.cancel()
            DispatchQueue.main.async {
                loading = false
                if let connectError = connectError {
                    error = connectError.localizedDescription
                } else {
                    AppState.shared.didLogin(chatClient: chatClient)
                    AppState.shared.userState = .loggedIn
                }
            }
        }
    }
}

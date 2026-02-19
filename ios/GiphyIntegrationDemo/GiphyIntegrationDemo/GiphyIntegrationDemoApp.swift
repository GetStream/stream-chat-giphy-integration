//
// Copyright © 2026 Stream.io Inc. All rights reserved.
//

import StreamChat
import StreamChatSwiftUI
import SwiftUI

@main
struct GiphyIntegrationDemoApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    @StateObject private var appState = AppState.shared

    var body: some Scene {
        WindowGroup {
            Group {
                switch appState.userState {
                case .notLoggedIn:
                    LoginView(chatClient: appDelegate.chatClient)
                case .loggedIn:
                    channelListView()
                }
            }
            .environmentObject(appState)
        }
    }

    private func channelListView() -> ChatChannelListView<GiphyIntegrationFactory> {
        return ChatChannelListView(
            viewFactory: GiphyIntegrationFactory(),
            channelListController: appState.channelListController,
            searchType: .channels
        )
    }
}

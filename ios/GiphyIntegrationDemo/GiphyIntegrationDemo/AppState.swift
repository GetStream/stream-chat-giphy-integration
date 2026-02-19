//
// Copyright © 2026 Stream.io Inc. All rights reserved.
//

import Combine
import StreamChat
import StreamChatSwiftUI

enum UserState {
    case notLoggedIn
    case loggedIn
}

final class AppState: ObservableObject {
    @Published var userState: UserState = .notLoggedIn
    private(set) var channelListController: ChatChannelListController?

    static let shared = AppState()

    private init() {}

    func didLogin(chatClient: ChatClient) {
        guard let userId = chatClient.currentUserId else { return }
        let query = ChannelListQuery(
            filter: .containMembers(userIds: [userId])
        )
        channelListController = chatClient.channelListController(query: query)
    }

    func didLogout() {
        channelListController = nil
    }
}

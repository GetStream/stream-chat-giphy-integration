//
// Copyright © 2026 Stream.io Inc. All rights reserved.
//

import StreamChat
import StreamChatSwiftUI
import UIKit

class AppDelegate: NSObject, UIApplicationDelegate {

    var streamChat: StreamChat?

    lazy var chatClient: ChatClient = {
        var config = ChatClientConfig(apiKey: .init(Credentials.streamAPIKey))
        config.isLocalStorageEnabled = true
        let client = ChatClient(config: config)
        return client
    }()

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        let utils = Utils(
            messageTypeResolver: GiphyMessageTypeResolver()
        )
        streamChat = StreamChat(chatClient: chatClient, utils: utils)
        return true
    }
}

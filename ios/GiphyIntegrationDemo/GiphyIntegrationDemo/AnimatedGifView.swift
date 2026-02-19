//
// Copyright © 2026 Stream.io Inc. All rights reserved.
//

import ImageIO
import SwiftUI
import UIKit

/// Displays an animated GIF via UIImageView and ImageIO (SwiftUI's AsyncImage only shows the first frame).
struct AnimatedGifView: UIViewRepresentable {
    let gifURL: URL

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    func makeUIView(context: Context) -> UIImageView {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFill
        imageView.clipsToBounds = true
        imageView.backgroundColor = .clear
        return imageView
    }

    func updateUIView(_ imageView: UIImageView, context: Context) {
        guard gifURL != context.coordinator.lastLoadedURL else { return }
        context.coordinator.lastLoadedURL = gifURL
        context.coordinator.task?.cancel()

        guard gifURL.scheme == "https" || gifURL.scheme == "http" else { return }

        context.coordinator.task = Task { @MainActor in
            do {
                let (data, _) = try await URLSession.shared.data(from: gifURL)
                guard !Task.isCancelled,
                      let animated = Self.decodeGif(data: data)
                else { return }
                imageView.image = animated
            } catch {
                // Ignore cancellation / load errors
            }
        }
    }

    private static func decodeGif(data: Data) -> UIImage? {
        guard let source = CGImageSourceCreateWithData(data as CFData, nil) else { return nil }
        let count = CGImageSourceGetCount(source)
        guard count > 0 else { return nil }

        var images: [UIImage] = []
        var duration: TimeInterval = 0

        for i in 0 ..< count {
            guard let cgImage = CGImageSourceCreateImageAtIndex(source, i, nil) else { continue }
            let delay = Self.frameDelay(at: i, source: source)
            duration += delay
            images.append(UIImage(cgImage: cgImage))
        }

        guard !images.isEmpty else { return nil }
        return UIImage.animatedImage(with: images, duration: duration > 0 ? duration : 0.1)
    }

    private static func frameDelay(at index: Int, source: CGImageSource) -> TimeInterval {
        guard let props = CGImageSourceCopyPropertiesAtIndex(source, index, nil) as? [String: Any],
              let gif = props[kCGImagePropertyGIFDictionary as String] as? [String: Any]
        else { return 0.1 }
        let unclamped = (gif[kCGImagePropertyGIFUnclampedDelayTime as String] as? NSNumber)?.doubleValue
        let clamped = (gif[kCGImagePropertyGIFDelayTime as String] as? NSNumber)?.doubleValue
        let delay = unclamped ?? clamped ?? 0.1
        return delay > 0 ? min(delay, 1.0) : 0.1
    }

    final class Coordinator {
        var lastLoadedURL: URL?
        var task: Task<Void, Never>?
    }
}

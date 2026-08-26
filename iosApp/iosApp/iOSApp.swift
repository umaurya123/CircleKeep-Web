import SwiftUI
import ComposeApp
import GoogleMobileAds
import AppTrackingTransparency

class AppDependencyManager: ObservableObject {
    var adsProvider: PlatformProviderImpl?

    init() {
        #if DEBUG
        PlatformUIKt.setBuildVariant(variant: "Debug")
        #else
        PlatformUIKt.setBuildVariant(variant: "Release")
        #endif
        
        self.adsProvider = PlatformProviderImpl(onPurchaseSuccess: {
            PlatformUIKt.notifyPurchaseSuccess()
        })
    }

    func start() {
        // Request tracking authorization before initializing ads
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            ATTrackingManager.requestTrackingAuthorization { status in
                // Crucial: Execute UI/SDK initialization on the Main Thread
                DispatchQueue.main.async {
                    MobileAds.shared.start(completionHandler: { _ in })
                    if let provider = self.adsProvider {
                        PlatformUIKt.setPlatformProvider(provider: provider)
                        provider.loadInterstitial()
                    }
                }
            }
        }
    }
}

@main
struct iOSApp: App {
    @StateObject private var dependencyManager = AppDependencyManager()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onAppear {
                    dependencyManager.start()
                }
        }
    }
}

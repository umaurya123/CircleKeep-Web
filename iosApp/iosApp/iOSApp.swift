import SwiftUI
import ComposeApp
import GoogleMobileAds
import AppTrackingTransparency

class AppDependencyManager: ObservableObject {
    var adsProvider: PlatformProviderImpl?

    init() {
        self.adsProvider = PlatformProviderImpl(onPurchaseSuccess: {
            PlatformUIKt.notifyPurchaseSuccess()
        })
    }

    func start() {
        // Request tracking authorization before initializing ads
        // We use dispatch_after to give the app a moment to finish launching
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

    init() {
        // We call start() in init or onAppear. 
        // Calling it here is safe because dependencyManager is a StateObject (reference type).
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onAppear {
                    dependencyManager.start()
                }
        }
    }
}

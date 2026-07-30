import SwiftUI
import ComposeApp
import GoogleMobileAds

@main
struct iOSApp: App {
    let adsProvider = PlatformProviderImpl(onPurchaseSuccess: {
        PlatformUIKt.notifyPurchaseSuccess()
    })

    init() {
        MobileAds.shared.start(completionHandler: { _ in })
        PlatformUIKt.setPlatformProvider(provider: adsProvider)
        adsProvider.loadInterstitial()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

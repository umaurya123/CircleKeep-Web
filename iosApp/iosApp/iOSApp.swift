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
    @State private var pendingImportData: String? = nil

    var body: some Scene {
        WindowGroup {
            ContentView(importData: $pendingImportData)
                .onAppear {
                    dependencyManager.start()
                }
                .onOpenURL { url in
                    print("DEBUG: Received URL: \(url.absoluteString)")
                    
                    let accessSecurityScopedResource = url.startAccessingSecurityScopedResource()
                    defer {
                        if accessSecurityScopedResource {
                            url.stopAccessingSecurityScopedResource()
                        }
                    }
                    
                    do {
                        // Attempt to read with multiple encodings
                        let data = try Data(contentsOf: url)
                        var jsonString: String? = String(data: data, encoding: .utf8)
                        
                        if jsonString == nil {
                            jsonString = String(data: data, encoding: .isoLatin1)
                        }
                        
                        if let finalString = jsonString {
                            print("DEBUG: Successfully read file data (\(data.count) bytes)")
                            // Propagate to UI on main thread with a small delay for cold launch stability
                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                                self.pendingImportData = finalString
                                PlatformUIKt.showToastMessage(message: "Processing backup file...")
                            }
                        } else {
                            print("ERROR: Could not decode file content as string")
                        }
                    } catch {
                        print("ERROR: Failed to read external file: \(error.localizedDescription)")
                    }
                }
        }
    }
}

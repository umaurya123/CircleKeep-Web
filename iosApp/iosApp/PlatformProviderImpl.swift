import UIKit
import GoogleMobileAds
import ComposeApp
import StoreKit

class PlatformProviderImpl: NSObject, NativePlatformProvider, FullScreenContentDelegate {
    
    private var bannerView: BannerView?
    private var interstitial: InterstitialAd?
    private var onInterstitialDismissed: (() -> Void)?
    private var onPurchaseSuccess: (() -> Void)?

    init(onPurchaseSuccess: @escaping () -> Void) {
        self.onPurchaseSuccess = onPurchaseSuccess
        super.init()
    }

    func getBannerView() -> UIView {
        if let banner = bannerView {
            return banner
        }
        
        let banner = BannerView(adSize: AdSizeBanner)
        banner.adUnitID = AdConfig.shared.IOS_BANNER_AD_UNIT_ID
        banner.rootViewController = IOSPlatformUI.companion.getTopViewController()
        banner.load(Request())
        self.bannerView = banner
        return banner
    }

    func showInterstitialAd(onAdDismissed: @escaping () -> Void) {
        self.onInterstitialDismissed = onAdDismissed
        
        if let ad = interstitial {
            let topVC = IOSPlatformUI.companion.getTopViewController()
            ad.present(from: topVC!)
        } else {
            print("Ad wasn't ready")
            loadInterstitial()
            onAdDismissed()
        }
    }
    
    func loadInterstitial() {
        let request = Request()
        InterstitialAd.load(with: AdConfig.shared.IOS_INTERSTITIAL_AD_UNIT_ID,
                              request: request,
                              completionHandler: { [weak self] ad, error in
            if let error = error {
                print("Failed to load interstitial ad with error: \(error.localizedDescription)")
                return
            }
            self?.interstitial = ad
            self?.interstitial?.fullScreenContentDelegate = self
        })
    }

    func launchPurchaseFlow(productId: String) {
        Task {
            do {
                let products = try await Product.products(for: [productId])
                if let product = products.first {
                    let result = try await product.purchase()
                    switch result {
                    case .success(let verification):
                        switch verification {
                        case .verified(let transaction):
                            await transaction.finish()
                            onPurchaseSuccess?()
                        case .unverified:
                            print("Transaction unverified")
                        }
                    case .userCancelled:
                        print("User cancelled")
                    case .pending:
                        print("Purchase pending")
                    @unknown default:
                        break
                    }
                }
            } catch {
                print("Failed to purchase: \(error.localizedDescription)")
            }
        }
    }

    func queryPurchases() {
        Task {
            for await result in Transaction.currentEntitlements {
                switch result {
                case .verified(let transaction):
                    if transaction.productID == "pro_upgrade" {
                        onPurchaseSuccess?()
                    }
                case .unverified:
                    break
                }
            }
        }
    }
    
    // MARK: - FullScreenContentDelegate
    
    func adDidDismissFullScreenContent(_ ad: FullScreenPresentingAd) {
        onInterstitialDismissed?()
        loadInterstitial() // Load next one
    }
    
    func ad(_ ad: FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: Error) {
        print("Ad did fail to present full screen content with error: \(error.localizedDescription)")
        onInterstitialDismissed?()
        loadInterstitial()
    }
}

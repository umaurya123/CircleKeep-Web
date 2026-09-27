import UIKit
import GoogleMobileAds
import ComposeApp
import StoreKit
import AVFoundation

class PlatformProviderImpl: NSObject, NativePlatformProvider, FullScreenContentDelegate {
    
    private var bannerView: BannerView?
    private var interstitial: InterstitialAd?
    private var onInterstitialDismissed: (() -> Void)?
    private var onPurchaseSuccess: (() -> Void)?
    private let platformUI = IOSPlatformUI()

    init(onPurchaseSuccess: @escaping () -> Void) {
        self.onPurchaseSuccess = onPurchaseSuccess
        super.init()
    }

    func getBannerView() -> UIView {
        if let banner = bannerView {
            return banner
        }
        
        let topVC = IOSPlatformUI.companion.getTopViewController()
        let width = topVC?.view.frame.width ?? UIScreen.main.bounds.width
        
        // Use modern Adaptive Banners for 2026
        let adSize = largeAnchoredAdaptiveBanner(width: width)
        
        let banner = BannerView(adSize: adSize)
        banner.adUnitID = AdConfig.shared.IOS_BANNER_AD_UNIT_ID
        banner.rootViewController = topVC
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
                guard let product = products.first else {
                    platformUI.showToast(message: "Product '\(productId)' not found in App Store.")
                    return
                }
                
                let result = try await product.purchase()
                switch result {
                case .success(let verification):
                    switch verification {
                    case .verified(let transaction):
                        await transaction.finish()
                        platformUI.showToast(message: "Purchase successful!")
                        onPurchaseSuccess?()
                    case .unverified:
                        platformUI.showToast(message: "Transaction verification failed.")
                    }
                case .userCancelled:
                    print("User cancelled")
                case .pending:
                    platformUI.showToast(message: "Purchase is pending approval.")
                @unknown default:
                    break
                }
            } catch {
                platformUI.showToast(message: "Error: \(error.localizedDescription)")
            }
        }
    }

    func queryPurchases() {
        Task {
            var found = false
            for await result in Transaction.currentEntitlements {
                switch result {
                case .verified(let transaction):
                    if transaction.productID == "pro_upgrade" {
                        found = true
                        onPurchaseSuccess?()
                    }
                case .unverified:
                    break
                }
            }
            if found {
                platformUI.showToast(message: "Purchases restored successfully.")
            } else {
                platformUI.showToast(message: "No previous purchases found.")
            }
        }
    }

    func showQRScanner(onCodeScanned: @escaping (String) -> Void, onCancel: @escaping () -> Void) {
        DispatchQueue.main.async {
            let scannerVC = QRScannerViewController()
            scannerVC.onCodeScanned = onCodeScanned
            scannerVC.onCancel = onCancel
            
            let topVC = IOSPlatformUI.companion.getTopViewController()
            topVC?.present(scannerVC, animated: true, completion: nil)
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

class QRScannerViewController: UIViewController, AVCaptureMetadataOutputObjectsDelegate {
    var captureSession: AVCaptureSession!
    var previewLayer: AVCaptureVideoPreviewLayer!
    var onCodeScanned: ((String) -> Void)?
    var onCancel: (() -> Void)?

    override func viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = UIColor.black
        captureSession = AVCaptureSession()

        guard let videoCaptureDevice = AVCaptureDevice.default(for: .video) else { return }
        let videoInput: AVCaptureDeviceInput

        do {
            videoInput = try AVCaptureDeviceInput(device: videoCaptureDevice)
        } catch {
            return
        }

        if (captureSession.canAddInput(videoInput)) {
            captureSession.addInput(videoInput)
        } else {
            return
        }

        let metadataOutput = AVCaptureMetadataOutput()

        if (captureSession.canAddOutput(metadataOutput)) {
            captureSession.addOutput(metadataOutput)

            metadataOutput.setMetadataObjectsDelegate(self, queue: DispatchQueue.main)
            metadataObjectTypes(metadataOutput)
        } else {
            return
        }

        previewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
        previewLayer.frame = view.layer.bounds
        previewLayer.videoGravity = .resizeAspectFill
        view.layer.addSublayer(previewLayer)

        DispatchQueue.global(qos: .userInitiated).async {
            self.captureSession.startRunning()
        }
        
        let closeButton = UIButton(type: .system)
        closeButton.setTitle("Close", for: .normal)
        closeButton.tintColor = .white
        closeButton.addTarget(self, action: #selector(closeTapped), for: .touchUpInside)
        closeButton.frame = CGRect(x: 20, y: 50, width: 60, height: 40)
        view.addSubview(closeButton)
    }

    private func metadataObjectTypes(_ output: AVCaptureMetadataOutput) {
        output.metadataObjectTypes = [.qr]
    }

    @objc func closeTapped() {
        self.onCancel?()
        dismiss(animated: true)
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        if (captureSession?.isRunning == false) {
            DispatchQueue.global(qos: .userInitiated).async {
                self.captureSession.startRunning()
            }
        }
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        if (captureSession?.isRunning == true) {
            captureSession.stopRunning()
        }
    }

    func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
        captureSession.stopRunning()

        if let metadataObject = metadataObjects.first {
            guard let readableObject = metadataObject as? AVMetadataMachineReadableCodeObject else { return }
            guard let stringValue = readableObject.stringValue else { return }
            
            // Success! 
            onCodeScanned?(stringValue)
        }

        dismiss(animated: true)
    }
}
